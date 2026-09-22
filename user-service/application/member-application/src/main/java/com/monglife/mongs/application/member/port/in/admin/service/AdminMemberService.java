package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.common.admin.log.AdminAuditLog;
import com.monglife.mongs.application.member.port.exception.InvalidAdminSlotCountException;
import com.monglife.mongs.application.member.port.exception.NotExistsPlayerException;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminAdjustStarPointCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetMembersCommand;
import com.monglife.mongs.application.member.port.in.admin.command.AdminUpdateSlotCountCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMemberVo;
import com.monglife.mongs.application.member.port.in.CollectionUseCase;
import com.monglife.mongs.application.member.port.in.command.CreateCollectionMapCommand;
import com.monglife.mongs.application.member.port.in.command.CreateCollectionMongCommand;
import com.monglife.mongs.application.member.port.out.CollectionReadPort;
import com.monglife.mongs.application.member.port.out.MemberPersistencePort;
import com.monglife.mongs.application.member.port.out.MemberPublishPort;
import com.monglife.mongs.application.member.port.out.admin.AdminMemberReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.CollectionMap;
import com.monglife.mongs.domain.member.model.CollectionMong;
import com.monglife.mongs.domain.member.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminMemberService implements AdminMemberUseCase {

    private final AdminMemberReadPort adminMemberReadPort;

    private final MemberPersistencePort memberPersistencePort;

    private final MemberPublishPort memberPublishPort;

    private final CollectionReadPort collectionReadPort;

    private final CollectionUseCase collectionUseCase;

    @Override
    @Transactional
    public AdminPageVo<AdminMemberVo> getMembersUseCase(AdminGetMembersCommand command) {
        return adminMemberReadPort.getMembersPort(command.getPageRequest(), command.getAccountId());
    }

    @Override
    @Transactional
    public AdminMemberVo getMemberUseCase(Long accountId) {
        return adminMemberReadPort.getMemberPort(accountId)
                .orElseThrow(NotExistsPlayerException::new);
    }

    /**
     * 스타 포인트 가감. 사유는 감사 로그로만 남긴다(별도 테이블 없음).
     */
    @Override
    @Transactional
    public Player adjustStarPointUseCase(AdminAdjustStarPointCommand command) {

        Player player = memberPersistencePort.getPlayerPort(command.getAccountId())
                .orElseThrow(NotExistsPlayerException::new);

        int before = player.getStarPoint();

        player.adjustStarPoint(command.getDelta());

        memberPersistencePort.savePlayerPort(player)
                .orElseThrow(NotExistsPlayerException::new);

        AdminAuditLog.write("star point adjusted accountId={} before={} delta={} after={}",
                player.getAccountId(), before, command.getDelta(), player.getStarPoint());

        // 스타 포인트 비동기 응답
        memberPublishPort.publishStarPointPort(player);

        return player;
    }

    @Override
    @Transactional
    public Player updateSlotCountUseCase(AdminUpdateSlotCountCommand command) {

        if (command.getSlotCount() == null || command.getSlotCount() < 1 || command.getSlotCount() > Player.getMaxSlotCount()) {
            throw new InvalidAdminSlotCountException();
        }

        Player player = memberPersistencePort.getPlayerPort(command.getAccountId())
                .orElseThrow(NotExistsPlayerException::new);

        player.updateSlotCount(command.getSlotCount());

        memberPersistencePort.savePlayerPort(player)
                .orElseThrow(NotExistsPlayerException::new);

        AdminAuditLog.write("slot count updated accountId={} slotCount={}", player.getAccountId(), player.getSlotCount());

        // 슬롯 수 비동기 응답
        memberPublishPort.publishSlotCountPort(player);

        return player;
    }

    @Override
    @Transactional
    public List<CollectionMap> getCollectionMapsUseCase(Long accountId) {
        return collectionReadPort.getCollectionMapsPort(accountId);
    }

    @Override
    @Transactional
    public List<CollectionMong> getCollectionMongsUseCase(Long accountId) {
        return collectionReadPort.getCollectionMongsPort(accountId);
    }

    @Override
    @Transactional
    public List<CollectionMap> grantCollectionMapUseCase(Long accountId, String mapCode) {

        AdminAuditLog.write("collection map granted accountId={} mapCode={}", accountId, mapCode);

        collectionUseCase.createCollectionMapUseCase(CreateCollectionMapCommand.builder()
                .accountId(accountId)
                .mapCode(mapCode)
                .build());

        return collectionReadPort.getCollectionMapsPort(accountId);
    }

    @Override
    @Transactional
    public List<CollectionMong> grantCollectionMongUseCase(Long accountId, String mongCode) {

        AdminAuditLog.write("collection mong granted accountId={} mongCode={}", accountId, mongCode);

        collectionUseCase.createCollectionMongUseCase(CreateCollectionMongCommand.builder()
                .accountId(accountId)
                .mongCode(mongCode)
                .build());

        return collectionReadPort.getCollectionMongsPort(accountId);
    }
}
