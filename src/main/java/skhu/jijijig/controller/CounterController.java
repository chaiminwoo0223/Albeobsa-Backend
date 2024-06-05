package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skhu.jijijig.domain.dto.CounterDTO;
import skhu.jijijig.domain.dto.ErrorResponseDTO;
import skhu.jijijig.service.CounterService;

@Tag(name = "Counter API", description = "방문자수 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/counter")
public class CounterController {
    private final CounterService counterService;

    @Operation(summary = "히트 증가", description = "히트를 증가시킵니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "히트 증가 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping
    public ResponseEntity<CounterDTO> hit() {
        CounterDTO counterDTO = counterService.increaseHit();
        return ResponseEntity.ok(counterDTO);
    }
}