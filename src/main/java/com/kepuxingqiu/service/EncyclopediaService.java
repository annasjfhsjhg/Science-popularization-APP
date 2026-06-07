package com.kepuxingqiu.service;

import com.kepuxingqiu.vo.EncyclopediaVO;

import java.util.List;

public interface EncyclopediaService {
    List<EncyclopediaVO> list(Long userId, String category);
    EncyclopediaVO getDetail(Long userId, Long id);
}
