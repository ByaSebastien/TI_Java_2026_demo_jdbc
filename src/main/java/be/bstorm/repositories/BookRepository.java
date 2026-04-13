package be.bstorm.repositories;

import be.bstorm.entities.Author;
import be.bstorm.entities.Book;
import be.bstorm.models.BookQuery;
import be.bstorm.utils.StatementSetter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static be.bstorm.utils.ConnectionUtils.getConnection;

public class BookRepository {

    public List<Book> findAll(BookQuery query) {
        try (Connection conn = getConnection()) {
            String sql = buildFindAllQuery(query);
            List<Object> params = buildFindAllParams(query);

            PreparedStatement stmt = conn.prepareStatement(sql);
            setStatementParams(stmt, params);

            List<Book> books = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    books.add(buildEntity(rs));
                }
            }
            return books;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all books", e);
        }
    }

    public Optional<Book> findByIsbn(String isbn) {
        try (Connection conn = getConnection()) {
            String query = """
                    SELECT b.isbn, b.title, b.description, b.author_id,
                           a.id, a.firstName, a.lastName, a.birthDate
                    FROM Book b
                    LEFT JOIN Author a ON b.author_id = a.id
                    WHERE b.isbn = ?
                    """;

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, isbn);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(buildEntityWithAuthor(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find book by ISBN: " + isbn, e);
        }
    }

    public void save(Book book) {
        executeUpdate(
            "INSERT INTO Book (isbn, title, description, author_id) VALUES (?, ?, ?, ?)",
            stmt -> {
                stmt.setString(1, book.getIsbn());
                stmt.setString(2, book.getTitle());
                stmt.setString(3, book.getDescription());
                stmt.setInt(4, book.getAuthorId());
            },
            "Failed to save book"
        );
    }

    public void update(String isbn, Book book) {
        executeUpdate(
            "UPDATE Book SET title = ?, description = ?, author_id = ? WHERE isbn = ?",
            stmt -> {
                stmt.setString(1, book.getTitle());
                stmt.setString(2, book.getDescription());
                stmt.setInt(3, book.getAuthorId());
                stmt.setString(4, isbn);
            },
            "Failed to update book"
        );
    }

    public void delete(String isbn) {
        executeUpdate(
            "DELETE FROM Book WHERE isbn = ?",
            stmt -> stmt.setString(1, isbn),
            "Failed to delete book"
        );
    }

    private Book buildEntity(ResultSet rs) throws SQLException {
        return Book.builder()
                .isbn(rs.getString("isbn"))
                .title(rs.getString("title"))
                .description(rs.getString("description"))
                .authorId(rs.getInt("author_id"))
                .build();
    }

    private Book buildEntityWithAuthor(ResultSet rs) throws SQLException {
        Author author = Author.builder()
                .id(rs.getInt("id"))
                .firstName(rs.getString("firstName"))
                .lastName(rs.getString("lastName"))
                .birthDate(rs.getDate("birthDate").toLocalDate())
                .build();

        Book book = buildEntity(rs);
        book.setAuthor(author);
        return book;
    }

    private void executeUpdate(String query, StatementSetter setter, String errorMessage) {
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(query);
            setter.setParams(stmt);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException(errorMessage);
            }
        } catch (SQLException e) {
            throw new RuntimeException(errorMessage, e);
        }
    }

    private String buildFindAllQuery(BookQuery query) {
        StringBuilder sql = new StringBuilder("SELECT * FROM Book WHERE 1 = 1");

        if (query.isbn() != null && !query.isbn().isBlank()) {
            sql.append(" AND isbn LIKE ?");
        }
        if (query.title() != null && !query.title().isBlank()) {
            sql.append(" AND title LIKE ?");
        }
        if (query.authorId() != null) {
            sql.append(" AND author_id = ?");
        }

        sql.append(" ORDER BY isbn LIMIT ? OFFSET ?");
        return sql.toString();
    }

    private List<Object> buildFindAllParams(BookQuery query) {
        List<Object> params = new ArrayList<>();

        if (query.isbn() != null && !query.isbn().isBlank()) {
            params.add("%" + query.isbn() + "%");
        }
        if (query.title() != null && !query.title().isBlank()) {
            params.add("%" + query.title() + "%");
        }
        if (query.authorId() != null) {
            params.add(query.authorId());
        }

        params.add(query.size());
        params.add(query.page() * query.size());
        return params;
    }

    private void setStatementParams(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            stmt.setObject(i + 1, params.get(i));
        }
    }
}
