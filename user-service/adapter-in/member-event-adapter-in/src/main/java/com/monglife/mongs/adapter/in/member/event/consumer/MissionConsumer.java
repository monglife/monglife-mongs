package com.monglife.mongs.adapter.in.member.event.consumer;

import com.monglife.module.common.kafka.event.TransactionEvent;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.transaction.MissionRewardStarPointEventDto;
import com.monglife.mongs.application.member.port.in.PlayerUseCase;
import com.monglife.mongs.application.member.port.in.command.IncreaseStarPointCommand;
import com.monglife.mongs.core.kafka.event.enums.EventTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 미션 리워드 중 스타 포인트만 이쪽으로 넘어온다.
 *
 * <p>경험치·페이 포인트·인벤토리는 몽 소유라 character-service 안에서 끝나고,
 * 스타 포인트만 Player 소유라 서비스를 넘는다. 랜덤 맵 뽑기와 같은 단방향 이벤트다 -
 * 롤백 토픽이 없다. 지급이 실패하면 미션은 이미 수령 처리된 채로 남으므로
 * 재지급은 관리자 스타 포인트 조정으로 해결한다.
 */
@Component
@RequiredArgsConstructor
public class MissionConsumer {

    private final PlayerUseCase playerUseCase;

    /**
     * 미션 리워드 스타 포인트 지급 이벤트 리스너
     * @param event 미션 리워드 스타 포인트 정보
     */
    @EntryLoggingPoint
    @KafkaListener(topics = "${spring.config.activate.on-profile}." + EventTopic.COMMIT_MISSION_REWARD_STAR_POINT)
    public void missionRewardStarPointEvent(TransactionEvent<MissionRewardStarPointEventDto> event) {

        if (event.getData() != null) {
            IncreaseStarPointCommand command = IncreaseStarPointCommand.builder()
                    .accountId(event.getData().getAccountId())
                    .starPoint(event.getData().getStarPoint())
                    .build();

            playerUseCase.increaseStarPointUseCase(command);
        }
    }
}
