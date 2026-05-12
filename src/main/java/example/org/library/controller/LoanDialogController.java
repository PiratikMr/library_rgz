package example.org.library.controller;

import example.org.library.model.LibraryItem;
import example.org.library.model.Reader;

import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Контроллер диалога выдачи книги.
 */
public class LoanDialogController {

    @FXML private ComboBox<LibraryItem> itemBox;
    @FXML private ComboBox<Reader> readerBox;
    @FXML private DatePicker dueDatePicker;
    @FXML private Label errorLabel;

    @FXML
    public void initialize() {
        // Устанавливаем дату возврата по умолчанию (через 14 дней)
        dueDatePicker.setValue(LocalDate.now().plusDays(14));

        // Формат даты
        dueDatePicker.setConverter(new StringConverter<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");

            @Override
            public String toString(LocalDate date) {
                return date != null ? fmt.format(date) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, fmt) : null;
            }
        });

        itemBox.valueProperty().addListener((obs, o, n) -> clearError());
        readerBox.valueProperty().addListener((obs, o, n) -> clearError());
    }

    /**
     * Устанавливает доступные книги и читателей.
     */
    public void setData(List<LibraryItem> availableItems, List<Reader> readers) {
        itemBox.getItems().setAll(availableItems);
        readerBox.getItems().setAll(readers);
    }

    public boolean isValid() {
        if (itemBox.getValue() == null) {
            showError("Выберите книгу");
            return false;
        }
        if (readerBox.getValue() == null) {
            showError("Выберите читателя");
            return false;
        }
        if (dueDatePicker.getValue() == null) {
            showError("Укажите дату возврата");
            return false;
        }
        if (dueDatePicker.getValue().isBefore(LocalDate.now())) {
            showError("Дата возврата не может быть в прошлом");
            return false;
        }
        return true;
    }

    public String getSelectedItemId() {
        return itemBox.getValue().getId();
    }

    public String getSelectedReaderId() {
        return readerBox.getValue().getId();
    }

    public LocalDate getDueDate() {
        return dueDatePicker.getValue();
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
