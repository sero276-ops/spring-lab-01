package kz.iitu.springlab.web;

import kz.iitu.springlab.notify.NotificationService;
import kz.iitu.springlab.lifecycle.LifecycleDemo;
import kz.iitu.springlab.scope.TicketOffice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api/lab2")
public class Lab2Controller {

    private final NotificationService notifications;
    private final LifecycleDemo lifecycle;
    private final TicketOffice office;

    public Lab2Controller(NotificationService notifications, LifecycleDemo lifecycle, TicketOffice office) {
        this.notifications = notifications;
        this.lifecycle = lifecycle;
        this.office = office;
    }


    @GetMapping("/notify")
    public Map<String, Object> notify(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of(
                "primary", notifications.viaPrimary(text),
                "console", notifications.viaConsole(text),
                "all", notifications.viaAll(text),
                "beanNames", notifications.names()
        );
    }

    @GetMapping("/lifecycle")
    public List<String> lifecycle() {
        return lifecycle.events();
    }

    @GetMapping("/scopes")
    public Map<String, Object> scopes() {
        return office.demo();
    }
    @GetMapping("/custom")
    public Map<String, Object> custom(@RequestParam(defaultValue = "Hello") String text) {
        return Map.of(
                "input", text,
                "output", notifications.viaCustom(text),
                "channel", "reversed"
        );
    }

}


