package com.kepuxingqiu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GameResultDTO {
    @NotBlank(message = "gameType不能为空")
    private String gameType;
    private Integer score;
    private Integer timeUsed;
    /** 星级评分 1-3 */
    private Integer stars;
}
