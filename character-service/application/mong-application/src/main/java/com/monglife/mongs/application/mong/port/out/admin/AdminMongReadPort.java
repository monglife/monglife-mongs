package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.model.Mong;

import java.time.LocalDateTime;
import java.util.Map;

public interface AdminMongReadPort {

    /** 정렬 키: mongId | createdAt | exp | payPoint | accountId */
    AdminPageVo<Mong> getMongsPort(AdminPageRequestVo pageRequest, Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query);

    Long countMongsPort();

    Long countMongsCreatedSincePort(LocalDateTime since);

    Map<MongStateCode, Long> countMongsByStatePort();

    Map<MongStatusCode, Long> countMongsByStatusPort();
}
