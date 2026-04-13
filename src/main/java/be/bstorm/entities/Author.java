package be.bstorm.entities;

import lombok.*;

import java.time.LocalDate;

@EqualsAndHashCode(of = {"id"}) @ToString(of = {"id", "firstName", "lastName"})
@NoArgsConstructor @AllArgsConstructor @Builder
public class Author {

    @Getter
    private Integer id;

    @Getter @Setter
    private String firstName;

    @Getter @Setter
    private String lastName;

    @Getter @Setter
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
