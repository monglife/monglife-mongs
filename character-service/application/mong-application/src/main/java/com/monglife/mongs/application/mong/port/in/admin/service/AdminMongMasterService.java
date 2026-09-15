package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.in.admin.AdminMongMasterUseCase;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongMasterReadPort;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMongMasterService implements AdminMongMasterUseCase {

    private final AdminMongMasterReadPort adminMongMasterReadPort;

    @Override
    @Transactional
    public List<AdminMongTypeVo> getMongTypesUseCase() {
        return adminMongMasterReadPort.getMongTypesPort();
    }

    @Override
    @Transactional
    public List<AdminFeedItemVo> getFoodsUseCase() {
        return adminMongMasterReadPort.getFoodsPort();
    }

    @Override
    @Transactional
    public List<AdminFeedItemVo> getSnacksUseCase() {
        return adminMongMasterReadPort.getSnacksPort();
    }

    @Override
    @Transactional
    public List<TrainingType> getTrainingTypesUseCase() {
        return adminMongMasterReadPort.getTrainingTypesPort();
    }

    @Override
    @Transactional
    public List<RandomDraw> getRandomDrawsUseCase() {
        return adminMongMasterReadPort.getRandomDrawsPort();
    }
}
