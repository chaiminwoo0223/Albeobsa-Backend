package skhu.jijijig.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Builder
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Counter {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counter_id")
    private Long id;

    @Column(nullable = false)
    private int hit;

    public Counter increaseHit() {
        return Counter.builder()
                .id(this.id)
                .hit(this.hit + 1)
                .build();
    }
}