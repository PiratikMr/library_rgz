package example.org.library.search;

import example.org.library.model.LibraryItem;

/**
 * Стратегия поиска по жанру.
 */
public class GenreSearchStrategy implements SearchStrategy {

    @Override
    public boolean matches(LibraryItem item, String query) {
        if (query == null || query.isEmpty()) return true;
        return item.getGenre().getDisplayName().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String getDisplayName() {
        return "По жанру";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
