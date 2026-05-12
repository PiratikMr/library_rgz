package example.org.library.search;

import example.org.library.model.LibraryItem;

/**
 * Стратегия поиска по году издания.
 */
public class YearSearchStrategy implements SearchStrategy {

    @Override
    public boolean matches(LibraryItem item, String query) {
        if (query == null || query.isEmpty()) return true;
        return String.valueOf(item.getYear()).contains(query);
    }

    @Override
    public String getDisplayName() {
        return "По году";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
