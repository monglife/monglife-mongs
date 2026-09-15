package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;

import java.util.List;

public interface AdminMongMasterReadPort {

    List<AdminMongTypeVo> getMongTypesPort();

    List<AdminFeedItemVo> getFoodsPort();

    List<AdminFeedItemVo> getSnacksPort();

    List<TrainingType> getTrainingTypesPort();

    List<RandomDraw> getRandomDrawsPort();
}
