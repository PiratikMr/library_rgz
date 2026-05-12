package example.org.library.model;

/**
 * Аудиокнига. Наследник LibraryItem.
 * Добавляет поля durationMinutes (длительность) и narrator (чтец).
 */
public class AudioBook extends LibraryItem {

    private static final long serialVersionUID = 1L;

    private int durationMinutes;
    private String narrator;

    public AudioBook() {
        super();
    }

    public AudioBook(String title, Author author, Genre genre, int year,
                     int durationMinutes, String narrator) {
        super(title, author, genre, year);
        this.durationMinutes = durationMinutes;
        this.narrator = narrator;
    }

    @Override
    public String getItemType() {
        return "Аудиокнига";
    }

    @Override
    public String getDetails() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        String duration = hours > 0 ? hours + " ч " + mins + " мин" : mins + " мин";
        return duration + (narrator != null && !narrator.isEmpty() ? ", чтец: " + narrator : "");
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getNarrator() {
        return narrator;
    }

    public void setNarrator(String narrator) {
        this.narrator = narrator;
    }
}
