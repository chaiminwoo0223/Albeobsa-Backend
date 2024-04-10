package skhu.jijijig.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skhu.jijijig.domain.dto.IntroductionDTO;
import skhu.jijijig.domain.model.Introduction;
import skhu.jijijig.repository.IntroductionRepository;

@RequiredArgsConstructor
@Service
public class IntroductionService {
    private final IntroductionRepository introductionRepository;

    @Transactional
    public Introduction createOrUpdateIntroduction(IntroductionDTO introductionDTO) {
        Introduction introduction = introductionRepository.findByEmail(introductionDTO.getEmail())
                .map(existingIntroduction -> {
                    existingIntroduction.updateFromDTO(introductionDTO);
                    return existingIntroduction;
                })
                .orElseGet(() -> Introduction.fromDTO(introductionDTO));
        return introductionRepository.save(introduction);
    }
}