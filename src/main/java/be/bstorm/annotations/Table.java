package be.bstorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer une classe comme entité mappée à une table de base de données.
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * @Table(name = "author")
 * public class Author {
 *     @Column(name = "id")
 *     private Integer id;
 *     // ...
 * }
 * }
 * </pre>
 *
 * <p>Le nom de la table est utilisé par le {@code CrudRepository} pour générer
 * les requêtes SQL. Si le nom n'est pas spécifié, le nom simple de la classe est utilisé
 * (converti en minuscules).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
    /**
     * Nom de la table en base de données.
     * Si vide, le nom simple de la classe sera utilisé (converti en minuscules).
     *
     * @return le nom de la table
     */
    String name() default "";
}
