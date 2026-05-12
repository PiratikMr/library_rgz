package example.org.library.service;

import example.org.library.exception.BookAlreadyBorrowedException;
import example.org.library.exception.ReaderHasActiveLoansException;
import example.org.library.exception.ValidationException;
import example.org.library.model.*;
import example.org.library.repository.LoanRepository;
import example.org.library.repository.LibraryItemRepository;
import example.org.library.repository.ReaderRepository;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервисный слой — содержит бизнес-логику приложения.
 * Работает с репозиториями, выполняет валидацию и бросает исключения.
 */
public class LibraryService {

    private final LibraryItemRepository itemRepository;
    private final ReaderRepository readerRepository;
    private final LoanRepository loanRepository;

    public LibraryService() {
        this.itemRepository = new LibraryItemRepository();
        this.readerRepository = new ReaderRepository();
        this.loanRepository = new LoanRepository();
    }

    // ========== Элементы библиотеки ==========

    public List<LibraryItem> getAllItems() {
        return itemRepository.findAll();
    }

    public Optional<LibraryItem> findItemById(String id) {
        return itemRepository.findById(id);
    }

    /**
     * Сохраняет элемент с валидацией.
     * @throws ValidationException если данные невалидны
     */
    public void saveItem(LibraryItem item) {
        validateItem(item);
        itemRepository.save(item);
    }

    public void deleteItem(String id) {
        // Проверяем, нет ли активных выдач для этого элемента
        List<Loan> activeLoans = loanRepository.findActiveByItemId(id);
        if (!activeLoans.isEmpty()) {
            Optional<LibraryItem> item = itemRepository.findById(id);
            String title = item.map(LibraryItem::getTitle).orElse("Неизвестная книга");
            throw new BookAlreadyBorrowedException(title);
        }
        itemRepository.delete(id);
    }

    private void validateItem(LibraryItem item) {
        if (item.getTitle() == null || item.getTitle().trim().isEmpty()) {
            throw new ValidationException("Введите название");
        }
        if (item.getAuthor() == null ||
                item.getAuthor().getFirstName() == null ||
                item.getAuthor().getFirstName().trim().isEmpty()) {
            throw new ValidationException("Введите имя автора");
        }
        if (item.getAuthor().getLastName() == null ||
                item.getAuthor().getLastName().trim().isEmpty()) {
            throw new ValidationException("Введите фамилию автора");
        }
        if (item.getYear() < 0 || item.getYear() > Year.now().getValue()) {
            throw new ValidationException("Некорректный год издания (0 — " + Year.now().getValue() + ")");
        }
        if (item instanceof PrintedBook) {
            PrintedBook pb = (PrintedBook) item;
            if (pb.getPageCount() < 0) {
                throw new ValidationException("Количество страниц не может быть отрицательным");
            }
        }
        if (item instanceof AudioBook) {
            AudioBook ab = (AudioBook) item;
            if (ab.getDurationMinutes() < 0) {
                throw new ValidationException("Длительность не может быть отрицательной");
            }
        }
    }

    // ========== Читатели ==========

    public List<Reader> getAllReaders() {
        return readerRepository.findAll();
    }

    public Optional<Reader> findReaderById(String id) {
        return readerRepository.findById(id);
    }

    /**
     * Сохраняет читателя с валидацией.
     * @throws ValidationException если данные невалидны
     */
    public void saveReader(Reader reader) {
        validateReader(reader);
        readerRepository.save(reader);
    }

    /**
     * Удаляет читателя, если у него нет активных выдач.
     * @throws ReaderHasActiveLoansException если есть невозвращённые книги
     */
    public void deleteReader(String id) {
        List<Loan> activeLoans = loanRepository.findActiveByReaderId(id);
        if (!activeLoans.isEmpty()) {
            Optional<Reader> reader = readerRepository.findById(id);
            String name = reader.map(Reader::getFullName).orElse("Неизвестный");
            throw new ReaderHasActiveLoansException(name);
        }
        readerRepository.delete(id);
    }

    private void validateReader(Reader reader) {
        if (reader.getFirstName() == null || reader.getFirstName().trim().isEmpty()) {
            throw new ValidationException("Введите имя читателя");
        }
        if (reader.getLastName() == null || reader.getLastName().trim().isEmpty()) {
            throw new ValidationException("Введите фамилию читателя");
        }
    }

    // ========== Выдачи ==========

    public List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /**
     * Выдать книгу читателю.
     * @throws BookAlreadyBorrowedException если книга уже на руках
     * @throws ValidationException если элемент или читатель не найден
     */
    public Loan borrowItem(String itemId, String readerId, LocalDate dueDate) {
        LibraryItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ValidationException("Элемент не найден"));
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ValidationException("Читатель не найден"));

        if (item.getStatus() == BookStatus.LENT_OUT) {
            throw new BookAlreadyBorrowedException(item.getTitle());
        }
        if (item.getStatus() == BookStatus.LOST || item.getStatus() == BookStatus.DAMAGED) {
            throw new ValidationException("Нельзя выдать книгу со статусом «" +
                    item.getStatus().getDisplayName() + "»");
        }

        // Создаём запись о выдаче
        Loan loan = new Loan(itemId, readerId, LocalDate.now(), dueDate);
        loanRepository.save(loan);

        // Обновляем статус элемента
        item.setStatus(BookStatus.LENT_OUT);
        itemRepository.save(item);

        return loan;
    }

    /**
     * Вернуть книгу в библиотеку.
     */
    public void returnItem(String loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ValidationException("Запись о выдаче не найдена"));

        loan.setReturnDate(LocalDate.now());
        loanRepository.save(loan);

        // Возвращаем статус элемента
        itemRepository.findById(loan.getItemId()).ifPresent(item -> {
            item.setStatus(BookStatus.IN_LIBRARY);
            itemRepository.save(item);
        });
    }

    /**
     * Получить список просроченных выдач.
     */
    public List<Loan> getOverdueLoans() {
        return loanRepository.findAll().stream()
                .filter(Loan::isOverdue)
                .filter(Loan::isActive)
                .collect(Collectors.toList());
    }

    /**
     * Получить количество активных выдач читателя.
     */
    public int getActiveLoansCount(String readerId) {
        return loanRepository.findActiveByReaderId(readerId).size();
    }
}
