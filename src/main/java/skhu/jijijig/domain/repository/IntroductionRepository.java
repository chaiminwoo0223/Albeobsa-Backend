package skhu.jijijig.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skhu.jijijig.domain.model.Introduction;

@Repository
public interface IntroductionRepository extends JpaRepository<Introduction, Long> {
    boolean existsByEmail(String email);
}