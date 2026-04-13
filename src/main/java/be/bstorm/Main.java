package be.bstorm;


import be.bstorm.entities.Book;
import be.bstorm.models.BookQuery;
import be.bstorm.repositories.BookRepository;

import java.util.List;


public class Main {
    static void main(String[] args) {

        BookRepository bookRepository = new BookRepository();

        List<Book> books = bookRepository.findAll(new BookQuery(
                "123",
                "e",
                1,
                0,
                10
        ));

        books.forEach(System.out::println);

//        bookRepository.save(new Book(
//                "1234567891",
//                "Mon super livre",
//                "Un livre génial",
//                1,
//                null
//        ));
//
//        Book book = bookRepository.findByIsbn("1234567891").orElse(null);
//
//        System.out.println(book);
//        System.out.println(book.getAuthor());
//
//
//        System.out.println("Done");
    }
}
