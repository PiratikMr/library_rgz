package example.org.library.controller;

import example.org.library.model.Reader;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

/**
 * Контроллер диалога добавления/редактирования читателя.
 */
public class ReaderDialogController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Label errorLabel;

    private Reader existingReader;

    @FXML
    public void initialize() {
        firstNameField.textProperty().addListener((obs, o, n) -> clearError());
        lastNameField.textProperty().addListener((obs, o, n) -> clearError());
    }

    /**
     * Заполняет поля данными существующего читателя.
     */
    public void setReader(Reader reader) {
        this.existingReader = reader;
        firstNameField.setText(reader.getFirstName());
        lastNameField.setText(reader.getLastName());
        if (reader.getPhone() != null) phoneField.setText(reader.getPhone());
        if (reader.getEmail() != null) emailField.setText(reader.getEmail());
    }

    public boolean isValid() {
        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            showError("Введите имя");
            firstNameField.requestFocus();
            return false;
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            showError("Введите фамилию");
            lastNameField.requestFocus();
            return false;
        }
        return true;
    }

    public Reader getReader() {
        Reader reader = new Reader();
        if (existingReader != null) {
            reader.setId(existingReader.getId());
        }
        reader.setFirstName(firstNameField.getText().trim());
        reader.setLastName(lastNameField.getText().trim());

        String phone = phoneField.getText();
        reader.setPhone(phone != null && !phone.trim().isEmpty() ? phone.trim() : null);

        String email = emailField.getText();
        reader.setEmail(email != null && !email.trim().isEmpty() ? email.trim() : null);

        return reader;
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
