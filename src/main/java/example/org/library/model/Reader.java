package example.org.library.model;

/**
 * Читатель библиотеки. Наследник Person.
 * Добавляет контактную информацию: телефон и email.
 */
public class Reader extends Person {

    private static final long serialVersionUID = 1L;

    private String phone;
    private String email;

    public Reader() {
        super();
    }

    public Reader(String firstName, String lastName, String phone, String email) {
        super(firstName, lastName);
        this.phone = phone;
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
