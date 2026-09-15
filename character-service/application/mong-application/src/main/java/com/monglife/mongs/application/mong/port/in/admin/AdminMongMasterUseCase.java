package com.monglife.mongs.application.mong.port.in.admin;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;

import java.util.List;

public interface AdminMongMasterUseCase {

    List<AdminMongTypeVo> getMongTypesUseCase();

    List<AdminFeedItemVo> getFoodsUseCase();

    List<AdminFeedItemVo> getSnacksUseCase();

    List<TrainingType> getTrainingTypesUseCase();

    List<RandomDraw> getRandomDrawsUseCase();
}
