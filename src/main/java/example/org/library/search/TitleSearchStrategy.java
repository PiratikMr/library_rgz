package example.org.library.search;

import example.org.library.model.LibraryItem;

/**
 * Стратегия поиска по названию.
 */
public class TitleSearchStrategy implements SearchStrategy {

    @Override
    public boolean matches(LibraryItem item, String query) {
        if (query == null || query.isEmpty()) return true;
        return item.getTitle().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String getDisplayName() {
        return "По названию";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
