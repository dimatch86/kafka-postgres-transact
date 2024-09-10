package com.example.kafka_postgres.controller;

import com.example.kafka_postgres.dto.KafkaDto;
import com.example.kafka_postgres.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
@RequiredArgsConstructor
public class MessageController {

    private final KafkaService kafkaService;

    @PostMapping("/post")
    public ResponseEntity<String> post(@RequestBody KafkaDto kafkaDto) {
        kafkaService.sendToKafka(kafkaDto);
        return ResponseEntity.ok("Success with " + kafkaDto.getName());
    }
}
