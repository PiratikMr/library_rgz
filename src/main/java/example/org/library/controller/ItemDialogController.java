package example.org.library.controller;

import example.org.library.model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.Year;

/**
 * Контроллер диалога добавления/редактирования элемента библиотеки.
 * Поддерживает переключение между типами (Печатная книга / Аудиокнига).
 */
public class ItemDialogController {

    @FXML private ComboBox<String> typeBox;
    @FXML private TextField titleField;
    @FXML private TextField authorFirstNameField;
    @FXML private TextField authorLastNameField;
    @FXML private TextField authorCountryField;
    @FXML private ComboBox<Genre> genreBox;
    @FXML private TextField yearField;

    // Поля для печатной книги
    @FXML private HBox printedBookBox;
    @FXML private TextField pageCountField;

    // Поля для аудиокниги
    @FXML private HBox audioBookBox;
    @FXML private TextField durationField;
    @FXML private TextField narratorField;

    @FXML private TextArea notesArea;
    @FXML private Label errorLabel;

    private LibraryItem existingItem;

    @FXML
    public void initialize() {
        // Типы элементов
        typeBox.getItems().addAll("Печатная книга", "Аудиокнига");
        typeBox.setValue("Печатная книга");

        // Жанры
        genreBox.getItems().addAll(Genre.values());
        genreBox.setValue(Genre.FICTION);

        // Показ/скрытие полей в зависимости от типа
        updateTypeFields("Печатная книга");
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> updateTypeFields(newVal));

        // Ограничение — только цифры
        yearField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) yearField.setText(n.replaceAll("[^\\d]", ""));
        });
        pageCountField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) pageCountField.setText(n.replaceAll("[^\\d]", ""));
        });
        durationField.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("\\d*")) durationField.setText(n.replaceAll("[^\\d]", ""));
        });

        // Убираем ошибку при редактировании
        titleField.textProperty().addListener((obs, o, n) -> clearError());
        authorFirstNameField.textProperty().addListener((obs, o, n) -> clearError());
        authorLastNameField.textProperty().addListener((obs, o, n) -> clearError());
        yearField.textProperty().addListener((obs, o, n) -> clearError());
    }

    private void updateTypeFields(String type) {
        boolean isPrinted = "Печатная книга".equals(type);
        printedBookBox.setVisible(isPrinted);
        printedBookBox.setManaged(isPrinted);
        audioBookBox.setVisible(!isPrinted);
        audioBookBox.setManaged(!isPrinted);
    }

    /**
     * Заполняет поля данными существующего элемента.
     */
    public void setItem(LibraryItem item) {
        this.existingItem = item;
        titleField.setText(item.getTitle());

        if (item.getAuthor() != null) {
            authorFirstNameField.setText(item.getAuthor().getFirstName());
            authorLastNameField.setText(item.getAuthor().getLastName());
            authorCountryField.setText(item.getAuthor().getCountry());
        }

        genreBox.setValue(item.getGenre());
        yearField.setText(String.valueOf(item.getYear()));

        if (item.getNotes() != null) {
            notesArea.setText(item.getNotes());
        }

        if (item instanceof PrintedBook) {
            typeBox.setValue("Печатная книга");
            PrintedBook pb = (PrintedBook) item;
            if (pb.getPageCount() > 0) {
                pageCountField.setText(String.valueOf(pb.getPageCount()));
            }
        } else if (item instanceof AudioBook) {
            typeBox.setValue("Аудиокнига");
            AudioBook ab = (AudioBook) item;
            if (ab.getDurationMinutes() > 0) {
                durationField.setText(String.valueOf(ab.getDurationMinutes()));
            }
            if (ab.getNarrator() != null) {
                narratorField.setText(ab.getNarrator());
            }
        }

        // Запрещаем менять тип при редактировании
        typeBox.setDisable(true);
    }

    /**
     * Валидация введённых данных.
     */
    public boolean isValid() {
        if (titleField.getText() == null || titleField.getText().trim().isEmpty()) {
            showError("Введите название");
            titleField.requestFocus();
            return false;
        }
        if (authorFirstNameField.getText() == null || authorFirstNameField.getText().trim().isEmpty()) {
            showError("Введите имя автора");
            authorFirstNameField.requestFocus();
            return false;
        }
        if (authorLastNameField.getText() == null || authorLastNameField.getText().trim().isEmpty()) {
            showError("Введите фамилию автора");
            authorLastNameField.requestFocus();
            return false;
        }

        String yearText = yearField.getText();
        if (yearText == null || yearText.trim().isEmpty()) {
            showError("Введите год издания");
            yearField.requestFocus();
            return false;
        }
        try {
            int year = Integer.parseInt(yearText.trim());
            if (year < 0 || year > Year.now().getValue()) {
                showError("Некорректный год (0 — " + Year.now().getValue() + ")");
                yearField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showError("Год должен быть числом");
            yearField.requestFocus();
            return false;
        }
        return true;
    }

    /**
     * Собирает данные из полей в объект LibraryItem (полиморфно).
     */
    public LibraryItem getItem() {
        Author author = new Author(
                authorFirstNameField.getText().trim(),
                authorLastNameField.getText().trim(),
                authorCountryField.getText() != null ? authorCountryField.getText().trim() : ""
        );

        LibraryItem item;
        if ("Аудиокнига".equals(typeBox.getValue())) {
            AudioBook ab = new AudioBook();
            String durText = durationField.getText();
            ab.setDurationMinutes(durText != null && !durText.isEmpty() ? Integer.parseInt(durText) : 0);
            ab.setNarrator(narratorField.getText() != null ? narratorField.getText().trim() : "");
            item = ab;
        } else {
            PrintedBook pb = new PrintedBook();
            String pgText = pageCountField.getText();
            pb.setPageCount(pgText != null && !pgText.isEmpty() ? Integer.parseInt(pgText) : 0);
            item = pb;
        }

        if (existingItem != null) {
            item.setId(existingItem.getId());
            item.setStatus(existingItem.getStatus());
        }

        item.setTitle(titleField.getText().trim());
        item.setAuthor(author);
        item.setGenre(genreBox.getValue());
        item.setYear(Integer.parseInt(yearField.getText().trim()));

        String notes = notesArea.getText();
        item.setNotes(notes != null && !notes.trim().isEmpty() ? notes.trim() : null);

        return item;
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
