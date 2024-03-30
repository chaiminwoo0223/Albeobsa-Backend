package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.service.CrawlingService;

import java.util.List;

@Tag(name = "Crawling API", description = "크롤링 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @GetMapping("/crawling")
    public ResponseEntity<List<CrawlingDTO>> getAllCrawlingData() {
        List<CrawlingDTO> crawlingData = crawlingService.getAllCrawledData();
        return ResponseEntity.ok(crawlingData);
    }
}