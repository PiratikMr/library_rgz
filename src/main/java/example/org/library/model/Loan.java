package example.org.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Запись о выдаче книги читателю.
 * Хранит ссылки на элемент и читателя по ID для правильной сериализации.
 */
public class Loan implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Срок выдачи по умолчанию (дни). */
    public static final int DEFAULT_LOAN_DAYS = 14;

    private String id;
    private String itemId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate; // null, пока книга не возвращена

    public Loan() {
        this.id = UUID.randomUUID().toString();
    }

    public Loan(String itemId, String readerId, LocalDate borrowDate, LocalDate dueDate) {
        this();
        this.itemId = itemId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
    }

    /**
     * Проверяет, просрочена ли выдача.
     */
    public boolean isOverdue() {
        if (returnDate != null) {
            return returnDate.isAfter(dueDate);
        }
        return isActive() && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Проверяет, активна ли выдача (книга ещё не возвращена).
     */
    public boolean isActive() {
        return returnDate == null;
    }

    /**
     * Возвращает текстовый статус выдачи.
     */
    public String getStatusText() {
        if (returnDate != null) {
            return "Возвращена";
        }
        if (isOverdue()) {
            return "Просрочена";
        }
        return "Активна";
    }

    // --- Геттеры и сеттеры ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getReaderId() {
        return readerId;
    }

    public void setReaderId(String readerId) {
        this.readerId = readerId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
}
