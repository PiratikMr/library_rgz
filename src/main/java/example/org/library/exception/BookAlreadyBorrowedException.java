package example.org.library.exception;

/**
 * Выбрасывается при попытке выдать книгу, которая уже на руках у другого читателя.
 */
public class BookAlreadyBorrowedException extends LibraryException {

    public BookAlreadyBorrowedException(String bookTitle) {
        super("Книга «" + bookTitle + "» уже выдана другому читателю");
    }
}
