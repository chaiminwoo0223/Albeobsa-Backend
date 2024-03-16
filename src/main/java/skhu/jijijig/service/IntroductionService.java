package skhu.jijijig.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.dto.IntroductionDTO;
import skhu.jijijig.domain.model.Introduction;
import skhu.jijijig.domain.repository.IntroductionRepository;

@Service
public class IntroductionService {
    private final IntroductionRepository introductionRepository;

    public IntroductionService(IntroductionRepository introductionRepository) {
        this.introductionRepository = introductionRepository;
    }

    @Transactional
    public Introduction saveIntroduction(IntroductionDTO introductionDTO) {
        if (introductionRepository.existsByEmail(introductionDTO.getEmail())) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다: " + introductionDTO.getEmail());
        }
        Introduction introduction = Introduction.fromDTO(introductionDTO);
        return introductionRepository.save(introduction);
    }
}