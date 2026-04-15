package be.bstorm;


import be.bstorm.entities.Book;
import be.bstorm.models.BookQuery;
import be.bstorm.repositories.AuthorRepository;
import be.bstorm.repositories.BookRepository;

import java.util.List;


/**
 * Classe principale pour tester les fonctionnalités du repository générique.
 *
 * <p>Ce point d'entrée démontre l'utilisation de {@link be.bstorm.repositories.BookRepository}
 * avec des critères de recherche et pagination.
 *
 * @see be.bstorm.repositories.BookRepository
 * @see be.bstorm.models.BookQuery
 */
public class Main {
    /**
     * Point d'entrée du programme.
     *
     * <p>Crée un {@code BookRepository}, construit une requête de recherche filtrée et paginée,
     * puis affiche les résultats dans la console.
     *
     * @param args arguments de la ligne de commande (non utilisés)
     */
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

        AuthorRepository authorRepository = new AuthorRepository();

         authorRepository.findAll().forEach(System.out::println);
    }
}
