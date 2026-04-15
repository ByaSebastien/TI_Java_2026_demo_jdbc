package be.bstorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer un champ comme mappé à une colonne de base de données.
 *
 * <p>Cette annotation indique au {@code CrudRepository} que ce champ doit être
 * inclus dans les requêtes SQL SELECT, INSERT, UPDATE.
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * @Table(name = "author")
 * public class Author {
 *     @Column(name = "id")
 *     private Integer id;
 *
 *     @Column(name = "firstname")
 *     private String firstName;
 * }
 * }
 * </pre>
 *
 * <p><b>Champs sans cette annotation :</b>
 * <ul>
 *   <li>Sont ignorés lors des opérations CRUD</li>
 *   <li>Peuvent être annotés {@link NavigationProperty} pour les propriétés de relation</li>
 * </ul>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
    /**
     * Nom de la colonne en base de données.
     * Si vide, le nom du champ Java sera utilisé.
     *
     * @return le nom de la colonne
     */
    String name() default "";
}
