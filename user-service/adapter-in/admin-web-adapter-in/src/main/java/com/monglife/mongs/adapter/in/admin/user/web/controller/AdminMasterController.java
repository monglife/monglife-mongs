package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminExchangeStarPointProductResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminMapTypeResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.adapter.in.admin.user.web.dto.request.AdminMasterCreateRequestDto;
import com.monglife.mongs.application.member.port.in.admin.AdminMemberMasterUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** 마스터 데이터 읽기 전용. 수정은 SQL 배포로 한다(캐시·시드와 충돌). */
@Validated
@RestController
@RequestMapping("/admin/master")
@RequiredArgsConstructor
public class AdminMasterController {

    private final AdminMemberMasterUseCase adminMemberMasterUseCase;

    @EntryLoggingPoint
    @GetMapping("/map-types")
    public ResponseEntity<ResponseDto<List<AdminMapTypeResponseDto>>> getMapTypes() {
        List<AdminMapTypeResponseDto> items = adminMemberMasterUseCase.getMapTypesUseCase().stream().map(AdminMapTypeResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_MAP_TYPES.toResponseDto(items));
    }

    @EntryLoggingPoint
    @GetMapping("/exchange-star-point-products")
    public ResponseEntity<ResponseDto<List<AdminExchangeStarPointProductResponseDto>>> getExchangeStarPointProducts() {
        List<AdminExchangeStarPointProductResponseDto> items = adminMemberMasterUseCase.getExchangeStarPointProductsUseCase().stream().map(AdminExchangeStarPointProductResponseDto::of).toList();
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_EXCHANGE_STAR_POINT_PRODUCTS.toResponseDto(items));
    }

    /** 마스터 데이터 등록. 종류를 body 의 kind 로 받는다 */
    @EntryLoggingPoint
    @PostMapping
    public ResponseEntity<ResponseDto<Map<String, Object>>> createMaster(@Valid @RequestBody AdminMasterCreateRequestDto requestDto) {

        AdminCreateMasterCommand command = AdminCreateMasterCommand.builder()
                .code(requestDto.getCode())
                .name(requestDto.getName())
                .words(requestDto.getWords())
                .starPoint(requestDto.getStarPoint())
                .build();

        adminMemberMasterUseCase.createMasterUseCase(requestDto.getKind(), command);

        return ResponseEntity.ok(AdapterInAdminUserWebResponse.CREATE_MASTER.toResponseDto(Map.of(
                "kind", requestDto.getKind().name(),
                "code", requestDto.getCode()
        )));
    }

    /** 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 */
    @EntryLoggingPoint
    @DeleteMapping("/{kind}/{id}")
    public ResponseEntity<ResponseDto<Map<String, Object>>> deleteMaster(
            @PathVariable AdminMemberMasterUseCase.Kind kind,
            @PathVariable String id
    ) {
        adminMemberMasterUseCase.deleteMasterUseCase(kind, id);
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.DELETE_MASTER.toResponseDto(Map.of("kind", kind.name(), "id", id)));
    }
}
