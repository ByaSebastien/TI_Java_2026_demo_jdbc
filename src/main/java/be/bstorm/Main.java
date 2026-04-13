package be.bstorm;


import be.bstorm.entities.Book;
import be.bstorm.repositories.BookRepository;

import java.util.List;


public class Main {
    static void main(String[] args) {

        BookRepository bookRepository = new BookRepository();

        List<Book> books = bookRepository.findAll();

        books.forEach(System.out::println);

        System.out.println("-------------------");

        Book book = Book.builder()
                .isbn("1234567891")
                .title("title")
                .description("description")
                .authorId(1)
                .build();

        bookRepository.save(book);

        System.out.println("-------------------");

        books = bookRepository.findAll();

        books.forEach(System.out::println);
    }
}
