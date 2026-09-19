package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.module.common.security.principal.Passport;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminPingResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 게이트웨이 → 패스포트 → ADMIN 권한 경로 확인용. 관리자 웹이 로그인 직후 한 번 두드린다.
 */
@RestController
@RequestMapping("/admin")
public class AdminHealthController {

    @EntryLoggingPoint
    @GetMapping("/ping")
    public ResponseEntity<ResponseDto<AdminPingResponseDto>> ping(@AuthenticationPrincipal Passport passport) {
        AdminPingResponseDto dto = new AdminPingResponseDto(passport.getAccountId(), passport.getEmail(), passport.getName(), passport.getRole());
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.PING.toResponseDto(dto));
    }
}
