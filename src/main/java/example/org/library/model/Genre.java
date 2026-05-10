package example.org.library.model;

import java.io.Serializable;

public enum Genre implements Serializable {
    FICTION("Художественная литература"),
    SCIENCE("Научная литература"),
    FANTASY("Фэнтези"),
    DETECTIVE("Детектив"),
    ROMANCE("Романтика"),
    HISTORY("Историческая литература"),
    BIOGRAPHY("Биография"),
    POETRY("Поэзия"),
    ADVENTURE("Приключения"),
    HORROR("Ужасы"),
    PHILOSOPHY("Философия"),
    OTHER("Другое");

    private final String displayName;

    Genre(String displayName) {
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
