package com.example.kafka_postgres.service;

import com.example.kafka_postgres.dto.KafkaDto;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    @KafkaListener(topics = "topic1", groupId = "dimatch", containerFactory = "kafkaListenerContainerFactory")
    public void listenMetricsAndObserved(KafkaDto kafkaDto) {
        System.out.println(kafkaDto.getName());
    }
}
