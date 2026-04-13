package be.bstorm.utils;

import java.sql.PreparedStatement;
import java.sql.SQLException;

@FunctionalInterface
public interface StatementSetter {
    void setParams(PreparedStatement stmt) throws SQLException;
}
