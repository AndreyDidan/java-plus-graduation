package ru.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Builder(toBuilder = true)
@Data
@Entity
@Table(name = "events_similarity")
@AllArgsConstructor
@NoArgsConstructor
public class EventSimilarity {
    @Id
    @Column(name = "event_a")
    private Long eventA;
    @Column(name = "event_b")
    private Long eventB;
    @Column(name = "score")
    private Double score;
    private Instant created;
}
