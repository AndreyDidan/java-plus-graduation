package ru.practicum.configuration;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Getter
@Configuration
@EnableConfigurationProperties({KafkaProperties.class})
public class KafkaConfiguration {
    private final KafkaProperties kafkaProperties;

    public KafkaConfiguration(KafkaProperties kafkaProperties) {
        this.kafkaProperties = kafkaProperties;
        log.info("KafkaProperties.bootstrapServers = {}", kafkaProperties.getBootstrapServers());
        log.info("KafkaProperties.producer = {}", kafkaProperties.getProducer());
        log.info("KafkaProperties.consumer = {}", kafkaProperties.getConsumer());

    }

    @Bean
    public Producer<String, SpecificRecordBase> producer() {
        Properties properties = new Properties();
        KafkaProducerProperties producerProps = kafkaProperties.getProducer();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, producerProps.getClientIdConfig());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, producerProps.getKeySerializer());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, producerProps.getValueSerializer());
        log.info("Kafka Producer properties: {}", properties);
        return new KafkaProducer<>(properties);

    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> consumer() {
        Properties props = new Properties();
        KafkaConsumerProperties consumerProps = kafkaProperties.getConsumer();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerProps.getGroupId());
        props.put(ConsumerConfig.CLIENT_ID_CONFIG, consumerProps.getClientIdConfig());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerProps.getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerProps.getValueDeserializer());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, consumerProps.getEnableAutoCommit());
        return new KafkaConsumer<>(props);
    }
}