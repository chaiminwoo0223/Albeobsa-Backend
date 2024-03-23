package skhu.jijijig.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponseDTO(
        @Schema(description = "오류 메시지")
        String message,

        @Schema(description = "HTTP 상태 코드")
        int status
) {}