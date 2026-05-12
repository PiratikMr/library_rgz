package example.org.library.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Абстрактный базовый класс для элементов библиотеки.
 * Демонстрирует абстрактный класс, наследование и полиморфизм.
 * От него наследуются PrintedBook и AudioBook.
 */
public abstract class LibraryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String title;
    private Author author;
    private Genre genre;
    private int year;
    private BookStatus status;
    private String notes;

    public LibraryItem() {
        this.id = UUID.randomUUID().toString();
        this.status = BookStatus.IN_LIBRARY;
    }

    public LibraryItem(String title, Author author, Genre genre, int year) {
        this();
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.year = year;
    }

    /**
     * Возвращает тип элемента библиотеки (полиморфный метод).
     * Каждый наследник возвращает своё значение.
     */
    public abstract String getItemType();

    /**
     * Возвращает специфические детали элемента (полиморфный метод).
     * Для PrintedBook — количество страниц, для AudioBook — длительность и чтеца.
     */
    public abstract String getDetails();

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

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Возвращает имя автора или "Неизвестный" если автор не указан.
     */
    public String getAuthorName() {
        return author != null ? author.getFullName() : "Неизвестный";
    }

    @Override
    public String toString() {
        return title + " — " + getAuthorName() + " (" + year + ")";
    }
}
