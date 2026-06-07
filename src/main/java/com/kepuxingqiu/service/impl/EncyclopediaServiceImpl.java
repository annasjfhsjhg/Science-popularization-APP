package com.kepuxingqiu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kepuxingqiu.common.exception.BusinessException;
import com.kepuxingqiu.common.result.ResultCode;
import com.kepuxingqiu.entity.Encyclopedia;
import com.kepuxingqiu.entity.UserEncyclopedia;
import com.kepuxingqiu.mapper.EncyclopediaMapper;
import com.kepuxingqiu.mapper.UserEncyclopediaMapper;
import com.kepuxingqiu.service.EncyclopediaService;
import com.kepuxingqiu.vo.EncyclopediaVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EncyclopediaServiceImpl implements EncyclopediaService {

    private final EncyclopediaMapper encyclopediaMapper;
    private final UserEncyclopediaMapper userEncMapper;

    public EncyclopediaServiceImpl(EncyclopediaMapper encyclopediaMapper, UserEncyclopediaMapper userEncMapper) {
        this.encyclopediaMapper = encyclopediaMapper;
        this.userEncMapper = userEncMapper;
    }

    @Override
    public List<EncyclopediaVO> list(Long userId, String category) {
        LambdaQueryWrapper<Encyclopedia> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category) && !"all".equals(category)) {
            wrapper.eq(Encyclopedia::getCategory, category);
        }
        List<Encyclopedia> encList = encyclopediaMapper.selectList(wrapper);

        Set<Long> unlockedIds = userEncMapper.selectList(
                new LambdaQueryWrapper<UserEncyclopedia>().eq(UserEncyclopedia::getUserId, userId))
                .stream().map(UserEncyclopedia::getEncyclopediaId).collect(Collectors.toSet());

        return encList.stream().map(enc -> toVO(enc, unlockedIds.contains(enc.getId()))).collect(Collectors.toList());
    }

    @Override
    public EncyclopediaVO getDetail(Long userId, Long id) {
        Encyclopedia enc = encyclopediaMapper.selectById(id);
        if (enc == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "图鉴不存在");
        }
        boolean unlocked = userEncMapper.selectCount(
                new LambdaQueryWrapper<UserEncyclopedia>()
                        .eq(UserEncyclopedia::getUserId, userId)
                        .eq(UserEncyclopedia::getEncyclopediaId, id)) > 0;
        return toVO(enc, unlocked);
    }

    private EncyclopediaVO toVO(Encyclopedia enc, boolean unlocked) {
        EncyclopediaVO vo = new EncyclopediaVO();
        vo.setId(enc.getId());
        vo.setCategory(enc.getCategory());
        vo.setName(enc.getName());
        vo.setImageUrl(enc.getImageUrl());
        vo.setUnlockCondition(enc.getUnlockCondition());
        vo.setUnlocked(unlocked);
        // 未解锁时不返回详细描述
        vo.setDescription(unlocked ? enc.getDescription() : null);
        return vo;
    }
}
