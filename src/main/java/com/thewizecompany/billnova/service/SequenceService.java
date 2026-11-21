package com.thewizecompany.billnova.service;


import com.thewizecompany.billnova.enums.SequenceType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SequenceService {

    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Integer nextSequence(SequenceType sequenceType, int year){
        String query = """
                INSERT INTO sequences (sequence_type, sequence_year, last_sequence) VALUES (?, ?, 1)
                ON CONFLICT (sequence_type, sequence_year) DO UPDATE SET last_sequence = sequences.last_sequence + 1
                RETURNING last_sequence
                """;
        return jdbcTemplate.queryForObject(query, Integer.class, sequenceType.name(), year);
    }
}
