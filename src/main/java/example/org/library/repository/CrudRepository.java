package example.org.library.repository;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Обобщённый интерфейс репозитория (паттерн Repository).
 * Определяет стандартные CRUD-операции для любой сущности.
 *
 * @param <T> тип сущности
 */
public interface CrudRepository<T extends Serializable> {

    /**
     * Сохранить или обновить сущность.
     */
    void save(T entity);

    /**
     * Удалить сущность по идентификатору.
     */
    void delete(String id);

    /**
     * Найти сущность по идентификатору.
     */
    Optional<T> findById(String id);

    /**
     * Получить все сущности.
     */
    List<T> findAll();

    /**
     * Сохранить список сущностей (перезаписывает все данные).
     */
    void saveAll(List<T> entities);
}
