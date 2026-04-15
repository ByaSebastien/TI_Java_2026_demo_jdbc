package be.bstorm.repositories;

import be.bstorm.entities.Author;
import be.bstorm.entities.Book;
import be.bstorm.models.BookQuery;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static be.bstorm.utils.ConnectionUtils.getConnection;

/**
 * Repository pour la gestion des livres ({@link Book}).
 *
 * <p>Hérite de {@link CrudRepository} et ajoute des méthodes de recherche personnalisées
 * avec des critères de filtrage (ISBN, titre, auteur) et support de la pagination.
 *
 * <p><b>Méthodes supplémentaires :</b>
 * <ul>
 *   <li>{@link #findAll(BookQuery)} - recherche avec filtrage et pagination</li>
 *   <li>{@link #findByIsbn(String)} - récupère un livre par ISBN avec ses données d'auteur</li>
 *   <li>{@link #saveAll(List)} - insertion en lot de plusieurs livres</li>
 * </ul>
 *
 * @see CrudRepository
 * @see Book
 * @see BookQuery
 */
public class BookRepository extends CrudRepository<Book, String> {

    /**
     * Recherche des livres selon des critères et un système de pagination.
     *
     * <p>Utilise {@link BookQuery} pour appliquer des filtres :
     * <ul>
     *   <li>ISBN (recherche partielle avec LIKE)</li>
     *   <li>Titre (recherche partielle avec LIKE)</li>
     *   <li>Auteur ID (correspondance exacte)</li>
     * </ul>
     *
     * <p>La pagination est basée sur le numéro de page et la taille de page.
     *
     * @param query les critères de recherche et pagination
     * @return liste des livres correspondant aux critères
     * @throws RuntimeException en cas d'erreur SQL
     */
    public List<Book> findAll(BookQuery query) {
        String sql = buildFindAllQuery(query);
        List<Object> params = buildFindAllParams(query);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            setStatementParams(stmt, params);

            List<Book> books = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(mapBook(rs));
                }
            }
            return books;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all books", e);
        }
    }

    /**
     * Récupère un livre par son ISBN avec les informations complètes de son auteur.
     *
     * <p>Effectue une jointure LEFT JOIN avec la table author pour récupérer
     * les données d'auteur (nom, prénom, date de naissance) et les attacher
     * au livre via la propriété de navigation {@link Book#author}.
     *
     * <p><b>Note :</b> Les propriétés de navigation (annotations {@link NavigationProperty})
     * ne sont pas automatiquement remplies par le repository générique. Cette méthode
     * le fait manuellement via un JOIN SQL personnalisé.
     *
     * @param isbn l'ISBN à rechercher (clé primaire)
     * @return {@code Optional} contenant le livre avec ses données d'auteur, ou vide si non trouvé
     * @throws RuntimeException en cas d'erreur SQL
     */
    public Optional<Book> findByIsbn(String isbn) {
        String query = """
                SELECT b.isbn, b.title, b.description, b.author_id,
                       a.id AS author_id_join, a.firstname AS author_firstname, a.lastname AS author_lastname, a.birthdate AS author_birthdate
                FROM book b
                LEFT JOIN author a ON b.author_id = a.id
                WHERE b.isbn = ?
                """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, isbn);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapBookWithAuthor(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find book by ISBN: " + isbn, e);
        }
    }

    /**
     * Insère plusieurs livres en une seule opération par batch.
     *
     * <p>Utilise {@code addBatch()} pour regrouper les INSERTs et {@code executeBatch()}
     * pour les exécuter en lot, ce qui est plus performant qu'appeler {@code save()}
     * individuellement.
     *
     * @param books la liste des livres à insérer
     * @throws RuntimeException en cas d'erreur SQL
     */
    public void saveAll(List<Book> books) {
        if (books == null || books.isEmpty()) {
            return;
        }

        String query = "INSERT INTO book (isbn, title, description, author_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            for (Book book : books) {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getDescription());
                stmt.setObject(4, book.getAuthorId());
                stmt.addBatch();
            }

            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save all books", e);
        }
    }

    /**
     * Convertit une ligne de ResultSet en entité {@link Book} minimale.
     *
     * <p>Utilise le pattern builder de Lombok pour créer une instance de Book.
     *
     * @param rs le ResultSet positionné sur la ligne
     * @return une nouvelle instance de Book
     * @throws SQLException en cas d'erreur de lecture
     */
    private Book mapBook(ResultSet rs) throws SQLException {
        return Book.builder()
                .isbn(rs.getString("isbn"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .authorId((Integer) rs.getObject("author_id"))
                .build();
    }

    /**
     * Convertit une ligne de ResultSet en entité {@link Book} enrichie avec ses données d'auteur.
     *
     * <p>Appelle d'abord {@link #mapBook(ResultSet)} pour créer le Book,
     * puis hydrate manuellement la propriété de navigation {@code author}
     * si une correspondance d'auteur existe.
     *
     * @param rs le ResultSet positionné sur la ligne (contenant les colonnes auteur)
     * @return une nouvelle instance de Book avec l'auteur complet
     * @throws SQLException en cas d'erreur de lecture
     */
    private Book mapBookWithAuthor(ResultSet rs) throws SQLException {
        Book book = mapBook(rs);

        Integer authorId = (Integer) rs.getObject("author_id_join");
        if (authorId == null) {
            return book;
        }

        Author author = Author.builder()
                .id(authorId)
                .firstName(rs.getString("author_firstname"))
                .lastName(rs.getString("author_lastname"))
                .birthDate(rs.getObject("author_birthdate", LocalDate.class))
                .build();

        book.setAuthor(author);
        return book;
    }

    /**
     * Construit dynamiquement la clause WHERE d'une requête SELECT basée sur les critères.
     *
     * @param query les critères de recherche
     * @return la requête SQL complète
     */
    private String buildFindAllQuery(BookQuery query) {
        StringBuilder sql = new StringBuilder("SELECT isbn, title, description, author_id FROM book WHERE 1 = 1");

        if (query.isbn() != null && !query.isbn().isBlank()) {
            sql.append(" AND isbn LIKE ?");
        }
        if (query.title() != null && !query.title().isBlank()) {
            sql.append(" AND title LIKE ?");
        }
        if (query.authorId() != null) {
            sql.append(" AND author_id = ?");
        }

        sql.append(" ORDER BY isbn LIMIT ? OFFSET ?");
        return sql.toString();
    }

    /**
     * Construit la liste des paramètres SQL correspondant aux critères de recherche.
     *
     * @param query les critères de recherche
     * @return liste des valeurs à binder aux paramètres (?)
     */
    private List<Object> buildFindAllParams(BookQuery query) {
        List<Object> params = new ArrayList<>();

        if (query.isbn() != null && !query.isbn().isBlank()) {
            params.add("%" + query.isbn() + "%");
        }
        if (query.title() != null && !query.title().isBlank()) {
            params.add("%" + query.title() + "%");
        }
        if (query.authorId() != null) {
            params.add(query.authorId());
        }

        params.add(query.size());
        params.add(query.page() * query.size());
        return params;
    }

    /**
     * Lie (bind) les paramètres à un PreparedStatement de manière séquentielle.
     *
     * @param stmt le PreparedStatement à remplir
     * @param params la liste des valeurs à binder
     * @throws SQLException en cas d'erreur
     */
    private void setStatementParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }
}
