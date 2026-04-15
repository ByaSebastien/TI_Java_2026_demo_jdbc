package be.bstorm;

import be.bstorm.entities.Author;
import be.bstorm.entities.Book;
import be.bstorm.models.BookQuery;
import be.bstorm.repositories.AuthorRepository;
import be.bstorm.repositories.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Classe principale pour tester les fonctionnalités du repository générique.
 *
 * <p>Ce programme démontre toutes les opérations CRUD et recherches disponibles
 * via le {@link be.bstorm.repositories.CrudRepository} générique.
 *
 * @see be.bstorm.repositories.AuthorRepository
 * @see be.bstorm.repositories.BookRepository
 */
public class Main {
    static void main() {
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║   🚀 DÉMONSTRATION DU CRUD REPOSITORY GÉNÉRIQUE            ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        AuthorRepository authorRepo = new AuthorRepository();
        BookRepository bookRepo = new BookRepository();

        // ============================================
        // 1️⃣ TEST DU CRUD AUTHOR
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  ✏️  TEST 1 : OPÉRATIONS CRUD SUR AUTHOR                   ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // 1.1 - Créer un nouvel auteur (INSERT)
        System.out.println("\n📌 [1.1] Créer un nouvel auteur (INSERT)");
        Author newAuthor = new Author(null, "Stephen", "King", LocalDate.of(1947, 9, 21));
        Author savedAuthor = authorRepo.save(newAuthor);
        Integer authorId = savedAuthor.getId();

        // 1.2 - Récupérer tous les auteurs (SELECT ALL)
        System.out.println("\n📌 [1.2] Récupérer tous les auteurs (SELECT ALL)");
        List<Author> allAuthors = authorRepo.findAll();
        displayAuthors(allAuthors);

        // 1.3 - Compter les auteurs (COUNT)
        System.out.println("\n📌 [1.3] Compter le nombre total d'auteurs");
        int authorCount = authorRepo.count();
        System.out.println("✅ Nombre total d'auteurs : " + authorCount);

        // 1.4 - Rechercher un auteur par ID (SELECT BY ID)
        System.out.println("\n📌 [1.4] Rechercher un auteur par ID : " + authorId);
        Optional<Author> foundAuthor = authorRepo.findById(authorId);
        foundAuthor.ifPresentOrElse(
                author -> System.out.println("✅ Auteur trouvé : " + author),
                () -> System.out.println("❌ Auteur non trouvé")
        );

        // 1.5 - Vérifier l'existence d'un auteur (EXISTS)
        System.out.println("\n📌 [1.5] Vérifier l'existence de l'auteur ID " + authorId);
        boolean exists = authorRepo.existsById(authorId);
        System.out.println("✅ Auteur existe : " + exists);

        // 1.6 - Mettre à jour un auteur (UPDATE)
        System.out.println("\n📌 [1.6] Mettre à jour l'auteur ID " + authorId);
        Author updatedAuthor = new Author(null, "Stephen", "King Jr.", LocalDate.of(1947, 9, 21));
        updatedAuthor = authorRepo.update(authorId, updatedAuthor);
        System.out.println("✅ Auteur mis à jour : " + updatedAuthor);

        // 1.7 - Récupérer l'auteur mis à jour
        System.out.println("\n📌 [1.7] Récupérer l'auteur mis à jour");
        Optional<Author> updatedFound = authorRepo.findById(authorId);
        updatedFound.ifPresent(author -> System.out.println("✅ Auteur mis à jour : " + author));

        // ============================================
        // 2️⃣ TEST DU CRUD BOOK
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  📚 TEST 2 : OPÉRATIONS CRUD SUR BOOK                      ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // 2.1 - Créer un nouveau livre (INSERT)
        System.out.println("\n📌 [2.1] Créer un nouveau livre (INSERT)");
        Book newBook = new Book(
                "9780451524935",
                "The Stand",
                "A post-apocalyptic novel",
                authorId,
                null
        );
        Book savedBook = bookRepo.save(newBook);

        // 2.2 - Récupérer tous les livres (SELECT ALL)
        System.out.println("\n📌 [2.2] Récupérer tous les livres (SELECT ALL)");
        List<Book> allBooks = bookRepo.findAll();
        displayBooks(allBooks);

        // 2.3 - Compter les livres (COUNT)
        System.out.println("\n📌 [2.3] Compter le nombre total de livres");
        int bookCount = bookRepo.count();
        System.out.println("✅ Nombre total de livres : " + bookCount);

        // 2.4 - Rechercher un livre par ISBN (SELECT BY ID)
        System.out.println("\n📌 [2.4] Rechercher un livre par ISBN : " + savedBook.getIsbn());
        Optional<Book> foundBook = bookRepo.findById(savedBook.getIsbn());
        foundBook.ifPresentOrElse(
                book -> System.out.println("✅ Livre trouvé : " + book),
                () -> System.out.println("❌ Livre non trouvé")
        );

        // 2.5 - Vérifier l'existence d'un livre (EXISTS)
        System.out.println("\n📌 [2.5] Vérifier l'existence du livre ISBN " + savedBook.getIsbn());
        boolean bookExists = bookRepo.existsById(savedBook.getIsbn());
        System.out.println("✅ Livre existe : " + bookExists);

        // 2.6 - Mettre à jour un livre (UPDATE)
        System.out.println("\n📌 [2.6] Mettre à jour le livre ISBN " + savedBook.getIsbn());
        Book updatedBook = new Book(
                savedBook.getIsbn(),
                "The Stand - Extended Edition",
                "A post-apocalyptic novel (extended)",
                authorId,
                null
        );
        bookRepo.update(savedBook.getIsbn(), updatedBook);

        // 2.7 - Récupérer le livre mis à jour
        System.out.println("\n📌 [2.7] Récupérer le livre mis à jour");
        Optional<Book> updatedFoundBook = bookRepo.findById(savedBook.getIsbn());
        updatedFoundBook.ifPresent(book -> System.out.println("✅ Livre mis à jour : " + book));

        // ============================================
        // 3️⃣ TEST DES RECHERCHES FILTRÉES & PAGINATION
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🔍 TEST 3 : RECHERCHES FILTRÉES & PAGINATION              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        // 3.1 - Recherche par titre partiel
        System.out.println("\n📌 [3.1] Recherche de livres contenant 'Stand'");
        List<Book> booksWithTitle = bookRepo.findAll(new BookQuery(
                null,       // Pas de filtre sur ISBN
                "Stand",    // Filtre sur titre
                null,       // Pas de filtre sur auteur
                0,          // Page 0
                10          // 10 résultats par page
        ));
        displayBooks(booksWithTitle);

        // 3.2 - Recherche par auteur
        System.out.println("\n📌 [3.2] Recherche de livres de l'auteur ID " + authorId);
        List<Book> booksByAuthor = bookRepo.findAll(new BookQuery(
                null,       // Pas de filtre sur ISBN
                null,       // Pas de filtre sur titre
                authorId,   // Filtre sur auteur
                0,          // Page 0
                10          // 10 résultats par page
        ));
        displayBooks(booksByAuthor);

        // 3.3 - Recherche par ISBN partiel
        System.out.println("\n📌 [3.3] Recherche de livres contenant '978' dans l'ISBN");
        List<Book> booksByIsbn = bookRepo.findAll(new BookQuery(
                "978",      // Filtre sur ISBN
                null,       // Pas de filtre sur titre
                null,       // Pas de filtre sur auteur
                0,          // Page 0
                10          // 10 résultats par page
        ));
        displayBooks(booksByIsbn);

        // ============================================
        // 4️⃣ TEST DES NAVIGATION PROPERTIES (JOIN)
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🔗 TEST 4 : NAVIGATION PROPERTIES (JOIN SQL)              ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        System.out.println("\n📌 [4.1] Récupérer un livre avec ses données d'auteur (LEFT JOIN)");
        Optional<Book> bookWithAuthor = bookRepo.findByIsbn(savedBook.getIsbn());
        if (bookWithAuthor.isPresent()) {
            Book book = bookWithAuthor.get();
            System.out.println("✅ Livre : " + book.getTitle());
            if (book.getAuthor() != null) {
                System.out.println("   └─ Auteur : " + book.getAuthor().getFirstName() + " " + 
                                 book.getAuthor().getLastName());
            } else {
                System.out.println("   └─ Pas d'auteur assigné");
            }
        }

        // ============================================
        // 5️⃣ TEST DE SUPPRESSION
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  🗑️  TEST 5 : SUPPRESSION (DELETE)                         ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        System.out.println("\n📌 [5.1] Supprimer le livre ISBN " + savedBook.getIsbn());
        Book deletedBook = bookRepo.deleteById(savedBook.getIsbn());
        System.out.println("✅ Livre supprimé : " + deletedBook.getTitle());

        System.out.println("\n📌 [5.2] Vérifier que le livre a été supprimé");
        boolean stillExists = bookRepo.existsById(savedBook.getIsbn());
        System.out.println("✅ Livre existe encore : " + stillExists);

        System.out.println("\n📌 [5.3] Supprimer l'auteur ID " + authorId);
        Author deletedAuthor = authorRepo.deleteById(authorId);
        System.out.println("✅ Auteur supprimé : " + deletedAuthor.getFirstName() + " " + 
                         deletedAuthor.getLastName());

        // ============================================
        // 📊 RÉSUMÉ FINAL
        // ============================================
        System.out.println("\n\n");
        System.out.println("╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  📊 RÉSUMÉ FINAL                                           ║");
        System.out.println("╚════════════════════════════════════════════════════════════╝");

        int finalAuthorCount = authorRepo.count();
        int finalBookCount = bookRepo.count();

        System.out.println("\n📈 Statistiques finales :");
        System.out.println("   ├─ Nombre d'auteurs : " + finalAuthorCount);
        System.out.println("   └─ Nombre de livres : " + finalBookCount);

        System.out.println("\n✨ Tous les tests ont été exécutés avec succès !");
        System.out.println("╚════════════════════════════════════════════════════════════╝");
    }

    /**
     * Affiche une liste d'auteurs de manière formatée.
     *
     * @param authors la liste des auteurs
     */
    private static void displayAuthors(List<Author> authors) {
        if (authors.isEmpty()) {
            System.out.println("❌ Aucun auteur trouvé");
            return;
        }
        System.out.println("✅ " + authors.size() + " auteur(s) trouvé(s) :");
        authors.forEach(author -> System.out.println("   ├─ [" + author.getId() + "] " + 
                author.getFirstName() + " " + author.getLastName() + 
                " (né le " + author.getBirthDate() + ")"));
    }

    /**
     * Affiche une liste de livres de manière formatée.
     *
     * @param books la liste des livres
     */
    private static void displayBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("❌ Aucun livre trouvé");
            return;
        }
        System.out.println("✅ " + books.size() + " livre(s) trouvé(s) :");
        books.forEach(book -> System.out.println("   ├─ [" + book.getIsbn() + "] " + 
                book.getTitle() + " (Auteur ID: " + book.getAuthorId() + ")"));
    }
}
