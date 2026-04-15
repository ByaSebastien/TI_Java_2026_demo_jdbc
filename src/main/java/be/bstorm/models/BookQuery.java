package be.bstorm.models;

/**
 * Record encapsulant les critères de recherche et pagination pour les livres.
 *
 * <p>Ce record immuable est utilisé par {@link be.bstorm.repositories.BookRepository#findAll(BookQuery)}
 * pour construire une requête SQL filtrée et paginée.
 *
 * <p><b>Champs :</b>
 * <ul>
 *   <li>{@code isbn} - filtre partiel sur l'ISBN (LIKE si non null)</li>
 *   <li>{@code title} - filtre partiel sur le titre (LIKE si non null)</li>
 *   <li>{@code authorId} - filtre exact sur l'ID auteur</li>
 *   <li>{@code page} - numéro de la page (0-indexed)</li>
 *   <li>{@code size} - nombre de résultats par page</li>
 * </ul>
 *
 * <p><b>Exemple :</b>
 * <pre>
 * {@code
 * BookQuery query = new BookQuery(
 *     "978",           // ISBN contenant "978"
 *     "Java",          // Titre contenant "Java"
 *     1,               // Livres de l'auteur ID 1
 *     0,               // Première page
 *     10               // 10 résultats par page
 * );
 * List<Book> results = bookRepository.findAll(query);
 * }
 * </pre>
 *
 * @see be.bstorm.repositories.BookRepository
 */
public record BookQuery(
        String isbn,
        String title,
        Integer authorId,
        int page,
        int size
) {
}
