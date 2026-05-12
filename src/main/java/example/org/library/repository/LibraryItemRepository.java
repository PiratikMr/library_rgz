package example.org.library.repository;

import example.org.library.model.LibraryItem;

/**
 * Репозиторий для элементов библиотеки (книги, аудиокниги).
 * Сериализует данные в файл library_items.dat.
 */
public class LibraryItemRepository extends AbstractFileRepository<LibraryItem> {

    public LibraryItemRepository() {
        super("library_items.dat", LibraryItem::getId);
    }
}
