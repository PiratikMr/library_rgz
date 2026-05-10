package example.org.library.service;

import example.org.library.model.Book;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для сохранения и загрузки данных библиотеки.
 * Использует Java-сериализацию (ObjectOutputStream / ObjectInputStream).
 */
public class DataService {

    private static final String DATA_DIR = "data";
    private static final String DATA_FILE = "library.dat";

    private final Path dataFilePath;

    public DataService() {
        // Файл данных хранится в директории data/ рядом с проектом
        dataFilePath = Paths.get(DATA_DIR, DATA_FILE);
    }

    /**
     * Сохранить список книг в файл.
     */
    @SuppressWarnings("unchecked")
    public void save(List<Book> books) {
        try {
            // Создаём директорию, если не существует
            Path dir = dataFilePath.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(dataFilePath.toFile()))) {
                oos.writeObject(new ArrayList<>(books));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Загрузить список книг из файла.
     * Возвращает пустой список, если файл не существует.
     */
    @SuppressWarnings("unchecked")
    public List<Book> load() {
        if (!Files.exists(dataFilePath)) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(dataFilePath.toFile()))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                return (List<Book>) obj;
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки данных: " + e.getMessage());
            e.printStackTrace();
        }

        return new ArrayList<>();
    }
}
