package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skhu.jijijig.domain.dto.ErrorResponseDTO;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.service.MemberService;

@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "인증", description = "Google OAuth 인증 코드를 받아서, 클라이언트에게 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인증 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/authenticate")
    public ResponseEntity<String> authenticate(@RequestParam("code") String code) {
        return ResponseEntity.ok(code);
    }

    @Operation(summary = "로그인", description = "Google OAuth 인증 코드를 사용하여, 사용자 로그인을 처리하고 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestParam("code") String code) {
        TokenDTO tokens = memberService.googleLoginSignup(code);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "리프레시", description = "리프레시 토큰을 사용하여, 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리프레시 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenDTO> refresh(@RequestBody TokenDTO tokenDTO) {
        TokenDTO tokens = memberService.refreshAccessToken(tokenDTO.getRefreshToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "로그아웃", description = "토큰을 비활성화하여, 로그아웃을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody TokenDTO tokenDTO) {
        memberService.deactivateTokens(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
        return ResponseEntity.ok("로그아웃 성공");
    }
}