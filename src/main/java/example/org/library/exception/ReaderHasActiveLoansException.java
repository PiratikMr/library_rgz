package example.org.library.exception;

/**
 * Выбрасывается при попытке удалить читателя, у которого есть невозвращённые книги.
 */
public class ReaderHasActiveLoansException extends LibraryException {

    public ReaderHasActiveLoansException(String readerName) {
        super("Нельзя удалить читателя «" + readerName + "»: у него есть невозвращённые книги");
    }
}
