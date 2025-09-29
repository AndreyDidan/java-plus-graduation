package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.proccesor.EventSimilarityProcessor;
import ru.practicum.proccesor.UserActionProcessor;

@EnableDiscoveryClient
@SpringBootApplication
public class AnalyzerApp {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnalyzerApp.class, args);

        final UserActionProcessor userActionProcessor =
                context.getBean(UserActionProcessor.class);
        final EventSimilarityProcessor eventSimilarityProcessor =
                context.getBean(EventSimilarityProcessor.class);

        Thread hubEventsThread = new Thread(userActionProcessor);
        hubEventsThread.setName("UserActionHandlerThread");
        hubEventsThread.start();

        eventSimilarityProcessor.run();
    }
}