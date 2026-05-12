package example.org.library.model;

/**
 * Печатная книга. Наследник LibraryItem.
 * Добавляет поле pageCount (количество страниц).
 */
public class PrintedBook extends LibraryItem {

    private static final long serialVersionUID = 1L;

    private int pageCount;

    public PrintedBook() {
        super();
    }

    public PrintedBook(String title, Author author, Genre genre, int year, int pageCount) {
        super(title, author, genre, year);
        this.pageCount = pageCount;
    }

    @Override
    public String getItemType() {
        return "Печатная книга";
    }

    @Override
    public String getDetails() {
        return pageCount > 0 ? pageCount + " стр." : "—";
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
