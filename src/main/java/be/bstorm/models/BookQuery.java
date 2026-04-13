package be.bstorm.models;

public record BookQuery(
        String isbn,
        String title,
        Integer authorId,
        int page,
        int size
) {
}
