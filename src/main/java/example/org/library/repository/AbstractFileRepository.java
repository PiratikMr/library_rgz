package example.org.library.repository;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Абстрактная реализация CrudRepository с сериализацией в файл.
 * Содержит общую логику сохранения/загрузки, конкретные репозитории
 * только указывают имя файла и способ получения ID сущности.
 *
 * @param <T> тип сущности
 */
public abstract class AbstractFileRepository<T extends Serializable> implements CrudRepository<T> {

    private static final String DATA_DIR = "data";

    private final List<T> items = new ArrayList<>();
    private final Path dataFilePath;
    private final Function<T, String> idExtractor;

    /**
     * @param fileName    имя файла данных (например, "items.dat")
     * @param idExtractor функция для получения ID из сущности
     */
    protected AbstractFileRepository(String fileName, Function<T, String> idExtractor) {
        this.dataFilePath = Paths.get(DATA_DIR, fileName);
        this.idExtractor = idExtractor;
        loadFromFile();
    }

    @Override
    public void save(T entity) {
        String id = idExtractor.apply(entity);
        // Обновляем, если сущность с таким ID уже существует
        for (int i = 0; i < items.size(); i++) {
            if (idExtractor.apply(items.get(i)).equals(id)) {
                items.set(i, entity);
                persist();
                return;
            }
        }
        // Иначе добавляем новую
        items.add(entity);
        persist();
    }

    @Override
    public void delete(String id) {
        items.removeIf(item -> idExtractor.apply(item).equals(id));
        persist();
    }

    @Override
    public Optional<T> findById(String id) {
        return items.stream()
                .filter(item -> idExtractor.apply(item).equals(id))
                .findFirst();
    }

    @Override
    public List<T> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public void saveAll(List<T> entities) {
        items.clear();
        items.addAll(entities);
        persist();
    }

    /**
     * Сериализация списка в файл.
     */
    @SuppressWarnings("unchecked")
    private void persist() {
        try {
            Path dir = dataFilePath.getParent();
            if (dir != null && !Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(dataFilePath.toFile()))) {
                oos.writeObject(new ArrayList<>(items));
            }
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных в " + dataFilePath + ": " + e.getMessage());
        }
    }

    /**
     * Десериализация списка из файла.
     */
    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        if (!Files.exists(dataFilePath)) {
            return;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new FileInputStream(dataFilePath.toFile()))) {
            Object obj = ois.readObject();
            if (obj instanceof List<?>) {
                items.addAll((List<T>) obj);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Ошибка загрузки данных из " + dataFilePath + ": " + e.getMessage());
        }
    }
}
