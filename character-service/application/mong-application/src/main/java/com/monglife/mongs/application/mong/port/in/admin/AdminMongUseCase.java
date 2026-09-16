package com.monglife.mongs.application.mong.port.in.admin;

import com.monglife.core.vo.page.PageResult;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminGetMongsCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongSleepCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStateCommand;
import com.monglife.mongs.application.mong.port.in.admin.command.AdminUpdateMongStatusCommand;
import com.monglife.mongs.application.mong.port.in.admin.vo.AdminTaskVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.InventoryTypeCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import com.monglife.mongs.domain.mong.model.Mong;
import com.monglife.mongs.domain.mong.model.MongEvolutionHistory;

import java.util.List;

public interface AdminMongUseCase {

    AdminPageVo<Mong> getMongsUseCase(AdminGetMongsCommand command);

    Mong getMongUseCase(Long mongId);

    /** 지수 수정. 변경 후 앱에 MQTT 로 즉시 반영하고, 포만감·체력 0 이면 사망 스케줄이 걸린다 */
    Mong updateMongStatusUseCase(AdminUpdateMongStatusCommand command);

    /** 상태 코드 강제 변경(DEAD 복구 등). 상태에 맞춰 스케줄을 다시 건다 */
    Mong updateMongStateUseCase(AdminUpdateMongStateCommand command);

    /**
     * 수면·기상 전환. 기존 수면/기상 유스케이스를 그대로 태운다 —
     * 지수 증감 스케줄 교체와 MQTT 반영이 앱에서 누른 것과 같아야 한다.
     */
    Mong updateMongSleepUseCase(AdminUpdateMongSleepCommand command);

    /** 몽 삭제. 기존 삭제 유스케이스(스케줄 정리 포함)를 그대로 태운다 */
    Mong deleteMongUseCase(Long mongId);

    List<AdminTaskVo> getTasksUseCase(Long mongId);

    AdminTaskVo pauseTaskUseCase(Long taskId);

    AdminTaskVo resumeTaskUseCase(Long taskId);

    List<MongEvolutionHistory> getEvolutionHistoriesUseCase(Long accountId);

    PageResult<Inventory> getInventoriesUseCase(Long mongId, Integer page, Integer size);

    /** 인벤토리 아이템 수동 지급 (FOOD/SNACK) */
    Inventory grantInventoryUseCase(Long mongId, String inventoryCode, InventoryTypeCode inventoryTypeCode);
}
