package be.bstorm.utils;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Utilitaire pour gérer les connexions à la base de données.
 *
 * <p>Cette classe centralise la configuration de la connexion PostgreSQL.
 * Elle fournit une méthode {@code static} pour obtenir des connexions.
 *
 * <p><b>Configuration :</b>
 * <ul>
 *   <li>URL : {@code jdbc:postgresql://localhost:5432/demo_jdbc}</li>
 *   <li>User : {@code postgres}</li>
 *   <li>Password : {@code postgres}</li>
 * </ul>
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * try (Connection conn = ConnectionUtils.getConnection()) {
 *     // Utiliser la connexion
 *     PreparedStatement stmt = conn.prepareStatement(...);
 * } catch (SQLException e) {
 *     // Gérer l'erreur
 * }
 * }
 * </pre>
 *
 * <p><b>Note :</b> La connexion doit être ferée explicitement ou utilisée
 * dans un try-with-resources pour libérer les ressources automatiquement.
 */
public class ConnectionUtils {

    private static final String URL = "jdbc:postgresql://localhost:5432/demo_jdbc";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    /**
     * Obtient une nouvelle connexion à la base de données.
     *
     * @return une {@link java.sql.Connection} à la base PostgreSQL
     * @throws SQLException si la connexion échoue
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
