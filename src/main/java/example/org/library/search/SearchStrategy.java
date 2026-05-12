package example.org.library.search;

import example.org.library.model.LibraryItem;

/**
 * Интерфейс стратегии поиска (паттерн Strategy).
 * Позволяет подменять алгоритм поиска без изменения клиентского кода.
 */
public interface SearchStrategy {

    /**
     * Проверяет, соответствует ли элемент поисковому запросу.
     *
     * @param item  элемент библиотеки
     * @param query поисковый запрос
     * @return true, если элемент подходит
     */
    boolean matches(LibraryItem item, String query);

    /**
     * Возвращает отображаемое имя стратегии (для ComboBox).
     */
    String getDisplayName();
}
