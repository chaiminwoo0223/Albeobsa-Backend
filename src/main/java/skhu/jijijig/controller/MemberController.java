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

    @Operation(summary = "Google 로그인", description = "Google OAuth 인증 코드를 사용하여, 사용자 로그인을 처리하고 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/login")
    public ResponseEntity<?> login(@RequestParam("code") String code) {
        try {
            TokenDTO tokens = memberService.googleLoginSignup(code);
            return ResponseEntity.ok(tokens);
        } catch (IllegalArgumentException e) {
            log.error("로그인 실패: 부적절한 인자", e);
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("로그인 처리 중 서버 오류 발생", e);
            return ResponseEntity.internalServerError().body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "리프레시", description = "리프레시 토큰을 사용하여, 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리프레시 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody TokenDTO tokenDTO) {
        try {
            TokenDTO tokens = memberService.refreshAccessToken(tokenDTO.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (IllegalStateException e) {
            log.error("리프레시 실패: 유효하지 않거나 만료된 리프레시 토큰입니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("리프레시 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "로그아웃", description = "토큰을 비활성화하여, 로그아웃을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenDTO tokenDTO) {
        try {
            memberService.deactivateTokens(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
            return ResponseEntity.ok().body("로그아웃 성공");
        } catch (IllegalStateException e) {
            log.error("로그아웃 실패: 유효하지 않은 토큰입니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}