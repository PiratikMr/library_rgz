package example.org.library.repository;

import example.org.library.model.Reader;

/**
 * Репозиторий для читателей.
 * Сериализует данные в файл readers.dat.
 */
public class ReaderRepository extends AbstractFileRepository<Reader> {

    public ReaderRepository() {
        super("readers.dat", Reader::getId);
    }
}
