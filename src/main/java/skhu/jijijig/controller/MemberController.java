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
import skhu.jijijig.domain.dto.MemberDTO;
import skhu.jijijig.domain.dto.TokenDTO;
import skhu.jijijig.exception.EmailAlreadyExistsException;
import skhu.jijijig.exception.FirebaseAuthenticationException;
import skhu.jijijig.exception.InvalidTokenException;
import skhu.jijijig.service.MemberService;

@Slf4j
@Tag(name = "Member API", description = "사용자 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/member")
public class MemberController {
    private final MemberService memberService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원가입 성공", content = @Content(schema = @Schema(implementation = MemberDTO.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/join")
    public ResponseEntity<?> join(@RequestBody MemberDTO memberDTO) {
        try {
            MemberDTO registeredMember = memberService.join(memberDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(registeredMember);
        } catch (EmailAlreadyExistsException e) {
            log.error("회원가입 실패: 이메일이 이미 존재합니다.", e);
            return ResponseEntity.badRequest().body(new ErrorResponseDTO(e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        } catch (Exception e) {
            log.error("회원가입 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody String firebaseToken) {
        try {
            TokenDTO tokens = memberService.login(firebaseToken);
            return ResponseEntity.ok(tokens);
        } catch (FirebaseAuthenticationException e) {
            log.error("로그인 실패: 인증에 실패하였습니다.", e);
            return ResponseEntity.status(e.getStatus()).body(new ErrorResponseDTO(e.getMessage(), e.getStatus().value()));
        } catch (Exception e) {
            log.error("로그인 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "리프레시", description = "리프레시 토큰을 사용하여 액세스 토큰을 갱신합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리프레시 성공", content = @Content(schema = @Schema(implementation = TokenDTO.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestParam("refreshToken") String refreshToken) {
        try {
            TokenDTO tokens = memberService.refresh(refreshToken);
            return ResponseEntity.ok(tokens);
        } catch (InvalidTokenException e) {
            log.error("리프레시 실패: 유효하지 않거나 만료된 리프레시 토큰입니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("리프레시 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그아웃 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 오류", content = @Content)
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody TokenDTO tokenDTO) {
        try {
            memberService.logout(tokenDTO.getAccessToken(), tokenDTO.getRefreshToken());
            return ResponseEntity.ok().build();
        } catch (InvalidTokenException e) {
            log.error("로그아웃 실패: 유효하지 않은 토큰입니다.", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponseDTO(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
        } catch (Exception e) {
            log.error("로그아웃 처리 중 서버 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponseDTO("서버 오류 발생", HttpStatus.INTERNAL_SERVER_ERROR.value()));
        }
    }
}