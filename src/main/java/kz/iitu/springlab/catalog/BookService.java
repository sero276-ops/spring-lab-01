package kz.iitu.springlab.catalog;

import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> findAll(String author) {
        return repository.findAll().stream()
                .filter(b -> author == null || author.isBlank()
                        || b.author().toLowerCase().contains(author.toLowerCase()))
                .sorted(Comparator.comparing(Book::id))
                .toList();
    }

    public Optional<Book> findById(long id) {
        return repository.findById(id);
    }

    public Book create(Book book) {
        return repository.save(new Book(null, book.title(), book.author(), book.year()));
    }

    public Optional<Book> update(long id, Book book) {
        if (repository.findById(id).isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(repository.save(new Book(id, book.title(), book.author(), book.year())));
    }

    public boolean delete(long id) {
        return repository.deleteById(id);
    }

    // Вариант 1: поиск по части названия без учёта регистра
    public List<Book> searchByTitle(String fragment) {
        String needle = fragment.toLowerCase();
        return repository.findAll().stream()
                .filter(b -> b.title().toLowerCase().contains(needle))
                .sorted(Comparator.comparing(Book::id))
                .toList();
    }
}
