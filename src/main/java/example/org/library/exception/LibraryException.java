package example.org.library.exception;

/**
 * Базовый класс исключений приложения «Личная Библиотека».
 * Все пользовательские исключения наследуются от него.
 */
public class LibraryException extends RuntimeException {

    public LibraryException(String message) {
        super(message);
    }

    public LibraryException(String message, Throwable cause) {
        super(message, cause);
    }
}
