package com.monglife.mongs.adapter.in.admin.character.web.controller;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.character.web.dto.response.*;
import com.monglife.mongs.adapter.in.admin.character.web.enums.AdapterInAdminCharacterWebResponse;
import com.monglife.mongs.adapter.in.admin.character.web.util.AdminPage;
import com.monglife.mongs.adapter.in.admin.character.web.util.PageQuery;
import com.monglife.mongs.application.battle.port.in.admin.AdminBattleUseCase;
import com.monglife.mongs.application.battle.port.in.admin.command.AdminGetMatchesCommand;
import com.monglife.mongs.domain.battle.enums.MatchStateCode;
import com.monglife.mongs.domain.battle.model.QueuePlayer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/admin/battle")
@RequiredArgsConstructor
public class AdminBattleController {

    private static final Set<String> SORT_KEYS = Set.of("matchId", "createdAt");

    private final AdminBattleUseCase adminBattleUseCase;

    @EntryLoggingPoint
    @GetMapping("/queue")
    public ResponseEntity<ResponseDto<List<AdminQueuePlayerResponseDto>>> getQueuePlayers() {
        List<AdminQueuePlayerResponseDto> items = adminBattleUseCase.getQueuePlayersUseCase().stream().map(AdminQueuePlayerResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_QUEUE_PLAYERS.toResponseDto(items));
    }

    /** 대기열 강제 이탈. 배팅 페이 포인트는 기존 이탈 경로가 되돌려 준다 */
    @EntryLoggingPoint
    @DeleteMapping("/queue/{mongId}")
    public ResponseEntity<ResponseDto<Map<String, Object>>> deleteQueuePlayer(@PathVariable Long mongId) {
        QueuePlayer queuePlayer = adminBattleUseCase.deleteQueuePlayerUseCase(mongId);
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.DELETE_QUEUE_PLAYER.toResponseDto(Map.of(
                "mongId", queuePlayer.getMongId(),
                "accountId", queuePlayer.getAccountId(),
                "deviceId", queuePlayer.getDeviceId()
        )));
    }

    @EntryLoggingPoint
    @GetMapping("/matches")
    public ResponseEntity<PageResponseDto<List<AdminMatchSummaryResponseDto>>> getMatches(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) MatchStateCode stateCode,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String sort
    ) {
        AdminGetMatchesCommand command = AdminGetMatchesCommand.builder()
                .pageRequest(PageQuery.of(page, size, sort, SORT_KEYS, "matchId", true))
                .stateCode(stateCode)
                .accountId(accountId)
                .build();

        return AdminPage.toResponse(AdapterInAdminCharacterWebResponse.GET_MATCHES, adminBattleUseCase.getMatchesUseCase(command), AdminMatchSummaryResponseDto::of);
    }

    @EntryLoggingPoint
    @GetMapping("/matches/{matchId}")
    public ResponseEntity<ResponseDto<AdminMatchResponseDto>> getMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_MATCH.toResponseDto(AdminMatchResponseDto.of(adminBattleUseCase.getMatchUseCase(matchId))));
    }

    @EntryLoggingPoint
    @PostMapping("/matches/{matchId}/terminate")
    public ResponseEntity<ResponseDto<AdminMatchResponseDto>> terminateMatch(@PathVariable Long matchId) {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.TERMINATE_MATCH.toResponseDto(AdminMatchResponseDto.of(adminBattleUseCase.terminateMatchUseCase(matchId))));
    }

    @EntryLoggingPoint
    @GetMapping("/stats")
    public ResponseEntity<ResponseDto<AdminBattleStatsResponseDto>> getStats() {
        return ResponseEntity.ok(AdapterInAdminCharacterWebResponse.GET_BATTLE_STATS.toResponseDto(AdminBattleStatsResponseDto.of(adminBattleUseCase.getStatsUseCase())));
    }
}
