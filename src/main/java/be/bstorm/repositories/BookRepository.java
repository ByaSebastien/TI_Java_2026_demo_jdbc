package be.bstorm.repositories;

import be.bstorm.entities.Book;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static be.bstorm.utils.ConnectionUtils.getConnection;

public class BookRepository {


    public List<Book> findAll() {

        try(Connection conn = getConnection()){

            String query = "SELECT * FROM Book";

            PreparedStatement preparedStatement = conn.prepareStatement(query);

            ResultSet rs = preparedStatement.executeQuery();

            List<Book> books = new ArrayList<>();

            while(rs.next()){
                books.add(buildEntity(rs));
            }

            return books;

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void save(Book book) {

        try(Connection conn = getConnection()) {

            String query = """
                    INSERT INTO Book (isbn, title, description, author_id)
                    VALUES (?, ?, ?, ?)
                    """;

            PreparedStatement preparedStatement = conn.prepareStatement(query);

            preparedStatement.setString(1, book.getIsbn());
            preparedStatement.setString(2, book.getTitle());
            preparedStatement.setString(3, book.getDescription());
            preparedStatement.setInt(4, book.getAuthorId());

            int rows = preparedStatement.executeUpdate();

            if(rows == 0){
                throw new RuntimeException("Failed to save book");
            }

        }catch (SQLException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private Book buildEntity(ResultSet rs) throws SQLException {
        String isbn = rs.getString("isbn");
        String title = rs.getString("title");
        String description = rs.getString("description");
        Integer authorId = rs.getInt("author_id");

        return Book.builder()
                .isbn(isbn)
                .title(title)
                .description(description)
                .authorId(authorId)
                .build();
    }
}
