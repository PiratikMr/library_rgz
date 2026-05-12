package example.org.library.exception;

/**
 * Выбрасывается при невалидных входных данных (пустое название, некорректный год и т.д.).
 */
public class ValidationException extends LibraryException {

    public ValidationException(String message) {
        super(message);
    }
}
