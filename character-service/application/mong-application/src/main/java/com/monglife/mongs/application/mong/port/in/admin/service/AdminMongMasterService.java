package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.application.mong.port.exception.AlreadyExistsMasterCodeException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMasterCodeException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMasterException;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongMasterUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminFeedItemVo;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminMongTypeVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongMasterReadPort;
import com.monglife.mongs.common.admin.log.AdminAuditLog;
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

    /**
     * 마스터 데이터 등록.
     *
     * <p>랜덤 뽑기는 이미 있는 음식·간식·맵 코드를 뽑기 풀에 한 줄 더 얹는 것이라
     * <b>같은 코드가 여러 번 들어가는 것이 정상</b>이다(중복 비율이 곧 확률). 나머지 종류는
     * 공통 코드를 새로 만들므로 이미 있으면 막는다.
     */
    @Override
    @Transactional
    public void createMasterUseCase(Kind kind, AdminCreateMasterCommand command) {

        if (kind == Kind.RANDOM_DRAW) {
            // 없는 코드는 뽑기 풀에 넣을 수 없다
            if (!Boolean.TRUE.equals(adminMongMasterReadPort.isExistsComnCodePort(command.getCode()))) {
                throw new NotExistsMasterCodeException();
            }
        } else if (Boolean.TRUE.equals(isExistsInKind(kind, command.getCode()))) {
            throw new AlreadyExistsMasterCodeException();
        }

        AdminAuditLog.write("master created kind={} code={} name={}", kind, command.getCode(), command.getName());

        switch (kind) {
            case MONG_TYPE -> adminMongMasterReadPort.createMongTypePort(command);
            case FOOD -> adminMongMasterReadPort.createFoodPort(command);
            case SNACK -> adminMongMasterReadPort.createSnackPort(command);
            case TRAINING_TYPE -> adminMongMasterReadPort.createTrainingTypePort(command);
            case RANDOM_DRAW -> adminMongMasterReadPort.createRandomDrawPort(command);
        }
    }

    /**
     * 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 —
     * 인벤토리·뽑기 이력 등이 코드를 참조하고 있어 함께 지우면 그 행들이 깨진다.
     */
    @Override
    @Transactional
    public void deleteMasterUseCase(Kind kind, Long id) {

        boolean deleted = switch (kind) {
            case MONG_TYPE -> adminMongMasterReadPort.deleteMongTypePort(id);
            case FOOD -> adminMongMasterReadPort.deleteFoodPort(id);
            case SNACK -> adminMongMasterReadPort.deleteSnackPort(id);
            case TRAINING_TYPE -> adminMongMasterReadPort.deleteTrainingTypePort(id);
            case RANDOM_DRAW -> adminMongMasterReadPort.deleteRandomDrawPort(id);
        };

        if (!deleted) throw new NotExistsMasterException();

        AdminAuditLog.write("master deleted kind={} id={}", kind, id);
    }

    /** 같은 종류에 같은 코드가 이미 있는지 */
    private Boolean isExistsInKind(Kind kind, String code) {
        return switch (kind) {
            case MONG_TYPE -> adminMongMasterReadPort.isExistsMongTypePort(code);
            case FOOD -> adminMongMasterReadPort.isExistsFoodPort(code);
            case SNACK -> adminMongMasterReadPort.isExistsSnackPort(code);
            case TRAINING_TYPE -> adminMongMasterReadPort.isExistsTrainingTypePort(code);
            case RANDOM_DRAW -> false;
        };
    }
}
