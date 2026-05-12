package example.org.library.search;

import example.org.library.model.LibraryItem;

/**
 * Стратегия поиска по автору.
 */
public class AuthorSearchStrategy implements SearchStrategy {

    @Override
    public boolean matches(LibraryItem item, String query) {
        if (query == null || query.isEmpty()) return true;
        return item.getAuthorName().toLowerCase().contains(query.toLowerCase());
    }

    @Override
    public String getDisplayName() {
        return "По автору";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
