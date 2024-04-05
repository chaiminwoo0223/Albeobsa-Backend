package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skhu.jijijig.service.CrawlingService;

@Tag(name = "Crawling API", description = "크롤링 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @GetMapping("/crawling")
    public ResponseEntity<String> crawledWebSites() {
        crawlingService.crawlPpomppuDomestic();
        crawlingService.crawlPpomppuOverseas();
        crawlingService.crawlClien();
        crawlingService.crawlRuliweb();
        crawlingService.crawlCoolenjoy();
        crawlingService.crawlQuasarzone();
        return ResponseEntity.ok("Body 내용이 모두 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/ppomppu")
    public ResponseEntity<String> crawledPpomppu() {
        crawlingService.crawlPpomppuDomestic();
        return ResponseEntity.ok("뿜뿌 국내게시판 body 내용이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/ppomppu4")
    public ResponseEntity<String> crawledPpomppu4() {
        crawlingService.crawlPpomppuOverseas();
        return ResponseEntity.ok("뿜뿌 해외게시판 body 내용이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/clien")
    public ResponseEntity<String> crawledClien() {
        crawlingService.crawlClien();
        return ResponseEntity.ok("클리앙 body 내용이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/ruliweb")
    public ResponseEntity<String> crawledRuliweb() {
        crawlingService.crawlRuliweb();
        return ResponseEntity.ok("루리웹 body 내용이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/coolenjoy")
    public ResponseEntity<String> crawledCoolenjoy() {
        crawlingService.crawlCoolenjoy();
        return ResponseEntity.ok("쿨엔조이 body 내용이 성공적으로 저장되었습니다.");
    }

    @GetMapping("/crawling/quasarzone")
    public ResponseEntity<String> crawledQuasarzone() {
        crawlingService.crawlQuasarzone();
        return ResponseEntity.ok("퀘사이존 body 내용이 성공적으로 저장되었습니다.");
    }
}