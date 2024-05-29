package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.service.CrawlingService;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Tag(name = "Crawling API", description = "크롤링 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @Operation(summary = "검색", description = "내용을 검색합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/search")
    public Page<CrawlingDTO> search(@RequestParam(required = false) String keyword, Pageable pageable) {
        return crawlingService.searchCrawling(keyword, pageable);
    }

    @Operation(summary = "랭킹 조회", description = "랭킹의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "랭킹 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/ranking")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ranking() {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getTop10CrawlingsByRanking();
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "핫딜 조회", description = "핫딜의 내용을 페이지별로 조회합니다. (page=0,1,2 ···)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "핫딜 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> hotdeal(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getAllCrawlingsSortedByDateTime(pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "핫딜 상세 조회", description = "핫딜의 상세 내용을 페이지별로 조회합니다. (page=0,1,2 ···)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "핫딜 상세 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/detail")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> detail(@RequestParam("label") String label, Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime(label, pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}