package kz.iitu.springlab.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class Pointcuts {

    // ГДЕ: все классы в пакете service и его подпакетах
    @Pointcut("within(kz.iitu.springlab.service..*)")
    public void serviceLayer() { }

    // ГДЕ: выполнение любого public-метода
    @Pointcut("execution(public * *(..))")
    public void publicMethod() { }

    // Комбинация: public-методы сервисного слоя
    @Pointcut("serviceLayer() && publicMethod()")
    public void serviceOperation() { }
}
