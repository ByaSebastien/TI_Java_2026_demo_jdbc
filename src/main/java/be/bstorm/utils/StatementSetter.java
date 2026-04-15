package be.bstorm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Interface fonctionnelle pour configurer les paramètres d'un PreparedStatement.
 *
 * <p>Cette interface est utilisée comme callback pour remplir les paramètres (?)
 * d'une requête SQL préparée. Elle encapsule la logique de binding des valeurs.
 *
 * <p><b>Usage :</b>
 * <pre>
 * {@code
 * StatementSetter setter = stmt -> {
 *     stmt.setInt(1, userId);
 *     stmt.setString(2, userName);
 * };
 * // Plus tard :
 * PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM user WHERE id = ? AND name = ?");
 * setter.setParams(pstmt);
 * }
 * </pre>
 *
 * @see java.sql.PreparedStatement
 */
@FunctionalInterface
public interface StatementSetter {
    /**
     * Configure les paramètres d'un PreparedStatement.
     *
     * @param stmt le {@code PreparedStatement} à configurer
     * @throws SQLException en cas d'erreur lors du binding
     */
    void setParams(PreparedStatement stmt) throws SQLException;
}
