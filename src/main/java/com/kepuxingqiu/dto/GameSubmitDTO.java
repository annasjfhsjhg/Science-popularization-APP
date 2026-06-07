package com.kepuxingqiu.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GameSubmitDTO {
    @NotNull(message = "关卡ID不能为空")
    private Long gameContentId;

    @Min(value = 0, message = "得分不能为负数")
    private Integer score;

    /** true=通关成功，false=未完成 */
    private Boolean completed;
}
