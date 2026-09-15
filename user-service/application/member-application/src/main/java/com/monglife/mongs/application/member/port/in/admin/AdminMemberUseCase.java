package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminAdjustStarPointCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetMembersCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateSlotCountCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.CollectionMap;
import com.monglife.mongs.domain.member.model.CollectionMong;
import com.monglife.mongs.domain.member.model.Player;

import java.util.List;

public interface AdminMemberUseCase {

    AdminPageVo<AdminMemberVo> getMembersUseCase(AdminGetMembersCommand command);

    AdminMemberVo getMemberUseCase(Long accountId);

    /** 스타 포인트 가감. 변경 후 앱에 MQTT 로 즉시 반영한다 */
    Player adjustStarPointUseCase(AdminAdjustStarPointCommand command);

    /** 슬롯 수 지정. 변경 후 앱에 MQTT 로 즉시 반영한다 */
    Player updateSlotCountUseCase(AdminUpdateSlotCountCommand command);

    List<CollectionMap> getCollectionMapsUseCase(Long accountId);

    List<CollectionMong> getCollectionMongsUseCase(Long accountId);

    /** 컬렉션 맵 수동 지급. 이미 보유 중이면 그대로 둔다 */
    List<CollectionMap> grantCollectionMapUseCase(Long accountId, String mapCode);

    /** 컬렉션 몽 수동 지급. 이미 보유 중이면 그대로 둔다 */
    List<CollectionMong> grantCollectionMongUseCase(Long accountId, String mongCode);
}
