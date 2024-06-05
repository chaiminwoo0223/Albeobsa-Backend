package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import skhu.jijijig.domain.Counter;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CounterDTO {
    @Schema(description = "히트", example = "1000")
    private int hit;

    public static CounterDTO fromEntity(Counter counter) {
        return CounterDTO.builder().hit(counter.getHit()).build();
    }
}