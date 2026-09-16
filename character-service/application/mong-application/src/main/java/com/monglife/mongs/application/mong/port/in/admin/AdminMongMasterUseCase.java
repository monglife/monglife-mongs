package com.monglife.mongs.application.mong.port.in.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.domain.mong.model.RandomDraw;
import com.monglife.mongs.domain.mong.model.TrainingType;

import java.util.List;

public interface AdminMongMasterUseCase {

    /** 등록 가능한 마스터 데이터 종류 */
    enum Kind {
        MONG_TYPE,
        FOOD,
        SNACK,
        TRAINING_TYPE,
        RANDOM_DRAW,
    }

    List<AdminMongTypeVo> getMongTypesUseCase();

    List<AdminFeedItemVo> getFoodsUseCase();

    List<AdminFeedItemVo> getSnacksUseCase();

    List<TrainingType> getTrainingTypesUseCase();

    List<RandomDraw> getRandomDrawsUseCase();

    /** 마스터 데이터 등록 */
    void createMasterUseCase(Kind kind, AdminCreateMasterCommand command);
}
