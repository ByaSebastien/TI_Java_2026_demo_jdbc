package be.bstorm.annotations;

/**
 * Énumération des stratégies de génération de clé primaire.
 *
 * <p>Utilisée dans l'annotation {@link Id} pour indiquer si la clé primaire
 * est auto-générée par la base de données ou fournie par le code.
 *
 * @see Id
 */
public enum GenerationType {
    /**
     * La clé primaire est auto-générée par la base de données (SERIAL/IDENTITY/AUTO_INCREMENT).
     * Le {@code CrudRepository} omettra cette clé de l'INSERT et la récupérera
     * après l'insertion via {@code RETURN_GENERATED_KEYS}.
     */
    GENERATED,

    /**
     * La clé primaire est fournie par le code (ex: ISBN string).
     * Le {@code CrudRepository} l'inclura dans l'INSERT.
     */
    NOT_GENERATED
}
