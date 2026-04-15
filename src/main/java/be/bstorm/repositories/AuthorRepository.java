package be.bstorm.repositories;

import be.bstorm.entities.Author;

/**
 * Repository concret pour la gestion des auteurs ({@link Author}).
 *
 * <p>Cette classe hérite de {@link CrudRepository} et propose des opérations CRUD
 * standard entièrement générées à partir des annotations de l'entité {@code Author}.
 *
 * <p><b>Exemple d'utilisation :</b>
 * <pre>
 * {@code
 * AuthorRepository repo = new AuthorRepository();
 *
 * // Créer et insérer un auteur
 * Author author = new Author(null, "Jane", "Doe", LocalDate.of(1980, 5, 15));
 * repo.save(author);  // id auto-généré
 * System.out.println(author.getId());  // 1, 2, 3, ...
 *
 * // Rechercher par ID
 * Optional<Author> found = repo.findById(1);
 *
 * // Tous les auteurs
 * List<Author> all = repo.findAll();
 *
 * // Mettre à jour
 * author.setFirstName("Janet");
 * repo.update(author.getId(), author);
 *
 * // Supprimer
 * repo.deleteById(1);
 * }
 * </pre>
 *
 * @see CrudRepository
 * @see Author
 */
public class AuthorRepository extends CrudRepository<Author, Integer> {
}
