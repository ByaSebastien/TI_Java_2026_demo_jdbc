package be.bstorm.repositories;

import be.bstorm.annotations.Column;
import be.bstorm.annotations.GenerationType;
import be.bstorm.annotations.Id;
import be.bstorm.annotations.NavigationProperty;
import be.bstorm.annotations.Table;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static be.bstorm.utils.ConnectionUtils.getConnection;

/**
 * Repository générique pour gérer le CRUD de n'importe quelle entité via la réflexion et les annotations.
 *
 * <p>Cette classe abstraite génère automatiquement les requêtes SQL à partir des annotations
 * {@link Table}, {@link Column}, {@link Id} présentes sur l'entité. Les sous-classes
 * concrètes (ex: {@code AuthorRepository}, {@code BookRepository}) n'ont besoin que de
 * déclarer leur type générique.
 *
 * <p><b>Fonctionnement :</b>
 * <ul>
 *   <li>Les champs mappés en base sont détectés via {@code @Column}</li>
 *   <li>La clé primaire est identifiée par {@code @Id}</li>
 *   <li>Les propriétés de navigation ({@code @NavigationProperty}) sont ignorées</li>
 *   <li>Le SQL est construit dynamiquement selon les champs présents</li>
 *   <li>La stratégie ID peut être auto-générée ou fournie ({@link GenerationType})</li>
 * </ul>
 *
 * <p><b>Exemple d'utilisation :</b>
 * <pre>
 * {@code
 * public class AuthorRepository extends CrudRepository<Author, Integer> {
 * }
 *
 * // Utilisation :
 * AuthorRepository repo = new AuthorRepository();
 * Author author = new Author(null, "Jean", "Dupont", LocalDate.now());
 * repo.save(author);  // Génère automatiquement le SQL INSERT
 * }
 * </pre>
 *
 * @param <TEntity> le type de l'entité gérée (ex: {@code Author})
 * @param <TId>     le type de la clé primaire (ex: {@code Integer})
 * @see Table
 * @see Column
 * @see Id
 * @see NavigationProperty
 */
public abstract class CrudRepository<TEntity, TId> {

    private final Class<TEntity> entityClass;
    private final String tableName;
    private final Field idField;
    private final String idColumn;
    private final boolean isGeneratedId;

    private final List<Field> mappedFields;
    private final List<Field> insertFields;
    private final List<Field> updateFields;
    private final String selectColumns;

    /**
     * Constructeur protégé qui initialise le repository en analysant les annotations de l'entité.
     *
     * <p>La résolution du type {@code TEntity} se fait via la réflexion générique
     * ({@link ParameterizedType}). La classe doit être instanciée via une sous-classe
     * concrète qui déclare les paramètres de type (ex: {@code AuthorRepository extends CrudRepository<Author, Integer>}).
     *
     * <p>Tous les métadonnées sont calculées une seule fois à la construction et cachées.
     *
     * @throws IllegalArgumentException si l'entité n'a pas d'annotation {@code @Table}
     * @throws IllegalArgumentException si l'entité n'a pas d'annotation {@code @Column}
     * @throws IllegalArgumentException si l'entité n'a pas exactement un champ {@code @Id}
     */
    protected CrudRepository() {
        this.entityClass = resolveEntityClass();
        this.tableName = resolveTableName(entityClass);

        this.mappedFields = resolveMappedFields(entityClass);
        this.idField = resolveIdField(mappedFields);
        this.idColumn = resolveColumnName(idField);
        this.isGeneratedId = idField.getAnnotation(Id.class).generation() == GenerationType.GENERATED;

        this.insertFields = buildInsertFields();
        this.updateFields = buildUpdateFields();
        this.selectColumns = mappedFields.stream().map(this::resolveColumnName).collect(Collectors.joining(", "));
    }

    /**
     * Récupère toutes les lignes de la table et les retourne en tant que liste d'entités.
     *
     * <p>Requête générée : {@code SELECT colonne1, colonne2, ... FROM table}
     *
     * @return liste de toutes les entités (potentiellement vide)
     * @throws RuntimeException en cas d'erreur lors de l'exécution de la requête
     */
    public List<TEntity> findAll() {
        String sql = "SELECT " + selectColumns + " FROM " + tableName;
        logQuery("FINDALL", sql, List.of());

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<TEntity> entities = new ArrayList<>();
            while (rs.next()) {
                entities.add(buildEntity(rs));
            }
            logResult("FINDALL", entities.size() + " entity(ies) found");
            return entities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all entities from " + tableName, e);
        }
    }

    /**
     * Recherche une entité par sa clé primaire.
     *
     * <p>Requête générée : {@code SELECT ... FROM table WHERE pk_column = ?}
     *
     * @param id l'identifiant à rechercher
     * @return {@code Optional} contenant l'entité si trouvée, {@code Optional.empty()} sinon
     * @throws RuntimeException en cas d'erreur lors de l'exécution de la requête
     */
    public Optional<TEntity> findById(TId id) {
        String sql = "SELECT " + selectColumns + " FROM " + tableName + " WHERE " + idColumn + " = ?";
        logQuery("FINDBYID", sql, List.of(id));

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    TEntity entity = buildEntity(rs);
                    logResult("FINDBYID", "Entity found");
                    return Optional.of(entity);
                }
                logResult("FINDBYID", "No entity found");
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find entity by id", e);
        }
    }

    /**
     * Insère une nouvelle entité dans la base de données.
     *
     * <p>Si la clé primaire est auto-générée ({@link GenerationType#GENERATED}),
     * elle est assignée automatiquement au retour. Sinon, elle doit être fournie
     * dans l'entité avant l'appel.
     *
     * <p>Requête générée (avec PK auto-générée) :
     * {@code INSERT INTO table (col1, col2, ...) VALUES (?, ?, ...)}
     *
     * <p>Requête générée (avec PK fournie) :
     * {@code INSERT INTO table (pk, col1, col2, ...) VALUES (?, ?, ?, ...)}
     *
     * @param entity l'entité à insérer
     * @return l'entité avec sa clé primaire assignée (si générée)
     * @throws RuntimeException en cas d'erreur lors de l'exécution
     */
    public TEntity save(TEntity entity) {
        String sql = buildInsertSql();

        List<Object> params = insertFields.stream()
                .map(field -> getFieldValue(entity, field))
                .toList();
        logQuery("SAVE/INSERT", sql, params);

        try (Connection conn = getConnection();
             PreparedStatement stmt = createInsertStatement(conn, sql)) {

            bindFields(stmt, entity, insertFields);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Insert affected 0 rows for " + tableName);
            }

            if (isGeneratedId) {
                assignGeneratedId(stmt, entity);
                logResult("SAVE/INSERT", "Entity inserted with generated ID: " + getFieldValue(entity, idField));
            } else {
                logResult("SAVE/INSERT", "Entity inserted");
            }

            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save entity in " + tableName, e);
        }
    }

    /**
     * Met à jour la ligne correspondant à l'identifiant donné avec les valeurs de l'entité.
     *
     * <p>Seuls les champs non-clé primaire sont mis à jour.
     * La clé primaire est définie via la clause WHERE et ne change jamais.
     *
     * <p>Requête générée :
     * {@code UPDATE table SET col1 = ?, col2 = ?, ... WHERE pk_column = ?}
     *
     * @param id     la clé primaire identifiant la ligne à modifier
     * @param entity l'entité contenant les nouvelles valeurs
     * @return l'entité avec sa clé primaire mis à jour
     * @throws RuntimeException si aucune ligne n'a été modifiée (id inexistant)
     */
    public TEntity update(TId id, TEntity entity) {
        String sql = buildUpdateSql();

        List<Object> params = Stream.concat(
                updateFields.stream().map(field -> getFieldValue(entity, field)),
                Stream.of(id)
        ).toList();
        logQuery("UPDATE", sql, params);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int nextIndex = bindFields(stmt, entity, updateFields);
            stmt.setObject(nextIndex, id);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No entity updated for id: " + id);
            }

            setFieldValue(entity, idField, id);
            logResult("UPDATE", "Entity updated");
            return entity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update entity in " + tableName, e);
        }
    }

    /**
     * Supprime la ligne correspondant à l'identifiant donné.
     *
     * <p>Requête générée : {@code DELETE FROM table WHERE pk_column = ?}
     *
     * @param id la clé primaire de la ligne à supprimer
     * @return l'entité qui a été supprimée
     * @throws RuntimeException si aucune ligne n'a été supprimée (id inexistant)
     */
    public TEntity deleteById(TId id) {
        TEntity existing = findById(id)
                .orElseThrow(() -> new RuntimeException("No entity found for id: " + id));

        String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
        logQuery("DELETE", sql, List.of(id));

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No entity deleted for id: " + id);
            }

            logResult("DELETE", "Entity deleted");
            return existing;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete entity from " + tableName, e);
        }
    }

    /**
     * Compte le nombre total de lignes dans la table.
     *
     * <p>Requête générée : {@code SELECT COUNT(*) FROM table}
     *
     * @return le nombre de lignes (0 si la table est vide)
     * @throws RuntimeException en cas d'erreur lors de l'exécution
     */
    public int count() {
        String sql = "SELECT COUNT(*) FROM " + tableName;
        logQuery("COUNT", sql, List.of());

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int count = rs.next() ? rs.getInt(1) : 0;
            logResult("COUNT", count + " entity(ies)");
            return count;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count entities in " + tableName, e);
        }
    }

    /**
     * Vérifie si une ligne avec l'identifiant donné existe dans la table.
     *
     * <p>Requête générée : {@code SELECT 1 FROM table WHERE pk_column = ?}
     *
     * @param id l'identifiant à vérifier
     * @return {@code true} si l'entité existe, {@code false} sinon
     * @throws RuntimeException en cas d'erreur lors de l'exécution
     */
    public boolean existsById(TId id) {
        String sql = "SELECT 1 FROM " + tableName + " WHERE " + idColumn + " = ?";
        logQuery("EXISTS", sql, List.of(id));

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                boolean exists = rs.next();
                logResult("EXISTS", exists ? "Entity found" : "No entity found");
                return exists;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check entity existence", e);
        }
    }

    /**
     * Transforme la ligne courante du ResultSet en une instance de l'entité.
     *
     * <p>Cette méthode utilise la réflexion pour :
     * <ul>
     *   <li>Instancier l'entité via son constructeur sans argument</li>
     *   <li>Lire chaque colonne depuis le ResultSet</li>
     *   <li>Assigner les valeurs aux champs correspondants</li>
     * </ul>
     *
     * <p>Les champs annotés {@link NavigationProperty} sont ignorés (non mappés en base).
     *
     * @param rs le ResultSet positionné sur la ligne à traiter
     * @return une nouvelle instance de l'entité avec les données de la ligne
     * @throws RuntimeException en cas d'erreur de réflexion ou d'accès DB
     */
    protected TEntity buildEntity(ResultSet rs) {
        try {
            Constructor<TEntity> constructor = entityClass.getDeclaredConstructor();
            constructor.setAccessible(true);

            TEntity entity = constructor.newInstance();
            mappedFields.forEach(field -> {
                String column = resolveColumnName(field);
                try {
                    Object value = rs.getObject(column, field.getType());
                    setFieldValue(entity, field, value);
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to read field: " + field.getName(), e);
                }
            });
            return entity;
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to build entity: " + entityClass.getName(), e);
        }
    }

    /**
     * Résout la classe réelle de TEntity via la réflexion générique.
     *
     * <p>Utilise {@link ParameterizedType} pour lire le paramètre de type
     * depuis la chaîne d'héritage.
     *
     * @return la classe de l'entité
     * @throws IllegalStateException si le type ne peut pas être résolu
     */
    @SuppressWarnings("unchecked")
    private Class<TEntity> resolveEntityClass() {
        ParameterizedType paramType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<TEntity>) paramType.getActualTypeArguments()[0];
    }

    /**
     * Résout le nom de la table depuis l'annotation {@link Table}.
     *
     * @param type la classe de l'entité
     * @return le nom de la table (ou le nom de la classe si non annoté)
     */
    private String resolveTableName(Class<TEntity> type) {
        Table table = type.getAnnotation(Table.class);
        if (table == null || table.name().isBlank()) {
            return type.getSimpleName().toLowerCase();
        }
        return table.name();
    }

    /**
     * Résout tous les champs mappés en base (annotés {@link Column}, hors {@link NavigationProperty}).
     *
     * @param type la classe de l'entité
     * @return liste des champs à inclure dans les requêtes SQL
     * @throws IllegalArgumentException si aucun champ @Column n'est trouvé
     */
    private List<Field> resolveMappedFields(Class<TEntity> type) {
        return Arrays.stream(type.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(NavigationProperty.class))
                .peek(field -> field.setAccessible(true))
                .toList();
    }

    /**
     * Résout le champ clé primaire (annoté {@link Id}).
     *
     * @param fields la liste des champs mappés
     * @return le champ @Id unique
     * @throws java.util.NoSuchElementException si aucun ou plusieurs @Id sont trouvés
     */
    private Field resolveIdField(List<Field> fields) {
        return fields.stream()
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Résout le nom de colonne d'un champ depuis l'annotation {@link Column}.
     *
     * @param field le champ Java
     * @return le nom de la colonne en base (ou le nom du champ si non annoté)
     */
    private String resolveColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        if (column == null || column.name().isBlank()) {
            return field.getName();
        }
        return column.name();
    }

    /**
     * Calcule les champs à insérer lors d'un INSERT.
     *
     * <p>Si la PK est auto-générée, elle est exclue de l'insertion.
     * Sinon, tous les champs mappés sont inclus.
     *
     * @return liste des champs pour l'INSERT
     */
    private List<Field> buildInsertFields() {
        if (!isGeneratedId) {
            return mappedFields;
        }

        return mappedFields.stream()
                .filter(field -> !field.equals(idField))
                .toList();
    }

    /**
     * Calcule les champs à mettre à jour lors d'un UPDATE.
     *
     * <p>La clé primaire est toujours exclue (elle sert en WHERE, jamais en SET).
     *
     * @return liste des champs pour l'UPDATE
     */
    private List<Field> buildUpdateFields() {
        return mappedFields.stream()
                .filter(field -> !field.equals(idField))
                .toList();
    }

    /**
     * Construit le SQL INSERT.
     *
     * @return la requête INSERT paramétrée
     */
    private String buildInsertSql() {
        if (insertFields.isEmpty()) {
            return "INSERT INTO " + tableName + " DEFAULT VALUES";
        }

        String columns = insertFields.stream()
                .map(this::resolveColumnName)
                .collect(Collectors.joining(", "));

        String placeholders = String.join(", ", Collections.nCopies(insertFields.size(), "?"));

        return "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + placeholders + ")";
    }

    /**
     * Construit le SQL UPDATE.
     *
     * @return la requête UPDATE paramétrée
     */
    private String buildUpdateSql() {
        String setClause = updateFields.stream()
                .map(field -> resolveColumnName(field) + " = ?")
                .collect(Collectors.joining(", "));

        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + idColumn + " = ?";
    }

    /**
     * Crée un PreparedStatement avec ou sans support des clés générées.
     *
     * @param conn  la connexion DB
     * @param query la requête SQL
     * @return le PreparedStatement configuré
     * @throws SQLException en cas d'erreur
     */
    private PreparedStatement createInsertStatement(Connection conn, String query) throws SQLException {
        if (isGeneratedId) {
            return conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
        }
        return conn.prepareStatement(query);
    }

    /**
     * Lie les valeurs des champs aux paramètres du PreparedStatement.
     *
     * @param stmt   le PreparedStatement
     * @param entity l'entité à lier
     * @param fields les champs à lier
     * @return l'index suivant disponible dans le statement
     * @throws SQLException en cas d'erreur
     */
    private int bindFields(PreparedStatement stmt, TEntity entity, List<Field> fields) throws SQLException {
        int index = 1;
        for(Field field : fields) {
            try {
                Object value = getFieldValue(entity, field);
                stmt.setObject(index++, value);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to bind field: " + field.getName(), e);
            }
        }
        return index;
    }

    /**
     * Assigne la clé générée à l'entité après un INSERT.
     *
     * @param stmt   le PreparedStatement exécuté
     * @param entity l'entité à mettre à jour
     * @throws SQLException en cas d'erreur
     */
    private void assignGeneratedId(PreparedStatement stmt, TEntity entity) throws SQLException {
        try (ResultSet keys = stmt.getGeneratedKeys()) {
            if (!keys.next()) {
                return;
            }

            Object generatedValue = keys.getObject(1, idField.getType());
            setFieldValue(entity, idField, generatedValue);
        }
    }

    /**
     * Lit la valeur d'un champ via la réflexion.
     *
     * @param entity l'entité
     * @param field  le champ à lire
     * @return la valeur du champ
     * @throws RuntimeException si l'accès au champ échoue
     */
    private Object getFieldValue(TEntity entity, Field field) {
        try {
            return field.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot read field: " + field.getName(), e);
        }
    }

    /**
     * Écrit la valeur d'un champ via la réflexion.
     *
     * @param entity l'entité
     * @param field  le champ à écrire
     * @param value  la nouvelle valeur
     * @throws RuntimeException si l'accès au champ échoue
     */
    private void setFieldValue(TEntity entity, Field field, Object value) {
        try {
            field.set(entity, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Cannot write field: " + field.getName(), e);
        }
    }

    // ====================================
    // 🔍 LOGGING & DEBUG
    // ====================================

    /**
     * Affiche une requête SQL avec ses paramètres de manière formatée.
     * Adapte dynamiquement la largeur du cadre et enveloppe le SQL sur plusieurs lignes.
     *
     * @param operation le type d'opération (FINDALL, FINDBYID, SAVE, UPDATE, DELETE, etc.)
     * @param sql       la requête SQL
     * @param params    les paramètres liés
     */
    private void logQuery(String operation, String sql, List<Object> params) {
        System.out.println();

        // Calculer la largeur nécessaire
        int width = calculateWidth(sql, operation, params);

        // Afficher le cadre supérieur
        printTopBorder(width);
        printHeaderLine(width, "🔍 SQL " + operation + " - " + entityClass.getSimpleName());
        printSeparator(width);

        // Afficher le SQL (enveloppé si nécessaire)
        printWrappedSQL(sql, width);

        // Afficher les paramètres s'il y en a
        if (!params.isEmpty()) {
            printSeparator(width);
            printLine(width, "📌 Paramètres :");
            java.util.stream.IntStream.range(0, params.size())
                    .forEach(i -> {
                        Object param = params.get(i);
                        String value = param == null ? "NULL" : param.toString();
                        printLine(width, "   [" + (i + 1) + "] → " + value);
                    });
        }

        // Afficher le cadre inférieur
        printBottomBorder(width);
    }

    /**
     * Affiche le résultat d'une opération de manière formatée.
     *
     * @param operation le type d'opération
     * @param result    la description du résultat
     */
    private void logResult(String operation, String result) {
        int width = 60; // Largeur fixe pour rester cohérent
        printLine(width, "✅ Résultat (" + operation + ") : " + result);
        printBottomBorder(width);
    }

    /**
     * Calcule la largeur nécessaire pour afficher le contenu.
     * Largeur fixe à 60 caractères pour garder un format compact.
     * Le SQL s'enveloppera sur plusieurs lignes si nécessaire.
     */
    private int calculateWidth(String sql, String operation, List<Object> params) {
        return 60; // Largeur fixe pour garder un format compact
    }

    /**
     * Affiche une ligne du cadre supérieur.
     */
    private void printTopBorder(int width) {
        System.out.print("┌");
        for (int i = 0; i < width - 2; i++) {
            System.out.print("─");
        }
        System.out.println("┐");
    }

    /**
     * Affiche une ligne du cadre inférieur.
     */
    private void printBottomBorder(int width) {
        System.out.print("└");
        for (int i = 0; i < width - 2; i++) {
            System.out.print("─");
        }
        System.out.println("┘");
    }

    /**
     * Affiche une ligne de séparation.
     */
    private void printSeparator(int width) {
        System.out.print("├");
        for (int i = 0; i < width - 2; i++) {
            System.out.print("─");
        }
        System.out.println("┤");
    }

    /**
     * Affiche une ligne de contenu avec le texte centré/justifié.
     */
    private void printLine(int width, String content) {
        int maxContentWidth = width - 4; // 2 pour les bordures │ et 2 pour les espaces

        if (content.length() <= maxContentWidth) {
            System.out.println("│ " + content + " ".repeat(maxContentWidth - content.length()) + " │");
        } else {
            // Si le contenu est trop long, le découper
            int offset = 0;
            while (offset < content.length()) {
                int endIndex = Math.min(offset + maxContentWidth, content.length());
                String part = content.substring(offset, endIndex);
                System.out.println("│ " + part + " ".repeat(maxContentWidth - part.length()) + " │");
                offset = endIndex;
            }
        }
    }

    /**
     * Affiche une ligne d'en-tête.
     */
    private void printHeaderLine(int width, String header) {
        int maxContentWidth = width - 4;
        if (header.length() <= maxContentWidth) {
            System.out.println("│ " + header + " ".repeat(maxContentWidth - header.length()) + " │");
        } else {
            System.out.println("│ " + header.substring(0, maxContentWidth) + " │");
        }
    }

    /**
     * Affiche le SQL en l'enveloppant sur plusieurs lignes si nécessaire.
     */
    private void printWrappedSQL(String sql, int width) {
        int maxContentWidth = width - 4; // Largeur disponible pour le contenu

        if (sql.length() <= maxContentWidth) {
            // Si le SQL tient sur une ligne
            System.out.println("│ " + sql + " ".repeat(maxContentWidth - sql.length()) + " │");
        } else {
            // Envelopper le SQL sur plusieurs lignes, en essayant de le couper à des endroits judicieux
            String[] words = sql.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if ((currentLine.length() + word.length() + 1) <= maxContentWidth) {
                    if (!currentLine.isEmpty()) {
                        currentLine.append(" ");
                    }
                    currentLine.append(word);
                } else {
                    // Afficher la ligne complète
                    if (!currentLine.isEmpty()) {
                        System.out.println("│ " + currentLine.toString() +
                                " ".repeat(maxContentWidth - currentLine.length()) + " │");
                        currentLine = new StringBuilder(word);
                    } else {
                        // Le mot lui-même est trop long, le couper
                        System.out.println("│ " + word.substring(0, maxContentWidth) +
                                " ".repeat(maxContentWidth - Math.min(word.length(), maxContentWidth)) + " │");
                        word = word.substring(maxContentWidth);
                        if (!word.isEmpty()) {
                            currentLine = new StringBuilder(word);
                        }
                    }
                }
            }

            // Afficher la dernière ligne
            if (!currentLine.isEmpty()) {
                System.out.println("│ " + currentLine.toString() +
                        " ".repeat(maxContentWidth - currentLine.length()) + " │");
            }
        }
    }
}