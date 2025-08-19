package ru.practicum.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.practicum.processor.EventSimilarityProcessor;
import ru.practicum.processor.UserActionEventProcessor;

@Component
@RequiredArgsConstructor
public class KafkaConsumersStarter implements ApplicationRunner {

    private final UserActionEventProcessor userActionEventProcessor;
    private final EventSimilarityProcessor eventSimilarityProcessor;

    @Override
    public void run(ApplicationArguments args) {
        Thread userActionThread = new Thread(userActionEventProcessor);
        userActionThread.setName("UserActionEventProcessor-Thread");
        userActionThread.setDaemon(true);
        userActionThread.start();

        Thread eventSimilarityThread = new Thread(eventSimilarityProcessor);
        eventSimilarityThread.setName("EventSimilarityProcessor-Thread");
        eventSimilarityThread.setDaemon(true);
        eventSimilarityThread.start();
    }
}