package example.org.library.model;

import java.io.Serializable;
import java.util.UUID;

/**
 * Абстрактный базовый класс для представления человека.
 * Демонстрирует наследование — от него наследуются Author и Reader.
 */
public abstract class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String firstName;
    private String lastName;

    public Person() {
        this.id = UUID.randomUUID().toString();
    }

    public Person(String firstName, String lastName) {
        this();
        this.firstName = firstName;
        this.lastName = lastName;
    }

    /**
     * Возвращает полное имя (имя + фамилия).
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    // --- Геттеры и сеттеры ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
