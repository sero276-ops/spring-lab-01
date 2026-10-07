package kz.iitu.springlab.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

// Индивидуальный вариант 1: счётчик вызовов
@Aspect
@Component
@Order(4)
public class CallCounterAspect {

    // имя метода -> количество вызовов
    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // перед каждым вызовом метода сервисного слоя увеличиваем счётчик
    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void count(JoinPoint jp) {
        String method = jp.getSignature().toShortString();
        counters.computeIfAbsent(method, k -> new AtomicLong()).incrementAndGet();
    }

    // отдаём копию статистики, отсортированную по имени метода
    public Map<String, Long> getStatistics() {
        Map<String, Long> result = new TreeMap<>();
        counters.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}

