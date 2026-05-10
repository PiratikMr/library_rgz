package example.org.library.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Модель книги для личной библиотеки.
 * Реализует Serializable для сохранения в файл.
 */
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private String author;
    private Genre genre;
    private int year;
    private BookStatus status;
    private String borrower; // кому выдана (null если в библиотеке)
    private String notes;    // примечания

    public Book() {
        this.id = UUID.randomUUID().toString();
        this.status = BookStatus.IN_LIBRARY;
    }

    public Book(String title, String author, Genre genre, int year) {
        this();
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
    }

    // --- Геттеры и сеттеры ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void setStatus(BookStatus status) {
        this.status = status;
    }

    public String getBorrower() {
        return borrower;
    }

    public void setBorrower(String borrower) {
        this.borrower = borrower;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return title + " — " + author + " (" + year + ")";
    }
}
