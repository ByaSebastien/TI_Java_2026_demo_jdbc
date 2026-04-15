package be.bstorm.entities;


import be.bstorm.annotations.*;
import lombok.*;

/**
 * Entité représentant un livre dans la base de données.
 *
 * <p>Cette classe utilise Lombok pour générer automatiquement les getters, setters,
 * constructeurs et méthodes {@code equals()}, {@code hashCode()}, {@code toString()}.
 *
 * <p><b>Annotations ORM :</b>
 * <ul>
 *   <li>{@code @Table(name = "book")} - mappe cette classe à la table "book"</li>
 *   <li>{@code @Column} sur chaque champ persisté - mappe aux colonnes</li>
 *   <li>{@code @Id(generation = GenerationType.NOT_GENERATED)} - ISBN fourni (pas auto-généré)</li>
 *   <li>{@code @NavigationProperty} sur {@code author} - propriété de relation, non persistée</li>
 * </ul>
 *
 * <p><b>Champs mappés :</b>
 * <ul>
 *   <li>{@code isbn} (String) - clé primaire fournie par l'utilisateur</li>
 *   <li>{@code title} (String) - titre du livre</li>
 *   <li>{@code description} (String) - description ou résumé</li>
 *   <li>{@code authorId} (Integer) - clé étrangère vers la table author</li>
 * </ul>
 *
 * <p><b>Propriétés de navigation :</b>
 * <ul>
 *   <li>{@code author} (Author) - référence complète de l'auteur (chargée via JOIN personnalisé)</li>
 * </ul>
 *
 * @see be.bstorm.repositories.BookRepository
 * @see be.bstorm.entities.Author
 * @see be.bstorm.annotations.Table
 * @see be.bstorm.annotations.Column
 * @see be.bstorm.annotations.Id
 * @see be.bstorm.annotations.NavigationProperty
 */
@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = {"isbn"}) @ToString(of = {"isbn", "title"})
@Table(name = "book")
public class Book {

    @Getter @Setter
    @Id(generation = GenerationType.NOT_GENERATED)
    @Column(name = "isbn")
    private String isbn;

    @Getter @Setter
    @Column(name = "title")
    private String title;

    @Getter @Setter
    @Column(name = "description")
    private String description;

    @Getter @Setter
    @Column(name = "author_id")
    private Integer authorId;

    @Getter @Setter
    @NavigationProperty
    private Author author; // Navigation property (utile en cas je join)
}
