package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.Counter;
import skhu.jijijig.domain.dto.CounterDTO;
import skhu.jijijig.repository.CounterRepository;

@Service
@RequiredArgsConstructor
public class CounterService {
    private final CounterRepository counterRepository;

    @Transactional
    public CounterDTO increaseHit() {
        Counter counter = counterRepository.findById(1L).orElse(Counter.builder().hit(0).build());
        Counter increasedCounter = Counter.builder()
                .id(counter.getId())
                .hit(counter.getHit() + 1)
                .build();
        counterRepository.save(increasedCounter);
        return CounterDTO.fromEntity(increasedCounter);
    }
}