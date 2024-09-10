package com.example.kafka_postgres.repository;

import com.example.kafka_postgres.model.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;


@Repository
@RequiredArgsConstructor
public class JdbcProductRepository {

    private final JdbcTemplate jdbcTemplate;

    public void savePeoduct(Product product) {
        String sql = "INSERT INTO product (id, name, price, timestamp) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, 1, product.getName(), product.getPrice(), product.getTimestamp());

    }
}
