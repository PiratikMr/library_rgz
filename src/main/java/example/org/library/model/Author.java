package example.org.library.model;

/**
 * Автор книги. Наследник Person.
 * Добавляет поле country (страна автора).
 */
public class Author extends Person {

    private static final long serialVersionUID = 1L;

    private String country;

    public Author() {
        super();
    }

    public Author(String firstName, String lastName, String country) {
        super(firstName, lastName);
        this.country = country;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString() {
        if (country != null && !country.isEmpty()) {
            return getFullName() + " (" + country + ")";
        }
        return getFullName();
    }
}
