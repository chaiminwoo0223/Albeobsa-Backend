package skhu.jijijig.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import skhu.jijijig.domain.dto.BoardDTO;
import skhu.jijijig.domain.dto.CommentDTO;
import skhu.jijijig.domain.dto.ErrorResponseDTO;
import skhu.jijijig.service.BoardService;
import skhu.jijijig.service.CommentService;
import skhu.jijijig.service.HeartService;

import java.security.Principal;

@Tag(name = "Board API", description = "게시판 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/boards")
public class BoardController {
    private final BoardService boardService;
    private final CommentService commentService;
    private final HeartService heartService;

    @Operation(summary = "전체 게시글 조회", description = "전체 게시글을 페이지별로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "전체 게시글 조회 성공", content = @Content(schema = @Schema(implementation = BoardDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping
    public ResponseEntity<Page<BoardDTO>> readBoards(Pageable pageable) {
        Page<BoardDTO> boards = boardService.getBoards(pageable);
        return ResponseEntity.ok(boards);
    }

    @Operation(summary = "게시글 조회", description = "게시글을 상세 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "게시글 조회 성공", content = @Content(schema = @Schema(implementation = BoardDTO.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDTO> readBoard(@PathVariable Long boardId) {
        BoardDTO boardDTO = boardService.getBoard(boardId);
        return ResponseEntity.ok(boardDTO);
    }

    @Operation(summary = "게시글 생성", description = "새로운 게시글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "게시글 생성 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/post")
    public ResponseEntity<String> createBoard(Principal principal, @RequestBody BoardDTO boardDTO) {
        Long memberId = Long.parseLong(principal.getName());
        Long boardId = boardService.addBoard(boardDTO, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body("게시글이 성공적으로 생성되었습니다. boardId: " + boardId);
    }

    @Operation(summary = "게시글 수정", description = "게시글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "게시글 수정 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "게시글 수정 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PutMapping("/{boardId}")
    public ResponseEntity<String> updateBoard(Principal principal, @PathVariable Long boardId, @RequestBody BoardDTO boardDTO) {
        Long memberId = Long.parseLong(principal.getName());
        boardService.editBoard(boardId, boardDTO, memberId);
        return ResponseEntity.accepted().body("게시글이 성공적으로 수정되었습니다. boardId: " + boardId);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "게시글 삭제 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "게시글 삭제 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        boardService.removeBoard(boardId, memberId);
        return ResponseEntity.accepted().body("게시글이 성공적으로 삭제되었습니다. boardId: " + boardId);
    }

    @Operation(summary = "댓글 생성", description = "게시글에 댓글을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "댓글 생성 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{boardId}/comments")
    public ResponseEntity<String> createComment(Principal principal, @PathVariable Long boardId, @RequestBody CommentDTO commentDTO) {
        Long memberId = Long.parseLong(principal.getName());
        Long commentId = commentService.addComment(boardId, memberId, commentDTO.getContent());
        return ResponseEntity.status(HttpStatus.CREATED).body("댓글이 성공적으로 생성되었습니다. commentId: " + commentId);
    }

    @Operation(summary = "댓글 수정", description = "게시글의 댓글을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "댓글 수정 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "댓글 수정 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PatchMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<String> updateComment(Principal principal, @PathVariable Long boardId, @PathVariable Long commentId, @RequestBody CommentDTO commentDTO) {
        Long memberId = Long.parseLong(principal.getName());
        commentService.editComment(boardId, commentId, memberId, commentDTO.getContent());
        return ResponseEntity.accepted().body("댓글이 성공적으로 수정되었습니다. commentId: " + commentId);
    }

    @Operation(summary = "댓글 삭제", description = "게시글의 댓글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "댓글 삭제 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "댓글 삭제 권한 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<String> deleteComment(Principal principal, @PathVariable Long boardId, @PathVariable Long commentId) {
        Long memberId = Long.parseLong(principal.getName());
        commentService.removeComment(boardId, commentId, memberId);
        return ResponseEntity.accepted().body("댓글이 성공적으로 삭제되었습니다. commentId: " + commentId);
    }

    @Operation(summary = "좋아요 생성", description = "게시글에 좋아요를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "좋아요 생성 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @PostMapping("/{boardId}/hearts")
    public ResponseEntity<String> createHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        heartService.addHeart(boardId, memberId);
        return ResponseEntity.status(HttpStatus.CREATED).body("좋아요가 성공적으로 생성되었습니다.");
    }

    @Operation(summary = "좋아요 삭제", description = "게시글의 좋아요를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "좋아요 삭제 성공", content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class))),
            @ApiResponse(responseCode = "500", description = "서버 에러", content = @Content(schema = @Schema(implementation = ErrorResponseDTO.class)))
    })
    @DeleteMapping("/{boardId}/hearts")
    public ResponseEntity<String> deleteHeart(Principal principal, @PathVariable Long boardId) {
        Long memberId = Long.parseLong(principal.getName());
        heartService.removeHeart(boardId, memberId);
        return ResponseEntity.accepted().body("좋아요가 성공적으로 삭제되었습니다.");
    }
}