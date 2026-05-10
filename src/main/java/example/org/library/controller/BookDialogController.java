package example.org.library.controller;

import example.org.library.model.Book;
import example.org.library.model.BookStatus;
import example.org.library.model.Genre;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.Year;

/**
 * Контроллер диалога добавления/редактирования книги.
 */
public class BookDialogController {

    @FXML private TextField titleField;
    @FXML private TextField authorField;
    @FXML private ComboBox<Genre> genreBox;
    @FXML private TextField yearField;
    @FXML private ComboBox<BookStatus> statusBox;
    @FXML private HBox borrowerBox;
    @FXML private TextField borrowerField;
    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;

    private Book existingBook; // null для новой книги

    @FXML
    public void initialize() {
        // Заполняем ComboBox жанров
        genreBox.getItems().addAll(Genre.values());
        genreBox.setValue(Genre.FICTION);

        // Заполняем ComboBox статусов
        statusBox.getItems().addAll(BookStatus.values());
        statusBox.setValue(BookStatus.IN_LIBRARY);

        // Показываем/скрываем поле "Кому выдана" в зависимости от статуса
        borrowerBox.setVisible(false);
        borrowerBox.setManaged(false);

        statusBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            boolean isLent = newVal == BookStatus.LENT_OUT;
            borrowerBox.setVisible(isLent);
            borrowerBox.setManaged(isLent);
            if (!isLent) {
                borrowerField.clear();
            }
        });

        // Ограничение поля года — только цифры
        yearField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                yearField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        // Убираем ошибку при редактировании полей
        titleField.textProperty().addListener((obs, o, n) -> clearError());
        authorField.textProperty().addListener((obs, o, n) -> clearError());
        yearField.textProperty().addListener((obs, o, n) -> clearError());
        borrowerField.textProperty().addListener((obs, o, n) -> clearError());
    }

    /**
     * Заполняет поля диалога данными существующей книги для редактирования.
     */
    public void setBook(Book book) {
        this.existingBook = book;
        titleField.setText(book.getTitle());
        authorField.setText(book.getAuthor());
        genreBox.setValue(book.getGenre());
        yearField.setText(String.valueOf(book.getYear()));
        statusBox.setValue(book.getStatus());

        if (book.getBorrower() != null && !book.getBorrower().isEmpty()) {
            borrowerField.setText(book.getBorrower());
        }

        if (book.getNotes() != null) {
            notesArea.setText(book.getNotes());
        }
    }

    /**
     * Проверяет валидность введённых данных.
     */
    public boolean isValid() {
        String title = titleField.getText();
        String author = authorField.getText();
        String yearText = yearField.getText();

        if (title == null || title.trim().isEmpty()) {
            showError("Введите название книги");
            titleField.requestFocus();
            return false;
        }

        if (author == null || author.trim().isEmpty()) {
            showError("Введите автора");
            authorField.requestFocus();
            return false;
        }

        if (yearText == null || yearText.trim().isEmpty()) {
            showError("Введите год издания");
            yearField.requestFocus();
            return false;
        }

        try {
            int year = Integer.parseInt(yearText.trim());
            if (year < 0 || year > Year.now().getValue()) {
                showError("Введите корректный год (0 — " + Year.now().getValue() + ")");
                yearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Год должен быть числом");
            yearField.requestFocus();
            return false;
        }

        if (statusBox.getValue() == BookStatus.LENT_OUT) {
            String borrower = borrowerField.getText();
            if (borrower == null || borrower.trim().isEmpty()) {
                showError("Укажите, кому выдана книга");
                borrowerField.requestFocus();
                return false;
            }
        }

        return true;
    }

    /**
     * Собирает данные из полей в объект Book.
     */
    public Book getBook() {
        Book book = (existingBook != null) ? new Book() : new Book();
        if (existingBook != null) {
            book.setId(existingBook.getId());
        }

        book.setTitle(titleField.getText().trim());
        book.setAuthor(authorField.getText().trim());
        book.setGenre(genreBox.getValue());
        book.setYear(Integer.parseInt(yearField.getText().trim()));
        book.setStatus(statusBox.getValue());

        if (statusBox.getValue() == BookStatus.LENT_OUT) {
            book.setBorrower(borrowerField.getText().trim());
        } else {
            book.setBorrower(null);
        }

        String notes = notesArea.getText();
        book.setNotes(notes != null && !notes.trim().isEmpty() ? notes.trim() : null);

        return book;
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
        errorLabel.setManaged(false);
    }
}
