package com.kepuxingqiu.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDTO {
    @Size(max = 20, message = "昵称最多20个字符")
    private String nickname;

    private String avatarUrl;
}
