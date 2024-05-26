package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import skhu.jijijig.domain.Comment;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    @Schema(description = "댓글 ID", example = "1")
    private Long id; // 댓글 ID 추가

    @Schema(description = "내용", example = "화이팅")
    private String content;

    @Schema(description = "작성자", example = "사용자명")
    private String memberName; // 댓글 작성자 이름 추가

    public static CommentDTO fromEntity(Comment comment) {
        return CommentDTO.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .memberName(comment.getMember().getName()) // 작성자 이름 설정
                .build();
    }
}