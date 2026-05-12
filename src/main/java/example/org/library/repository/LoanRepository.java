package example.org.library.repository;

import example.org.library.model.Loan;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Репозиторий для записей выдачи книг.
 * Сериализует данные в файл loans.dat.
 * Содержит дополнительные методы поиска по элементу и читателю.
 */
public class LoanRepository extends AbstractFileRepository<Loan> {

    public LoanRepository() {
        super("loans.dat", Loan::getId);
    }

    /**
     * Найти активную выдачу по ID элемента (книга ещё не возвращена).
     */
    public List<Loan> findActiveByItemId(String itemId) {
        return findAll().stream()
                .filter(loan -> loan.getItemId().equals(itemId) && loan.isActive())
                .collect(Collectors.toList());
    }

    /**
     * Найти все выдачи конкретного читателя.
     */
    public List<Loan> findByReaderId(String readerId) {
        return findAll().stream()
                .filter(loan -> loan.getReaderId().equals(readerId))
                .collect(Collectors.toList());
    }

    /**
     * Найти все активные выдачи читателя (невозвращённые).
     */
    public List<Loan> findActiveByReaderId(String readerId) {
        return findAll().stream()
                .filter(loan -> loan.getReaderId().equals(readerId) && loan.isActive())
                .collect(Collectors.toList());
    }
}
