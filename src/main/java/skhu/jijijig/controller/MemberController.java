package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import skhu.jijijig.domain.dto.ErrorResponseDTO;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.service.MemberService;

@Slf4j
@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "콜백", description = "Google OAuth2를 통해 리디렉트된 요청을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "콜백 처리 성공, 사용자는 성공적으로 인증되었으며 액세스 토큰이 발급되었습니다."),
            @ApiResponse(responseCode = "400", description = "잘못된 요청, 요청에 필요한 'code' 매개변수가 누락되었거나 잘못된 값이 제공되었습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패, 제공된 'code'가 유효하지 않거나 만료되었습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 내부 오류, Google OAuth2 처리 중 예기치 않은 오류가 발생했습니다.", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/authenticate")
    public ResponseEntity<?> googleOAuth2Callback(@RequestParam(name = "code") String code) {
        try {
            TokenDTO tokens = memberService.googleLoginSignup(code);
            return ResponseEntity.ok(tokens);
        } catch (RuntimeException e) {
            log.error("런타임 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (Exception e) {
            log.error("처리되지 않은 예외 발생: ", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "예기치 않은 오류가 발생했습니다.", e);
        }
    }
}