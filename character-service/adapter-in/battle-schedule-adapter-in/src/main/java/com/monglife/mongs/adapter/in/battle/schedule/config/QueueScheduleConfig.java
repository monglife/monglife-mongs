package com.monglife.mongs.adapter.in.battle.schedule.config;

import com.monglife.mongs.adapter.in.battle.schedule.worker.MatchScheduleWorker;
import com.monglife.mongs.adapter.in.battle.schedule.worker.QueueScheduleWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class QueueScheduleConfig implements SchedulingConfigurer {

    private static final Long FIXED_DELAY = 1000L;

    /** 입장 기한 확인 주기. 기한이 30초라 이 정도면 충분하고, 매칭만큼 자주 돌 이유가 없다 */
    private static final Long MATCH_EXPIRE_FIXED_DELAY = 5000L;

    private final ScheduledExecutorService scheduledExecutorService;

    private final QueueScheduleWorker queueScheduleWorker;

    private final MatchScheduleWorker matchScheduleWorker;

    public QueueScheduleConfig(
            @Autowired QueueScheduleWorker queueScheduleWorker,
            @Autowired MatchScheduleWorker matchScheduleWorker,
            @Qualifier("battleScheduledExecutorService") ScheduledExecutorService scheduledExecutorService
    ) {
        this.queueScheduleWorker = queueScheduleWorker;
        this.matchScheduleWorker = matchScheduleWorker;
        this.scheduledExecutorService = scheduledExecutorService;
    }

    /**
     * 매칭 스케줄, 입장 기한 스케줄
     */
    @Override
    public void configureTasks(ScheduledTaskRegistrar registrar) {
        registrar.addFixedDelayTask(queueScheduleWorker::doMatching, Duration.ofMillis(FIXED_DELAY));
        registrar.addFixedDelayTask(matchScheduleWorker::doExpireEnteringMatches, Duration.ofMillis(MATCH_EXPIRE_FIXED_DELAY));
        registrar.setScheduler(scheduledExecutorService);
    }
}
