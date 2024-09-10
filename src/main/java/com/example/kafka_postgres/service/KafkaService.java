package com.example.kafka_postgres.service;

import com.example.kafka_postgres.dto.KafkaDto;
import com.example.kafka_postgres.repository.JdbcProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class KafkaService {

    @Value("${spring.kafka.topic}")
    private String topic;

    private final KafkaTemplate<String, KafkaDto> kafkaTemplate;
    private final JdbcProductRepository jdbcProductRepository;

    @Transactional("jdbcTransactionManager")
    public void sendToKafka(KafkaDto kafkaDto) {
        kafkaTemplate.send(topic,"ff", kafkaDto);
        jdbcProductRepository.savePeoduct(Mapper.toEntity(kafkaDto));
    }
}
