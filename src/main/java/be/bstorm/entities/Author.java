package be.bstorm.entities;

import be.bstorm.annotations.Column;
import be.bstorm.annotations.GenerationType;
import be.bstorm.annotations.Id;
import be.bstorm.annotations.Table;
import lombok.*;

import java.time.LocalDate;

/**
 * Entité représentant un auteur dans la base de données.
 *
 * <p>Cette classe utilise Lombok pour générer automatiquement les getters, setters,
 * constructeurs et méthodes {@code equals()}, {@code hashCode()}, {@code toString()}.
 *
 * <p><b>Annotations ORM :</b>
 * <ul>
 *   <li>{@code @Table(name = "author")} - mappe cette classe à la table "author"</li>
 *   <li>{@code @Column} sur chaque champ - mappe les champs aux colonnes</li>
 *   <li>{@code @Id(generation = GenerationType.GENERATED)} - marque {@code id} comme PK auto-générée</li>
 * </ul>
 *
 * <p><b>Champs :</b>
 * <ul>
 *   <li>{@code id} (Integer) - clé primaire auto-générée</li>
 *   <li>{@code firstName} (String) - prénom de l'auteur</li>
 *   <li>{@code lastName} (String) - nom de l'auteur</li>
 *   <li>{@code birthDate} (LocalDate) - date de naissance</li>
 * </ul>
 *
 * @see be.bstorm.repositories.AuthorRepository
 * @see be.bstorm.annotations.Table
 * @see be.bstorm.annotations.Column
 * @see be.bstorm.annotations.Id
 */
@EqualsAndHashCode(of = {"id"}) @ToString(of = {"id", "firstName", "lastName"})
@NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "author")
public class Author {

    @Getter
    @Id(generation = GenerationType.GENERATED)
    @Column(name = "id")
    private Integer id;

    @Getter @Setter
    @Column(name = "firstname")
    private String firstName;

    @Getter @Setter
    @Column(name = "lastname")
    private String lastName;

    @Getter @Setter
    @Column(name = "birthdate")
    private LocalDate birthDate;

    //region Ce que genere lombok

//    public Integer getId() {
//        return id;
//    }
//
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public LocalDate getBirthDate() {
//        return birthDate;
//    }
//
//    public void setBirthDate(LocalDate birthDate) {
//        this.birthDate = birthDate;
//    }
//
//    public Author() {}
//
//    public Author(Integer id, String firstName, String lastName, LocalDate birthDate) {
//        this.id = id;
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.birthDate = birthDate;
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (o == null || getClass() != o.getClass()) return false;
//        Author author = (Author) o;
//        return Objects.equals(id, author.id);
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(id);
//    }
//
//    @Override
//    public String toString() {
//        return "Author{" +
//                "id=" + id +
//                ", firstName='" + firstName + '\'' +
//                ", lastName='" + lastName + '\'' +
//                '}';
//    }

    //endregion
}
