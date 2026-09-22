package com.monglife.mongs.application.mong.port.in.service;

import com.monglife.mongs.application.mong.port.annotation.CheckMongDead;
import com.monglife.mongs.application.mong.port.annotation.MissionProgress;
import com.monglife.mongs.application.mong.port.annotation.PublishMongPort;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.exception.NotExistsTrainingTypeException;
import com.monglife.mongs.application.mong.port.in.ActivityUseCase;
import com.monglife.mongs.application.mong.port.in.MissionUseCase;
import com.monglife.mongs.application.mong.port.in.command.IncreaseMissionProgressCommand;
import com.monglife.mongs.domain.mission.enums.MissionActionCode;
import com.monglife.mongs.application.mong.port.in.command.GetTrainingTypeCommand;
import com.monglife.mongs.application.mong.port.in.command.TrainingEndCommand;
import com.monglife.mongs.application.mong.port.in.vo.TrainingEndVo;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.domain.mong.model.Mong;
import com.monglife.mongs.domain.mong.model.TrainingType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService  implements ActivityUseCase {

    private final MongPersistencePort mongPersistencePort;

    private final MongReadPort mongReadPort;

    private final MissionUseCase missionUseCase;

    /**
     * 훈련 타입 목록 조회
     */
    @Override
    @Transactional
    public List<TrainingType> getTrainingTypesUseCase() {
        return mongReadPort.getTrainingTypesPort();
    }

    /**
     * 훈련 타입 조회
     */
    @Override
    @Transactional
    public TrainingType getTrainingTypeUseCase(GetTrainingTypeCommand command) {
        return mongReadPort.getTrainingTypePort(command.getTrainingCode())
                .orElseThrow(NotExistsTrainingTypeException::new);
    }

    /**
     * 훈련 완료
     */
    @Override
    @Transactional
    @CheckMongDead
    @PublishMongPort
    @MissionProgress(value = MissionActionCode.TRAINING_END, detailField = "trainingCode")
    public TrainingEndVo trainingEndUseCase(TrainingEndCommand command) {

        // 훈련 타입 조회
        TrainingType trainingType = mongReadPort.getTrainingTypePort(command.getTrainingCode())
                .orElseThrow(NotExistsTrainingTypeException::new);

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new)
                .verify(command.getAccountId());

        // 스코어 달성 시 페이 포인트 증가
        boolean isSuccess = trainingType.getScore() <= command.getScore();

        double expBeforeTraining = mong.getExp();
        int payPointBeforeTraining = mong.getPayPoint();

        if (isSuccess) {
            mong.trainingWithReward(trainingType);
        } else {
            mong.training(trainingType);
        }

        // 누적 미션. 훈련 타입의 정의값이 아니라 실제 반영된 증가분을 쓴다
        // (경험치는 maxStatus 에서 잘리므로 정의값과 다를 수 있다)
        this.accumulate(command.getAccountId(), MissionActionCode.EXP_EARN,
                (int) Math.round(mong.getExp() - expBeforeTraining));
        this.accumulate(command.getAccountId(), MissionActionCode.PAY_POINT_EARN,
                mong.getPayPoint() - payPointBeforeTraining);
        this.accumulate(command.getAccountId(), MissionActionCode.TRAINING_SCORE, command.getScore());

        // 몽 정보 동기화
        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        return TrainingEndVo.builder()
                .isSuccess(isSuccess)
                .rewardPayPoint(trainingType.getPayPoint())
                .score(command.getScore())
                .mong(mong)
                .build();
    }

    /**
     * 누적 미션 반영. 0 이하면 부를 필요가 없다
     */
    private void accumulate(Long accountId, MissionActionCode actionCode, int amount) {

        if (amount <= 0) {
            return;
        }

        missionUseCase.increaseMissionProgressUseCase(IncreaseMissionProgressCommand.builder()
                .accountId(accountId)
                .actionCode(actionCode)
                .amount(amount)
                .build());
    }
}
