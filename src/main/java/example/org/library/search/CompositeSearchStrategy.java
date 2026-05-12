package example.org.library.search;

import example.org.library.model.LibraryItem;

import java.util.Arrays;
import java.util.List;

/**
 * Составная стратегия поиска — комбинирует несколько стратегий.
 * Элемент считается подходящим, если хотя бы одна стратегия возвращает true (логическое ИЛИ).
 */
public class CompositeSearchStrategy implements SearchStrategy {

    private final List<SearchStrategy> strategies;

    public CompositeSearchStrategy(SearchStrategy... strategies) {
        this.strategies = Arrays.asList(strategies);
    }

    @Override
    public boolean matches(LibraryItem item, String query) {
        if (query == null || query.isEmpty()) return true;
        return strategies.stream().anyMatch(s -> s.matches(item, query));
    }

    @Override
    public String getDisplayName() {
        return "Везде";
    }

    @Override
    public String toString() {
        return getDisplayName();
    }
}
