package skhu.jijijig.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Introduction;

import java.util.Optional;

@Repository
public interface IntroductionRepository extends JpaRepository<Introduction, Long> {
    Optional<Introduction> findByEmail(String email);
}