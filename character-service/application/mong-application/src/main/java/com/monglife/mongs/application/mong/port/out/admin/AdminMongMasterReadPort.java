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
     * 같은 종류에 같은 코드가 이미 있는지. 중복 판정은 공통 코드가 아니라 <b>종류별</b>로 한다 —
     * 지웠다가 같은 코드로 다시 넣을 수 있어야 하고, 공통 코드는 삭제해도 남기기 때문이다.
     */
    Boolean isExistsMongTypePort(String code);

    Boolean isExistsFoodPort(String code);

    Boolean isExistsSnackPort(String code);

    Boolean isExistsTrainingTypePort(String code);

    /**
     * 마스터 데이터 등록. 공통 코드가 없으면 종류가 정한 그룹 코드와 함께 만들어 붙이고,
     * 이미 있으면 그대로 쓴다. 랜덤 뽑기는 이미 있는 코드를 참조하므로 새로 만들지 않는다.
     */
    void createMongTypePort(AdminCreateMasterCommand command);

    void createFoodPort(AdminCreateMasterCommand command);

    void createSnackPort(AdminCreateMasterCommand command);

    void createTrainingTypePort(AdminCreateMasterCommand command);

    void createRandomDrawPort(AdminCreateMasterCommand command);

    /**
     * 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 —
     * 인벤토리·뽑기 이력 등이 코드를 참조하고 있어 함께 지우면 그 행들이 깨진다.
     */
    Boolean deleteMongTypePort(Long id);

    Boolean deleteFoodPort(Long id);

    Boolean deleteSnackPort(Long id);

    Boolean deleteTrainingTypePort(Long id);

    Boolean deleteRandomDrawPort(Long id);
}
