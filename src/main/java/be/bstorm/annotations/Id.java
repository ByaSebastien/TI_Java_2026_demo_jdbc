package be.bstorm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour marquer un champ comme clé primaire d'une entité.
 *
 * <p>Exactement un champ par entité doit être annoté avec {@code @Id}.
 * Ce champ sera utilisé dans les clauses WHERE pour les recherches par ID,
 * les UPDATE, les DELETE.
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * @Table(name = "author")
 * public class Author {
 *     @Id(generation = GenerationType.GENERATED)
 *     @Column(name = "id")
 *     private Integer id;
 *     // ...
 * }
 * }
 * </pre>
 *
 * @see GenerationType
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Id {
    /**
     * Stratégie de génération de la clé primaire.
     *
     * @return {@link GenerationType#GENERATED} si auto-générée,
     *         {@link GenerationType#NOT_GENERATED} si fournie par le code
     */
    GenerationType generation() default GenerationType.GENERATED;
}
