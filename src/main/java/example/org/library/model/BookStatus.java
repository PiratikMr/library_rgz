package example.org.library.model;

import java.io.Serializable;

/**
 * Статус элемента библиотеки.
 */
public enum BookStatus implements Serializable {
    IN_LIBRARY("В библиотеке"),
    LENT_OUT("На руках"),
    LOST("Утеряна"),
    DAMAGED("Повреждена");

    private final String displayName;

    BookStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
