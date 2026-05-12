package example.org.library.controller;

import example.org.library.exception.LibraryException;
import example.org.library.model.*;
import example.org.library.search.*;
import example.org.library.service.LibraryService;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.stage.Modality;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Контроллер главного окна приложения.
 * Управляет тремя вкладками: Каталог, Читатели, Выдачи.
 */
public class MainController {

    // --- Общие ---
    @FXML private TabPane mainTabPane;
    @FXML private Label overdueLabel;

    // --- Каталог ---
    @FXML private ComboBox<SearchStrategy> searchStrategyBox;
    @FXML private TextField catalogSearchField;
    @FXML private ComboBox<String> genreFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private TableView<LibraryItem> catalogTable;
    @FXML private TableColumn<LibraryItem, String> itemTypeColumn;
    @FXML private TableColumn<LibraryItem, String> itemTitleColumn;
    @FXML private TableColumn<LibraryItem, String> itemAuthorColumn;
    @FXML private TableColumn<LibraryItem, String> itemGenreColumn;
    @FXML private TableColumn<LibraryItem, String> itemYearColumn;
    @FXML private TableColumn<LibraryItem, String> itemDetailsColumn;
    @FXML private TableColumn<LibraryItem, String> itemStatusColumn;
    @FXML private Button addItemButton;
    @FXML private Button editItemButton;
    @FXML private Button deleteItemButton;
    @FXML private Label catalogCountLabel;

    // --- Читатели ---
    @FXML private TextField readerSearchField;
    @FXML private TableView<Reader> readerTable;
    @FXML private TableColumn<Reader, String> readerNameColumn;
    @FXML private TableColumn<Reader, String> readerPhoneColumn;
    @FXML private TableColumn<Reader, String> readerEmailColumn;
    @FXML private TableColumn<Reader, String> readerLoansColumn;
    @FXML private Button addReaderButton;
    @FXML private Button editReaderButton;
    @FXML private Button deleteReaderButton;
    @FXML private Label readerCountLabel;

    // --- Выдачи ---
    @FXML private ComboBox<String> loanStatusFilter;
    @FXML private TableView<Loan> loanTable;
    @FXML private TableColumn<Loan, String> loanBookColumn;
    @FXML private TableColumn<Loan, String> loanReaderColumn;
    @FXML private TableColumn<Loan, String> loanBorrowDateColumn;
    @FXML private TableColumn<Loan, String> loanDueDateColumn;
    @FXML private TableColumn<Loan, String> loanReturnDateColumn;
    @FXML private TableColumn<Loan, String> loanStatusColumn;
    @FXML private Button issueLoanButton;
    @FXML private Button returnLoanButton;
    @FXML private Label loanCountLabel;

    // --- Данные ---
    private final LibraryService libraryService = new LibraryService();
    private final ObservableList<LibraryItem> items = FXCollections.observableArrayList();
    private final ObservableList<Reader> readers = FXCollections.observableArrayList();
    private final ObservableList<Loan> loans = FXCollections.observableArrayList();
    private FilteredList<LibraryItem> filteredItems;
    private FilteredList<Reader> filteredReaders;
    private FilteredList<Loan> filteredLoans;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    public void initialize() {
        setupCatalogTab();
        setupReadersTab();
        setupLoansTab();
        loadAllData();
        updateOverdueBadge();

        // Обновление данных при переключении вкладок
        mainTabPane.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            loadAllData();
            updateOverdueBadge();
        });
    }

    // ==================== КАТАЛОГ ====================

    private void setupCatalogTab() {
        // Стратегии поиска
        SearchStrategy titleStrategy = new TitleSearchStrategy();
        SearchStrategy authorStrategy = new AuthorSearchStrategy();
        SearchStrategy genreStrategy = new GenreSearchStrategy();
        SearchStrategy yearStrategy = new YearSearchStrategy();
        SearchStrategy compositeStrategy = new CompositeSearchStrategy(
                titleStrategy, authorStrategy, genreStrategy, yearStrategy);

        searchStrategyBox.getItems().addAll(
                compositeStrategy, titleStrategy, authorStrategy, genreStrategy, yearStrategy);
        searchStrategyBox.setValue(compositeStrategy);

        // Фильтры
        genreFilter.getItems().add("Все жанры");
        for (Genre g : Genre.values()) genreFilter.getItems().add(g.getDisplayName());
        genreFilter.setValue("Все жанры");

        statusFilter.getItems().add("Все статусы");
        for (BookStatus s : BookStatus.values()) statusFilter.getItems().add(s.getDisplayName());
        statusFilter.setValue("Все статусы");

        // Колонки таблицы (полиморфный вызов getItemType и getDetails)
        itemTypeColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getItemType()));
        itemTitleColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTitle()));
        itemAuthorColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getAuthorName()));
        itemGenreColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getGenre().getDisplayName()));
        itemYearColumn.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getYear())));
        itemDetailsColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDetails()));
        itemStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatus().getDisplayName()));

        // Цветовая индикация строк
        catalogTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(LibraryItem item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("row-in-library", "row-lent-out", "row-lost", "row-damaged");
                if (empty || item == null) return;
                switch (item.getStatus()) {
                    case LENT_OUT -> getStyleClass().add("row-lent-out");
                    case LOST -> getStyleClass().add("row-lost");
                    case DAMAGED -> getStyleClass().add("row-damaged");
                    default -> getStyleClass().add("row-in-library");
                }
            }
        });

        // Фильтрация
        filteredItems = new FilteredList<>(items, p -> true);
        catalogSearchField.textProperty().addListener((obs, o, n) -> applyCatalogFilters());
        searchStrategyBox.valueProperty().addListener((obs, o, n) -> applyCatalogFilters());
        genreFilter.valueProperty().addListener((obs, o, n) -> applyCatalogFilters());
        statusFilter.valueProperty().addListener((obs, o, n) -> applyCatalogFilters());

        SortedList<LibraryItem> sortedItems = new SortedList<>(filteredItems);
        sortedItems.comparatorProperty().bind(catalogTable.comparatorProperty());
        catalogTable.setItems(sortedItems);

        // Кнопки
        editItemButton.disableProperty().bind(catalogTable.getSelectionModel().selectedItemProperty().isNull());
        deleteItemButton.disableProperty().bind(catalogTable.getSelectionModel().selectedItemProperty().isNull());

        // Двойной клик для редактирования
        catalogTable.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                LibraryItem sel = catalogTable.getSelectionModel().getSelectedItem();
                if (sel != null) editItem(sel);
            }
        });

        catalogTable.setPlaceholder(new Label("Каталог пуст. Нажмите «+ Добавить» для добавления."));
    }

    private void applyCatalogFilters() {
        String query = catalogSearchField.getText();
        SearchStrategy strategy = searchStrategyBox.getValue();
        String genre = genreFilter.getValue();
        String status = statusFilter.getValue();

        filteredItems.setPredicate(item -> {
            if (query != null && !query.isEmpty() && strategy != null) {
                if (!strategy.matches(item, query)) return false;
            }
            if (genre != null && !"Все жанры".equals(genre)) {
                if (!item.getGenre().getDisplayName().equals(genre)) return false;
            }
            if (status != null && !"Все статусы".equals(status)) {
                if (!item.getStatus().getDisplayName().equals(status)) return false;
            }
            return true;
        });
        updateCatalogCount();
    }

    @FXML
    private void onClearCatalogFilters() {
        catalogSearchField.clear();
        genreFilter.setValue("Все жанры");
        statusFilter.setValue("Все статусы");
    }

    @FXML
    private void onAddItem() {
        LibraryItem newItem = openItemDialog(null);
        if (newItem != null) {
            try {
                libraryService.saveItem(newItem);
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void onEditItem() {
        LibraryItem selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected != null) editItem(selected);
    }

    private void editItem(LibraryItem item) {
        LibraryItem edited = openItemDialog(item);
        if (edited != null) {
            try {
                libraryService.saveItem(edited);
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void onDeleteItem() {
        LibraryItem selected = catalogTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (confirmDelete("Удалить элемент?", "«" + selected.getTitle() + "» — " + selected.getAuthorName())) {
            try {
                libraryService.deleteItem(selected.getId());
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка удаления", e.getMessage());
            }
        }
    }

    private LibraryItem openItemDialog(LibraryItem item) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/example/org/library/view/item-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            ItemDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(item == null ? "Добавление элемента" : "Редактирование элемента");

            if (item != null) controller.setItem(item);

            dialogPane.getStylesheets().add(
                    getClass().getResource("/example/org/library/style.css").toExternalForm());

            Button saveBtn = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst().orElse(null));
            if (saveBtn != null) {
                saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.isValid()) event.consume();
                });
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return controller.getItem();
            }
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
        return null;
    }

    // ==================== ЧИТАТЕЛИ ====================

    private void setupReadersTab() {
        readerNameColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFullName()));
        readerPhoneColumn.setCellValueFactory(d -> {
            String phone = d.getValue().getPhone();
            return new SimpleStringProperty(phone != null ? phone : "—");
        });
        readerEmailColumn.setCellValueFactory(d -> {
            String email = d.getValue().getEmail();
            return new SimpleStringProperty(email != null ? email : "—");
        });
        readerLoansColumn.setCellValueFactory(d -> {
            int count = libraryService.getActiveLoansCount(d.getValue().getId());
            return new SimpleStringProperty(String.valueOf(count));
        });

        filteredReaders = new FilteredList<>(readers, p -> true);
        readerSearchField.textProperty().addListener((obs, o, n) -> applyReaderFilter());

        SortedList<Reader> sortedReaders = new SortedList<>(filteredReaders);
        sortedReaders.comparatorProperty().bind(readerTable.comparatorProperty());
        readerTable.setItems(sortedReaders);

        editReaderButton.disableProperty().bind(readerTable.getSelectionModel().selectedItemProperty().isNull());
        deleteReaderButton.disableProperty().bind(readerTable.getSelectionModel().selectedItemProperty().isNull());

        readerTable.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                Reader sel = readerTable.getSelectionModel().getSelectedItem();
                if (sel != null) editReader(sel);
            }
        });

        readerTable.setPlaceholder(new Label("Нет читателей. Нажмите «+ Добавить читателя»."));
    }

    private void applyReaderFilter() {
        String query = readerSearchField.getText();
        filteredReaders.setPredicate(reader -> {
            if (query == null || query.isEmpty()) return true;
            return reader.getFullName().toLowerCase().contains(query.toLowerCase());
        });
        updateReaderCount();
    }

    @FXML
    private void onAddReader() {
        Reader newReader = openReaderDialog(null);
        if (newReader != null) {
            try {
                libraryService.saveReader(newReader);
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void onEditReader() {
        Reader selected = readerTable.getSelectionModel().getSelectedItem();
        if (selected != null) editReader(selected);
    }

    private void editReader(Reader reader) {
        Reader edited = openReaderDialog(reader);
        if (edited != null) {
            try {
                libraryService.saveReader(edited);
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка", e.getMessage());
            }
        }
    }

    @FXML
    private void onDeleteReader() {
        Reader selected = readerTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        if (confirmDelete("Удалить читателя?", selected.getFullName())) {
            try {
                libraryService.deleteReader(selected.getId());
                loadAllData();
            } catch (LibraryException e) {
                showError("Ошибка удаления", e.getMessage());
            }
        }
    }

    private Reader openReaderDialog(Reader reader) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/example/org/library/view/reader-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            ReaderDialogController controller = loader.getController();

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle(reader == null ? "Добавление читателя" : "Редактирование читателя");

            if (reader != null) controller.setReader(reader);

            dialogPane.getStylesheets().add(
                    getClass().getResource("/example/org/library/style.css").toExternalForm());

            Button saveBtn = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst().orElse(null));
            if (saveBtn != null) {
                saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.isValid()) event.consume();
                });
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                return controller.getReader();
            }
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
        return null;
    }

    // ==================== ВЫДАЧИ ====================

    private void setupLoansTab() {
        loanStatusFilter.getItems().addAll("Все", "Активные", "Просроченные", "Завершённые");
        loanStatusFilter.setValue("Все");

        loanBookColumn.setCellValueFactory(d -> {
            Optional<LibraryItem> item = libraryService.findItemById(d.getValue().getItemId());
            return new SimpleStringProperty(item.map(LibraryItem::getTitle).orElse("Удалена"));
        });
        loanReaderColumn.setCellValueFactory(d -> {
            Optional<Reader> reader = libraryService.findReaderById(d.getValue().getReaderId());
            return new SimpleStringProperty(reader.map(Reader::getFullName).orElse("Удалён"));
        });
        loanBorrowDateColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getBorrowDate() != null ? d.getValue().getBorrowDate().format(DATE_FMT) : "—"));
        loanDueDateColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDueDate() != null ? d.getValue().getDueDate().format(DATE_FMT) : "—"));
        loanReturnDateColumn.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getReturnDate() != null ? d.getValue().getReturnDate().format(DATE_FMT) : "—"));
        loanStatusColumn.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getStatusText()));

        // Подсветка просроченных выдач
        loanTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Loan loan, boolean empty) {
                super.updateItem(loan, empty);
                getStyleClass().removeAll("row-overdue", "row-returned");
                if (empty || loan == null) return;
                if (loan.isOverdue() && loan.isActive()) getStyleClass().add("row-overdue");
                else if (!loan.isActive()) getStyleClass().add("row-returned");
            }
        });

        filteredLoans = new FilteredList<>(loans, p -> true);
        loanStatusFilter.valueProperty().addListener((obs, o, n) -> applyLoanFilter());

        SortedList<Loan> sortedLoans = new SortedList<>(filteredLoans);
        sortedLoans.comparatorProperty().bind(loanTable.comparatorProperty());
        loanTable.setItems(sortedLoans);

        // Кнопка «Вернуть» доступна только для активных (невозвращённых) выдач
        returnLoanButton.disableProperty().bind(
                javafx.beans.binding.Bindings.createBooleanBinding(
                        () -> {
                            Loan sel = loanTable.getSelectionModel().getSelectedItem();
                            return sel == null || !sel.isActive();
                        },
                        loanTable.getSelectionModel().selectedItemProperty()
                )
        );

        loanTable.setPlaceholder(new Label("Нет записей о выдачах."));
    }

    private void applyLoanFilter() {
        String status = loanStatusFilter.getValue();
        filteredLoans.setPredicate(loan -> {
            if (status == null || "Все".equals(status)) return true;
            return switch (status) {
                case "Активные" -> loan.isActive() && !loan.isOverdue();
                case "Просроченные" -> loan.isActive() && loan.isOverdue();
                case "Завершённые" -> !loan.isActive();
                default -> true;
            };
        });
        updateLoanCount();
    }

    @FXML
    private void onIssueLoan() {
        // Только книги со статусом "В библиотеке" доступны для выдачи
        List<LibraryItem> available = libraryService.getAllItems().stream()
                .filter(i -> i.getStatus() == BookStatus.IN_LIBRARY)
                .collect(Collectors.toList());
        List<Reader> allReaders = libraryService.getAllReaders();

        if (available.isEmpty()) {
            showError("Нет доступных книг", "Все книги выданы или недоступны.");
            return;
        }
        if (allReaders.isEmpty()) {
            showError("Нет читателей", "Сначала добавьте хотя бы одного читателя.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/example/org/library/view/loan-dialog.fxml"));
            DialogPane dialogPane = loader.load();
            LoanDialogController controller = loader.getController();
            controller.setData(available, allReaders);

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setDialogPane(dialogPane);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("Выдача книги");

            dialogPane.getStylesheets().add(
                    getClass().getResource("/example/org/library/style.css").toExternalForm());

            Button saveBtn = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst().orElse(null));
            if (saveBtn != null) {
                saveBtn.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (!controller.isValid()) event.consume();
                });
            }

            Optional<ButtonType> result = dialog.showAndWait();
            if (result.isPresent() && result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                try {
                    libraryService.borrowItem(
                            controller.getSelectedItemId(),
                            controller.getSelectedReaderId(),
                            controller.getDueDate());
                    loadAllData();
                    updateOverdueBadge();
                } catch (LibraryException e) {
                    showError("Ошибка выдачи", e.getMessage());
                }
            }
        } catch (IOException e) {
            showError("Ошибка", "Не удалось открыть диалог: " + e.getMessage());
        }
    }

    @FXML
    private void onReturnLoan() {
        Loan selected = loanTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;



        Optional<LibraryItem> item = libraryService.findItemById(selected.getItemId());
        String title = item.map(LibraryItem::getTitle).orElse("Книга");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Возврат книги");
        alert.setHeaderText("Вернуть книгу?");
        alert.setContentText("«" + title + "»");
        styleAlert(alert);

        ButtonType yesBtn = new ButtonType("Вернуть", ButtonBar.ButtonData.OK_DONE);
        ButtonType noBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesBtn) {
            try {
                libraryService.returnItem(selected.getId());
                loadAllData();
                updateOverdueBadge();
            } catch (LibraryException e) {
                showError("Ошибка возврата", e.getMessage());
            }
        }
    }

    // ==================== ОБЩИЕ МЕТОДЫ ====================

    private void loadAllData() {
        items.setAll(libraryService.getAllItems());
        readers.setAll(libraryService.getAllReaders());
        loans.setAll(libraryService.getAllLoans());
        updateCatalogCount();
        updateReaderCount();
        updateLoanCount();
    }

    private void updateCatalogCount() {
        catalogCountLabel.setText(filteredItems.size() + " из " + items.size());
    }

    private void updateReaderCount() {
        readerCountLabel.setText(filteredReaders.size() + " из " + readers.size());
    }

    private void updateLoanCount() {
        loanCountLabel.setText(filteredLoans.size() + " из " + loans.size());
    }

    private void updateOverdueBadge() {
        int overdue = libraryService.getOverdueLoans().size();
        if (overdue > 0) {
            overdueLabel.setText("⚠ Просрочено: " + overdue);
            overdueLabel.setVisible(true);
            overdueLabel.setManaged(true);
        } else {
            overdueLabel.setVisible(false);
            overdueLabel.setManaged(false);
        }
    }

    private boolean confirmDelete(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Подтверждение удаления");
        alert.setHeaderText(header);
        alert.setContentText(content);
        styleAlert(alert);

        ButtonType yesBtn = new ButtonType("Удалить", ButtonBar.ButtonData.OK_DONE);
        ButtonType noBtn = new ButtonType("Отмена", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == yesBtn;
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private void styleAlert(Alert alert) {
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/example/org/library/style.css").toExternalForm());
        alert.getDialogPane().getStyleClass().add("custom-alert");
    }

    /**
     * Настройка горячих клавиш.
     */
    public void setupHotkeys(Scene scene) {
        // Горячие клавиши убраны для простоты — можно добавить позже
    }
}
