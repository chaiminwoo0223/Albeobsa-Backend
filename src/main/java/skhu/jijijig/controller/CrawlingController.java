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

    @Operation(summary = "핫딜 조회", description = "핫딜의 내용을 조회합니다.")
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

    @Operation(summary = "뽐뿌(국내게시판) 조회", description = "뽐뿌(국내게시판)의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(국내게시판) 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ppomppu")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ppomppuDomestic(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("뽐뿌(국내게시판)", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "뽐뿌(해외게시판) 조회", description = "뽐뿌(해외게시판)의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(해외게시판) 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ppomppu4")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ppomppuOverseas(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("뽐뿌(해외게시판)", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "퀘사이존 조회", description = "퀘사이존의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀘사이존 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/quasarzone")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> quasarzone(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("퀘사이존", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "어미새 조회", description = "어미새의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "어미새 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/eomisae")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> eomisae(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("어미새", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "루리웹 조회", description = "루리웹의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "루리웹 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ruliweb")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ruliweb(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("루리웹", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "쿨엔조이 조회", description = "쿨엔조이의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿨엔조이 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/coolenjoy")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> coolenjoy(Pageable pageable) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("쿨엔조이", pageable);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}