package skhu.jijijig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.Counter;

@Repository
public interface CounterRepository extends JpaRepository<Counter, Long> {
}