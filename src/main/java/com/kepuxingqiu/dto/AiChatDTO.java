package com.kepuxingqiu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiChatDTO {

    @NotBlank(message = "消息不能为空")
    @Size(max = 500, message = "消息最多500字")
    private String message;

    /** GENERAL=科普问答, GAME_HINT=游戏提示, ENCYCLOPEDIA=图鉴解释 */
    private String contextType = "GENERAL";

    /** contextType != GENERAL 时传入对应ID */
    private Long contextId;
}
