package be.bstorm.entities;


import lombok.*;

@NoArgsConstructor @AllArgsConstructor @Builder
@EqualsAndHashCode(of = {"isbn"}) @ToString(of = {"isbn", "title"})
public class Book {

    @Getter @Setter
    private String isbn;

    @Getter @Setter
    private String title;

    @Getter @Setter
    private String description;

    @Getter @Setter
    private Integer authorId;

    @Getter @Setter
    private Author author; // Navigation property (utile en cas je join)
}
