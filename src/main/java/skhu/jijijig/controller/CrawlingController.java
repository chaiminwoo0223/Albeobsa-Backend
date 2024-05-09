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
import org.springframework.web.bind.annotation.RestController;
import skhu.jijijig.domain.dto.CrawlingDTO;
import skhu.jijijig.service.CrawlingService;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Tag(name = "Crawling API", description = "크롤링 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/crawling")
public class CrawlingController {
    private final CrawlingService crawlingService;

    @Operation(summary = "뽐뿌(국내게시판) 크롤링", description = "뽐뿌(국내게시판)의 내용을 크롤링하여 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(국내게시판) 크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/ppomppu")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> crawledPpomppuDomestic() {
        return crawlingService.performCrawlingForPpomppuDomestic()
                .thenApply(crawlings -> ResponseEntity.ok(crawlings.stream()
                        .map(CrawlingDTO::fromEntity)
                        .collect(Collectors.toList())))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @Operation(summary = "뽐뿌(해외게시판) 크롤링", description = "뽐뿌(해외게시판)의 내용을 크롤링하여 결과를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "뽐뿌(해외게시판) 크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/ppomppu4")
    public CompletableFuture<ResponseEntity<List<CrawlingDTO>>> crawledPpomppuOverseas() {
        return crawlingService.performCrawlingForPpomppuOverseas()
                .thenApply(crawlings -> ResponseEntity.ok(crawlings.stream()
                        .map(CrawlingDTO::fromEntity)
                        .collect(Collectors.toList())))
                .exceptionally(ex -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }
}