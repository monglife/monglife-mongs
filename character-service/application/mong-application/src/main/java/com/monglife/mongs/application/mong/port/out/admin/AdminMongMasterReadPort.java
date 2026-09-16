package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMasterCommand;
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

    /** 공통 코드(monglife_comn_code) 존재 여부 */
    Boolean isExistsComnCodePort(String code);

    /**
     * 마스터 데이터 등록. 공통 코드가 없으면 종류가 정한 그룹 코드와 함께 만들어 붙인다.
     * 랜덤 뽑기만 예외로, 이미 있는 음식·간식·맵 코드를 참조하므로 새로 만들지 않는다.
     */
    void createMongTypePort(AdminCreateMasterCommand command);

    void createFoodPort(AdminCreateMasterCommand command);

    void createSnackPort(AdminCreateMasterCommand command);

    void createTrainingTypePort(AdminCreateMasterCommand command);

    void createRandomDrawPort(AdminCreateMasterCommand command);
}
