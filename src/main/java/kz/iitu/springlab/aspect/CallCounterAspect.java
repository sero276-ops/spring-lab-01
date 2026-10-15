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

//Мой вариант счетчик вызовов
@Aspect
@Component
@Order(4)
public class CallCounterAspect {

    private final Map<String, AtomicLong> counters = new ConcurrentHashMap<>();

    @Before("kz.iitu.springlab.aspect.Pointcuts.serviceOperation()")
    public void count(JoinPoint jp) {
        String method = jp.getSignature().toShortString();
        counters.computeIfAbsent(method, k -> new AtomicLong()).incrementAndGet();
    }

    public Map<String, Long> getStatistics() {
        Map<String, Long> result = new TreeMap<>();
        counters.forEach((k, v) -> result.put(k, v.get()));
        return result;
    }
}

