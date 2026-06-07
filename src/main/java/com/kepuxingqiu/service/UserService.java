package com.kepuxingqiu.service;

import com.kepuxingqiu.dto.LoginDTO;
import com.kepuxingqiu.dto.RegisterDTO;
import com.kepuxingqiu.dto.UpdateProfileDTO;
import com.kepuxingqiu.vo.LoginVO;
import com.kepuxingqiu.vo.UserInfoVO;
import com.kepuxingqiu.vo.UserProfileVO;

public interface UserService {
    void register(RegisterDTO dto);
    LoginVO login(LoginDTO dto);
    void logout(Long userId);
    UserInfoVO getUserInfo(Long userId);
    UserProfileVO getProfile(Long userId);
    void updateProfile(Long userId, UpdateProfileDTO dto);
    UserProfileVO getLearningReport(Long userId);
}
