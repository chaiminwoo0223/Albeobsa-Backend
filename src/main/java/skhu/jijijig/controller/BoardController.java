package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import skhu.jijijig.domain.dto.BoardDTO;
import skhu.jijijig.domain.dto.CommentDTO;
import skhu.jijijig.exception.ResourceNotFoundException;
import skhu.jijijig.service.BoardService;
import skhu.jijijig.service.CommentService;
import skhu.jijijig.service.HeartService;

import java.security.Principal;

@Slf4j
@Tag(name = "Board API", description = "게시판 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final HeartService heartService;

    @Operation(summary = "전체 게시글 조회", description = "전체 게시글을 페이지별로 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "전체 게시글 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/")
    public ResponseEntity<Page<BoardDTO>> readBoards(Pageable pageable) {
        try {
            Page<BoardDTO> boards = boardService.getBoards(pageable);
            return ResponseEntity.ok(boards);
        } catch (Exception e) {
            log.error("전체 게시글을 페이지별로 조회 중 오류 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 조회", description = "게시글을 상세 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDTO> readBoard(@PathVariable Long boardId) {
        try{
            BoardDTO boardDTO = boardService.getBoard(boardId);
            return ResponseEntity.ok(boardDTO);
        } catch (ResourceNotFoundException e) {
            log.error("게시글 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("게시글 조회 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/post")
    public ResponseEntity<?> createBoard(Principal principal, @RequestBody BoardDTO boardDTO) {
        try {
            Long memberId = Long.parseLong(principal.getName());
            Long boardId = boardService.addBoard(boardDTO, memberId);
            return new ResponseEntity<>(boardId, HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("게시글 생성 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게시글 수정 성공"),
            @ApiResponse(responseCode = "403", description = "게시글 수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PutMapping("/{boardId}")
    public ResponseEntity<?> updateBoard(Principal principal, @PathVariable Long boardId, @RequestBody BoardDTO boardDTO) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            boardService.editBoard(boardId, boardDTO, memberId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.error("게시글 수정 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("게시글 수정 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("게시글 수정 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "게시글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "게시글 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            boardService.removeBoard(boardId, memberId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            log.error("게시글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("게시글 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("게시글 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 생성", description = "게시글에 댓글을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "댓글 생성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{boardId}/comments")
    public ResponseEntity<?> createComment(Principal principal, @PathVariable Long boardId, @RequestBody CommentDTO commentDTO) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            commentService.addComment(boardId, memberId, commentDTO.getContent());
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("댓글 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("댓글 생성 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "댓글 삭제", description = "게시글의 댓글을 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음")
    })
    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(Principal principal, @PathVariable Long boardId, @PathVariable Long commentId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            commentService.removeComment(boardId, commentId, memberId);
            return ResponseEntity.ok().body("댓글이 성공적으로 삭제되었습니다.");
        } catch (ResourceNotFoundException e) {
            log.error("댓글 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (AccessDeniedException e) {
            log.error("댓글 삭제 권한 없음: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            log.error("댓글 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 생성", description = "게시글에 좋아요를 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요 생성 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @PostMapping("/{boardId}/hearts")
    public ResponseEntity<?> createHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            heartService.addHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("좋아요 생성 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("좋아요 생성 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "좋아요 삭제", description = "게시글의 좋아요를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "좋아요 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음")
    })
    @DeleteMapping("/{boardId}/hearts")
    public ResponseEntity<?> deleteHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        try {
            heartService.removeHeart(boardId, memberId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ResourceNotFoundException e) {
            log.error("좋아요 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("좋아요 삭제 중 예외 발생: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}