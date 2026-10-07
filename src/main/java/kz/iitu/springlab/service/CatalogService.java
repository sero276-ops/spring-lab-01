package kz.iitu.springlab.service;

import kz.iitu.springlab.audit.Audited;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

@Service
public class CatalogService {

    public String findById(long id) {
        sleep(50); // имитация обращения к БД
        return "Item no. " + id;
    }

    @Audited(action = "CATALOG_LIST", logArguments = true)
    public List<String> findAll(int limit) {
        sleep(300); // намеренно «медленный» метод
        return IntStream.rangeClosed(1, limit)
                .mapToObj(i -> "Item no. " + i)
                .toList();
    }

    @Audited(action = "CATALOG_REMOVE")
    public String remove(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Invalid identifier: " + id);
        }
        return "Removed item no. " + id;
    }

    // Self-invocation: remove() вызывается через this — мимо прокси
    public String removeTwice(long id) {
        String first = remove(id);       // это this.remove(id)
        String second = remove(id + 1);  // и это тоже
        return first + "; " + second;
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
