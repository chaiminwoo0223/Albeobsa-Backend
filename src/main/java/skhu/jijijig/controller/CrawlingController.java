package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

    @Operation(summary = "핫딜 조회", description = "핫딜의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "핫딜 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> hotdeal(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "100") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getAllCrawlingsSortedByDateTime(page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "뽐뿌(국내게시판) 조회", description = "뽐뿌(국내게시판)의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(국내게시판) 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ppomppu")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ppomppuDomestic(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("뽐뿌(국내게시판)", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "뽐뿌(해외게시판) 조회", description = "뽐뿌(해외게시판)의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(해외게시판) 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ppomppu4")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ppomppuOverseas(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("뽐뿌(해외게시판)", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "퀘사이존 조회", description = "퀘사이존의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "퀘사이존 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/quasarzone")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> quasarzone(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("퀘사이존", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "어미새 조회", description = "어미새의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "어미새 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/eomisae")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> eomisae(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("어미새", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "루리웹 조회", description = "루리웹의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "루리웹 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/ruliweb")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> ruliweb(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("루리웹", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "쿨엔조이 조회", description = "쿨엔조이의 내용을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿨엔조이 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/hotdeal/coolenjoy")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> coolenjoy(@RequestParam(value = "page", defaultValue = "0") int page, @RequestParam(value = "size", defaultValue = "20") int size) {
        return CompletableFuture.supplyAsync(() -> {
            List<CrawlingDTO> crawlings = crawlingService.getCrawlingsSortedByLabelAndDateTime("쿨엔조이", page, size);
            return ResponseEntity.ok(crawlings);
        }).exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}