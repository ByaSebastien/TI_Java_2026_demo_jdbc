package be.bstorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer un champ comme propriété de navigation (non persistée en BD).
 *
 * <p>Les champs annotés {@code @NavigationProperty} :
 * <ul>
 *   <li>Ne sont pas inclus dans les requêtes SQL du {@code CrudRepository}</li>
 *   <li>Peuvent référencer d'autres entités (ex: {@code Author author} dans {@code Book})</li>
 *   <li>Doivent être hydratés manuellement via des JOINs personnalisés</li>
 * </ul>
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * @Table(name = "book")
 * public class Book {
 *     @Column(name = "isbn")
 *     private String isbn;
 *
 *     @Column(name = "author_id")
 *     private Integer authorId;
 *
 *     @NavigationProperty
 *     private Author author;  // Pas de colonne correspondante
 * }
 * }
 * </pre>
 *
 * <p>Dans ce cas, {@code BookRepository.findByIsbn()} charge le {@code author}
 * via un LEFT JOIN manuel.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NavigationProperty {
}
