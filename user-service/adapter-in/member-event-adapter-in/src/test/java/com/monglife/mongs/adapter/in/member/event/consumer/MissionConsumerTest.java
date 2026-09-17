package com.monglife.mongs.adapter.in.member.event.consumer;

import com.monglife.module.common.kafka.config.KafkaAutoConfig;
import com.monglife.module.common.kafka.service.KafkaService;
import com.monglife.mongs.adapter.transaction.MissionRewardStarPointEventDto;
import com.monglife.mongs.application.member.port.in.PlayerUseCase;
import com.monglife.mongs.application.member.port.in.command.IncreaseStarPointCommand;
import com.monglife.mongs.core.kafka.event.enums.EventTopic;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@EnableAutoConfiguration
@ActiveProfiles("test")
@ContextConfiguration(classes = { MissionConsumer.class, KafkaAutoConfig.class })
@ComponentScan({"com.monglife.module.common.kafka"})
@EmbeddedKafka(partitions = 1, bootstrapServersProperty = "module.kafka.url")
@DirtiesContext
class MissionConsumerTest {

    @MockBean
    private PlayerUseCase playerUseCase;

    private final KafkaService kafkaService;

    @Autowired
    public MissionConsumerTest(KafkaService kafkaService) {
        this.kafkaService = kafkaService;
    }

    @Nested
    @DisplayName("미션 리워드 스타 포인트 지급 이벤트 리스너 단위 테스트")
    class MissionRewardStarPointEvent {

        @Test
        @DisplayName("미션 리워드 이벤트를 소비하여 스타 포인트 증가 UseCase를 실행 한다.")
        void missionRewardStarPoint() {
            // arrange
            final String topic = EventTopic.COMMIT_MISSION_REWARD_STAR_POINT;
            final long accountId = 1L;
            final int starPoint = 3;
            final String missionCode = "MS_M_003";

            // act
            MissionRewardStarPointEventDto missionRewardStarPointEventDto = MissionRewardStarPointEventDto.builder()
                    .accountId(accountId)
                    .starPoint(starPoint)
                    .missionCode(missionCode)
                    .build();

            kafkaService.generateEventWithProfile(topic, missionRewardStarPointEventDto);

            // assert
            ArgumentCaptor<IncreaseStarPointCommand> captor = ArgumentCaptor.forClass(IncreaseStarPointCommand.class);

            Awaitility.await()
                    .atMost(Duration.ofSeconds(30))
                    .untilAsserted(() -> Mockito.verify(playerUseCase, Mockito.times(1))
                            .increaseStarPointUseCase(Mockito.any()));

            Mockito.verify(playerUseCase).increaseStarPointUseCase(captor.capture());

            var command = captor.getValue();
            assertEquals(accountId, command.getAccountId());
            assertEquals(starPoint, command.getStarPoint());
        }
    }
}
