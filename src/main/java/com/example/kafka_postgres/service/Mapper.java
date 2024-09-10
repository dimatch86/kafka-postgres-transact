package com.example.kafka_postgres.service;

import com.example.kafka_postgres.dto.KafkaDto;
import com.example.kafka_postgres.model.Product;

public class Mapper {
    public static Product toEntity(KafkaDto kafkaDto) {
        return Product.builder()
                .name(kafkaDto.getName())
                .price(kafkaDto.getNumber())
                .build();
    }
}
