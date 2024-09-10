package com.example.kafka_postgres;

import com.example.kafka_postgres.dto.KafkaDto;
import com.example.kafka_postgres.repository.JdbcProductRepository;
import com.example.kafka_postgres.service.KafkaService;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
@DirtiesContext
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@EmbeddedKafka(partitions = 1, count = 3, controlledShutdown = true)
@SpringBootTest(properties = "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}")
class ProductItTest {

    @Mock
    private JdbcProductRepository repository;
    @Autowired
    @InjectMocks
    private KafkaService kafkaService;
    @Autowired
    private Environment environment;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafka;
    private KafkaMessageListenerContainer<String, KafkaDto> container;
    private BlockingQueue<ConsumerRecord<String, KafkaDto>> records;

    @BeforeAll
    void setUp() {
        DefaultKafkaConsumerFactory<String, Object> kafkaConsumerFactory =
                new DefaultKafkaConsumerFactory<>(getConsumerProperties());
        ContainerProperties containerProperties =
                new ContainerProperties(environment.getProperty("spring.kafka.topic"));
        container = new KafkaMessageListenerContainer<>(kafkaConsumerFactory, containerProperties);
        records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, KafkaDto>)records::add);
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getPartitionsPerTopic());
    }

    private Map<String, Object> getConsumerProperties() {
        return Map.of(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getBrokersAsString(),
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
                ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
                ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("spring.kafka.consumer.group-id"),
                JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("spring.kafka.consumer.properties.spring.json.trusted.packages"),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("spring.kafka.consumer.auto-offset-reset"));
    }

    @Test
    void test() throws InterruptedException {
        System.out.println(environment.getProperty("spring.kafka.topic"));
        KafkaDto kafkaDto = new KafkaDto();
        kafkaDto.setName("milk");
        kafkaDto.setNumber(123);

        doThrow(RuntimeException.class).when(repository).savePeoduct(any());

        kafkaService.sendToKafka(kafkaDto);

        ConsumerRecord<String, KafkaDto> message = records.poll(3000, TimeUnit.MILLISECONDS);

        assertNotNull(message);
        assertNotNull(message.key());
        KafkaDto k = message.value();
        assertEquals(kafkaDto.getName(), k.getName());

    }

    @AfterAll
    void tearDown() {
        container.stop();
    }

}
