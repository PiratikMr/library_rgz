package example.org.library.controller;

import example.org.library.model.Book;
import example.org.library.model.BookStatus;
import example.org.library.model.Genre;
import example.org.library.service.DataService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * Контроллер главного окна приложения.
 * Управляет таблицей книг, фильтрацией и операциями CRUD.
 */
public class MainController {

    // --- FXML-элементы ---
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> genreFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private Label bookCountLabel;

    @FXML
    private TableView<Book> bookTable;
    @FXML
    private TableColumn<Book, String> titleColumn;
    @FXML
    private TableColumn<Book, String> authorColumn;
    @FXML
    private TableColumn<Book, String> genreColumn;
    @FXML
    private TableColumn<Book, String> yearColumn;
    @FXML
    private TableColumn<Book, String> statusColumn;
    @FXML
    private TableColumn<Book, String> borrowerColumn;

    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;

    // --- Данные ---
    private final ObservableList<Book> books = FXCollections.observableArrayList();
    private FilteredList<Book> filteredBooks;
    private final DataService dataService = new DataService();

    /**
     * Инициализация контроллера. Вызывается автоматически после загрузки FXML.
     */
    @FXML
    public void initialize() {
        // Настройка колонок таблицы
        setupTableColumns();

        // Загрузка данных из файла
        List<Book> loaded = dataService.load();
        books.addAll(loaded);

        // Настройка фильтрации
        setupFilters();

        // Настройка кнопок
        setupButtons();

        // Двойной клик для редактирования
        bookTable.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Book selected = bookTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    editBook(selected);
                }
            }
        });

        // Placeholder для пустой таблицы
        bookTable.setPlaceholder(new Label("Библиотека пуста. Нажмите «Добавить» для добавления книги."));

        // Обновить счётчик
        updateBookCount();
    }

    /**
     * Настройка горячих клавиш. Вызывается из LibraryApp после создания Scene.
     */
    public void setupHotkeys(Scene scene) {
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                this::onAddBook);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DELETE),
                this::onDeleteBook);
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ENTER),
                () -> {
                    // Только если фокус не на поле поиска
                    if (!searchField.isFocused()) {
                        Book selected = bookTable.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            editBook(selected);
                        }
                    }
                });
    }

    private void setupTableColumns() {
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        genreColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenre().getDisplayName()));
        yearColumn.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getYear())));
        statusColumn
                .setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus().getDisplayName()));
        borrowerColumn.setCellValueFactory(data -> {
            String borrower = data.getValue().getBorrower();
            return new SimpleStringProperty(borrower != null ? borrower : "—");
        });

        // Цветовая индикация статуса в строках таблицы
        bookTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Book book, boolean empty) {
                super.updateItem(book, empty);
                if (empty || book == null) {
                    setStyle("");
                    getStyleClass().removeAll("row-in-library", "row-lent-out");
                } else {
                    getStyleClass().removeAll("row-in-library", "row-lent-out");
                    if (book.getStatus() == BookStatus.LENT_OUT) {
                        getStyleClass().add("row-lent-out");
                    } else {
                        getStyleClass().add("row-in-library");
                    }
                }
            }
        });
    }

    private void setupFilters() {
        // Заполнение ComboBox жанров
        genreFilter.getItems().add("Все жанры");
        for (Genre g : Genre.values()) {
            genreFilter.getItems().add(g.getDisplayName());
        }
        genreFilter.setValue("Все жанры");

        // Заполнение ComboBox статусов
        statusFilter.getItems().add("Все статусы");
        for (BookStatus s : BookStatus.values()) {
            statusFilter.getItems().add(s.getDisplayName());
        }
        statusFilter.setValue("Все статусы");

        // FilteredList
        filteredBooks = new FilteredList<>(books, p -> true);

        // Обновление фильтра при изменении любого из критериев
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        genreFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // SortedList для сортировки по клику на заголовок колонки
        SortedList<Book> sortedBooks = new SortedList<>(filteredBooks);
        sortedBooks.comparatorProperty().bind(bookTable.comparatorProperty());

        bookTable.setItems(sortedBooks);
    }

    private void applyFilters() {
        String searchText = searchField.getText();
        String selectedGenre = genreFilter.getValue();
        String selectedStatus = statusFilter.getValue();

        filteredBooks.setPredicate(book -> {
            // Фильтр по тексту поиска (название или автор)
            if (searchText != null && !searchText.isEmpty()) {
                String lower = searchText.toLowerCase();
                boolean matchTitle = book.getTitle().toLowerCase().contains(lower);
                boolean matchAuthor = book.getAuthor().toLowerCase().contains(lower);
                if (!matchTitle && !matchAuthor) {
                    return false;
                }
            }

            // Фильтр по жанру
            if (selectedGenre != null && !"Все жанры".equals(selectedGenre)) {
                if (!book.getGenre().getDisplayName().equals(selectedGenre)) {
                    return false;
                }
            }

            // Фильтр по статусу
            if (selectedStatus != null && !"Все статусы".equals(selectedStatus)) {
                if (!book.getStatus().getDisplayName().equals(selectedStatus)) {
                    return false;
                }
            }

            return true;
        });

        updateBookCount();
    }

    private void setupButtons() {
        editButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull());
        deleteButton.disableProperty().bind(
                bookTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void updateBookCount() {
        int total = books.size();
        int shown = filteredBooks.size();
        if (total == shown) {
            bookCountLabel.setText(total + " " + pluralBooks(total));
        } else {
            bookCountLabel.setText(shown + " из " + total);
        }
    }

    private String pluralBooks(int n) {
        int mod10 = n % 10;
        int mod100 = n % 100;
        if (mod10 == 1 && mod100 != 11)
            return "книга";
        if (mod10 >= 2 && mod10 <= 4 && (mod100 < 12 || mod100 > 14))
            return "книги";
        return "книг";
    }

    // --- Обработчики кнопок ---

    @FXML
    private void onAddBook() {
        Book newBook = openBookDialog(null);
        if (newBook != null) {
            books.add(newBook);
            saveData();
            updateBookCount();
        }
    }

    @FXML
    private void onEditBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            editBook(selected);
        }
    }

    private void editBook(Book book) {
        Book edited = openBookDialog(book);
        if (edited != null) {
            // Обновляем поля существующей книги
            book.setTitle(edited.getTitle());
            book.setAuthor(edited.getAuthor());
            book.setGenre(edited.getGenre());
            book.setYear(edited.getYear());
            book.setStatus(edited.getStatus());
            book.setBorrower(edited.getBorrower());
            book.setNotes(edited.getNotes());

            // Обновляем отображение таблицы
            int idx = books.indexOf(book);
            if (idx >= 0) {
                books.set(idx, book);
            }
            saveData();
        }
    }

    @FXML
    private void onDeleteBook() {
        Book selected = bookTable.getSelectionModel().getSelectedItem();
        if (selected == null)
            return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText("Удалить книгу?");
        alert.setContentText("«" + selected.getTitle() + "» — " + selected.getAuthor());

        // Стилизуем кнопки диалога
        ButtonType yesButton = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType noButton = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        // Применяем CSS к диалогу
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/example/org/library/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            books.remove(selected);
            saveData();
            updateBookCount();
        }
    }

    @FXML
    private void onClearFilters() {
        searchField.clear();
        genreFilter.setValue("Все жанры");
        statusFilter.setValue("Все статусы");
    }

    /**
     * Открывает диалог добавления/редактирования книги.
     * 
     * @param book книга для редактирования (null для новой)
     * @return отредактированная/новая книга или null при отмене
     */
    private Book openBookDialog(Book book) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/example/org/library/view/book-dialog.fxml"));
            DialogPane dialogPane = loader.load();

            BookDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(book == null ? "Добавление книги" : "Редактирование книги");

            if (book != null) {
                controller.setBook(book);
            }

            // Стилизация
            dialogPane.getStylesheets().add(
                    getClass().getResource("/example/org/library/style.css").toExternalForm());

            // Валидация при нажатии "Сохранить"
            Button saveBtn = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst().orElse(null));

            if (saveBtn != null) {
                saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.isValid()) {
                        event.consume(); // Предотвращаем закрытие при невалидных данных
                    }
                });
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return controller.getBook();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Ошибка");
            errorAlert.setHeaderText("Не удалось открыть диалог");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
        return null;
    }

    private void saveData() {
        dataService.save(books);
    }
}
