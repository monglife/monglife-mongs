package com.monglife.mongs.application.mong.port.in.admin.service;

import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.application.mong.port.annotation.PublishMongPort;
import com.monglife.mongs.application.mong.port.enums.MongSchedulerType;
import com.monglife.mongs.application.mong.port.exception.InvalidCreateInventoryItemException;
import com.monglife.mongs.application.mong.port.exception.InvalidCreateMongScheduleException;
import com.monglife.mongs.application.mong.port.exception.NotExistsMongException;
import com.monglife.mongs.application.mong.port.exception.AlreadyExistsTaskException;
import com.monglife.mongs.application.mong.port.exception.NotExistsTaskException;
import com.monglife.mongs.application.mong.port.in.ManagementUseCase;
import com.monglife.mongs.application.mong.port.in.admin.AdminMongUseCase;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminGetMongsCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongSleepCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStateCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStatusCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import com.monglife.mongs.application.mong.port.in.command.DeleteMongCommand;
import com.monglife.mongs.application.mong.port.in.command.SleepMongCommand;
import com.monglife.mongs.application.mong.port.in.command.WakeupMongCommand;
import com.monglife.mongs.application.mong.port.out.MongPersistencePort;
import com.monglife.mongs.application.mong.port.out.MongReadPort;
import com.monglife.mongs.application.mong.port.out.MongSchedulerPort;
import com.monglife.mongs.application.mong.port.out.vo.CreateInventoryVo;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongReadPort;
import com.monglife.mongs.application.mong.port.out.admin.AdminMongSchedulerPort;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import com.monglife.mongs.domain.mong.model.Mong;
import com.monglife.mongs.domain.mong.model.MongEvolutionHistory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMongService implements AdminMongUseCase {

    private final AdminMongReadPort adminMongReadPort;

    private final AdminMongSchedulerPort adminMongSchedulerPort;

    private final MongReadPort mongReadPort;

    private final MongPersistencePort mongPersistencePort;

    private final MongSchedulerPort mongSchedulerPort;

    private final ManagementUseCase managementUseCase;

    @Override
    @Transactional
    public AdminPageVo<Mong> getMongsUseCase(AdminGetMongsCommand command) {
        return adminMongReadPort.getMongsPort(command.getPageRequest(), command.getAccountId(), command.getStateCode(), command.getStatusCode(), command.getQuery());
    }

    @Override
    @Transactional
    public Mong getMongUseCase(Long mongId) {
        return mongReadPort.getMongPort(mongId)
                .orElseThrow(NotExistsMongException::new);
    }

    /**
     * 지수 수정. {@code @PublishMongPort} 가 앱에 MQTT 로 반영한다.
     * 사망 스케줄은 기존 {@code @CheckMongDead} 와 같은 규칙(포만감·체력 0 이면 걸고 아니면 푼다)을
     * 살아 있는 몽에만 적용한다 — 이미 DEAD 인 몽에 사망 스케줄을 다시 걸면 만료 시 예외만 남긴다.
     */
    @Override
    @Transactional
    @PublishMongPort
    public Mong updateMongStatusUseCase(AdminUpdateMongStatusCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new);

        mong.adminUpdateStatus(
                command.getWeight(), command.getStrength(), command.getSatiety(), command.getHealthy(), command.getFatigue(),
                command.getExp(), command.getPayPoint(), command.getPoopCount(), command.getRandomDrawTicketCount());

        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        if (!MongStateCode.DEAD.equals(mong.getStateCode()) && !MongStateCode.GRADUATE.equals(mong.getStateCode())) {
            if (mong.getSatiety() == 0D || mong.getHealthy() == 0D) {
                mongSchedulerPort.createTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.DEAD);
            } else {
                mongSchedulerPort.deleteTaskPort(mong.getMongId(), MongSchedulerType.DEAD);
            }
        }

        AdminAuditLog.write("mong status updated mongId={} accountId={} mong={}",
                mong.getMongId(), mong.getAccountId(), mong);

        return mong;
    }

    /**
     * 상태 코드 강제 변경. DEAD/GRADUATE 로 보내면 스케줄을 전부 지우고,
     * 그 상태에서 되살리면 수면 여부·레벨에 맞춰 스케줄을 다시 건다.
     */
    @Override
    @Transactional
    @PublishMongPort
    public Mong updateMongStateUseCase(AdminUpdateMongStateCommand command) {

        Mong mong = mongPersistencePort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new);

        MongStateCode before = mong.getStateCode();
        MongStateCode after = command.getStateCode();

        mong.adminUpdateStateCode(after);

        mongPersistencePort.saveMongPort(mong)
                .orElseThrow(NotExistsMongException::new);

        boolean wasTerminal = MongStateCode.DEAD.equals(before) || MongStateCode.GRADUATE.equals(before);
        boolean isTerminal = MongStateCode.DEAD.equals(after) || MongStateCode.GRADUATE.equals(after);

        if (isTerminal) {
            mongSchedulerPort.deleteAllTaskPort(mong.getMongId());
        } else if (wasTerminal) {
            this.restoreTasks(mong);
        }

        AdminAuditLog.write("mong state updated mongId={} accountId={} before={} after={}",
                mong.getMongId(), mong.getAccountId(), before, after);

        return mong;
    }

    /**
     * 되살린 몽의 스케줄 복구. 기존 진화/기상/수면 유스케이스가 거는 조합과 같다.
     */
    private void restoreTasks(Mong mong) {

        if (mong.getLevel() == 0) {
            mongSchedulerPort.createTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.EGG_EVOLUTION)
                    .orElseThrow(InvalidCreateMongScheduleException::new);
            return;
        }

        if (Boolean.TRUE.equals(mong.getIsSleep())) {
            mongSchedulerPort.createCycleTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.INCREASE_STATUS)
                    .orElseThrow(InvalidCreateMongScheduleException::new);
        } else {
            mongSchedulerPort.createCycleTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.DECREASE_STATUS)
                    .orElseThrow(InvalidCreateMongScheduleException::new);
            mongSchedulerPort.createCycleTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.INCREASE_POOP)
                    .orElseThrow(InvalidCreateMongScheduleException::new);
        }

        mongSchedulerPort.createFixedTimeCycleTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.SLEEP, mong.getSleepAt())
                .orElseThrow(InvalidCreateMongScheduleException::new);
        mongSchedulerPort.createFixedTimeCycleTaskPort(mong.getMongId(), mong.getAccountId(), MongSchedulerType.WAKEUP, mong.getWakeupAt())
                .orElseThrow(InvalidCreateMongScheduleException::new);
    }

    /**
     * 수면·기상 전환. 상태 검증(사망·알 단계·이미 그 상태)과 스케줄 교체가 전부 기존
     * 유스케이스 안에 있으므로 그대로 위임한다. 소유자 검증을 통과시키려고 몽의 계정 ID 를 넘긴다.
     */
    @Override
    @Transactional
    public Mong updateMongSleepUseCase(AdminUpdateMongSleepCommand command) {

        Mong mong = mongReadPort.getMongPort(command.getMongId())
                .orElseThrow(NotExistsMongException::new);

        AdminAuditLog.write("mong sleep changed mongId={} accountId={} isSleep={}",
                mong.getMongId(), mong.getAccountId(), command.getIsSleep());

        if (Boolean.TRUE.equals(command.getIsSleep())) {
            return managementUseCase.sleepMongUseCase(SleepMongCommand.builder()
                    .mongId(mong.getMongId())
                    .accountId(mong.getAccountId())
                    .build());
        }

        return managementUseCase.wakeUpMongUseCase(WakeupMongCommand.builder()
                .mongId(mong.getMongId())
                .accountId(mong.getAccountId())
                .build());
    }

    @Override
    @Transactional
    public Mong deleteMongUseCase(Long mongId) {

        Mong mong = mongReadPort.getMongPort(mongId)
                .orElseThrow(NotExistsMongException::new);

        AdminAuditLog.write("mong deleted mongId={} accountId={}", mong.getMongId(), mong.getAccountId());

        // 소유자 검증을 통과시키려고 몽의 계정 ID 를 그대로 넘긴다
        return managementUseCase.deleteMongUseCase(DeleteMongCommand.builder()
                .mongId(mong.getMongId())
                .accountId(mong.getAccountId())
                .build());
    }

    @Override
    @Transactional
    public List<AdminTaskVo> getTasksUseCase(Long mongId) {
        return adminMongSchedulerPort.getTasksPort(mongId);
    }

    @Override
    @Transactional
    public AdminTaskVo pauseTaskUseCase(Long taskId) {
        AdminAuditLog.write("task paused taskId={}", taskId);
        return adminMongSchedulerPort.pauseTaskPort(taskId)
                .orElseThrow(NotExistsTaskException::new);
    }

    @Override
    @Transactional
    public AdminTaskVo resumeTaskUseCase(Long taskId) {
        AdminAuditLog.write("task resumed taskId={}", taskId);
        return adminMongSchedulerPort.resumeTaskPort(taskId)
                .orElseThrow(NotExistsTaskException::new);
    }

    @Override
    @Transactional
    public AdminTaskVo deleteTaskUseCase(Long taskId) {
        AdminTaskVo deleted = adminMongSchedulerPort.deleteTaskPort(taskId)
                .orElseThrow(NotExistsTaskException::new);
        AdminAuditLog.write("task deleted taskId={} mongId={} type={}",
                taskId, deleted.getMongId(), deleted.getSchedulerTypeCode());
        return deleted;
    }

    /**
     * 스케줄 등록.
     *
     * <p>같은 타입이 이미 있으면 막는다. 등록 경로가 기존 행을 재사용하는데, 그 행이
     * 일시중지 상태면 {@code expiredAt} 이 null 이라 타이머를 걸다 NPE 로 트랜잭션이 통째로 깨진다.
     */
    @Override
    @Transactional
    public AdminTaskVo createTaskUseCase(Long mongId, MongSchedulerType schedulerType) {

        Mong mong = mongReadPort.getMongPort(mongId).orElseThrow(NotExistsMongException::new);

        if (Boolean.TRUE.equals(adminMongSchedulerPort.isExistsTaskPort(mongId, schedulerType))) {
            throw new AlreadyExistsTaskException();
        }

        // 수면·기상은 몽에 저장된 시각을 쓴다. 관리자가 따로 넣게 하면 몽 설정과 어긋난다.
        LocalTime fixTime = switch (schedulerType) {
            case SLEEP -> mong.getSleepAt();
            case WAKEUP -> mong.getWakeupAt();
            default -> null;
        };

        AdminTaskVo created = adminMongSchedulerPort.createTaskPort(mongId, mong.getAccountId(), schedulerType, fixTime)
                .orElseThrow(InvalidCreateMongScheduleException::new);

        AdminAuditLog.write("task created taskId={} mongId={} type={} fixTime={}",
                created.getTaskId(), mongId, schedulerType, fixTime);

        return created;
    }

    @Override
    @Transactional
    public List<MongEvolutionHistory> getEvolutionHistoriesUseCase(Long accountId) {
        return mongReadPort.getMongEvolutionHistoriesPort(accountId);
    }

    @Override
    @Transactional
    public AdminPageVo<Inventory> getInventoriesUseCase(AdminPageRequestVo pageRequest, Long mongId) {
        return adminMongReadPort.getInventoriesPort(pageRequest, mongId);
    }

    /**
     * 인벤토리 아이템 수동 지급. 없는 공통 코드면 등록에 실패한다.
     */
    @Override
    @Transactional
    public Inventory grantInventoryUseCase(Long mongId, String inventoryCode, InventoryTypeCode inventoryTypeCode) {

        Mong mong = mongReadPort.getMongPort(mongId)
                .orElseThrow(NotExistsMongException::new);

        AdminAuditLog.write("inventory granted mongId={} accountId={} code={} type={}", mongId, mong.getAccountId(), inventoryCode, inventoryTypeCode);

        return mongPersistencePort.createInventoryPort(CreateInventoryVo.builder()
                        .mongId(mongId)
                        .inventoryCode(inventoryCode)
                        .inventoryTypeCode(inventoryTypeCode)
                        .build())
                .orElseThrow(InvalidCreateInventoryItemException::new);
    }
}
