package com.monglife.mongs.application.mong.port.out.admin;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.mong.enums.MongStateCode;
import com.monglife.mongs.domain.mong.enums.MongStatusCode;
import com.monglife.mongs.domain.mong.model.Inventory;
import com.monglife.mongs.domain.mong.model.Mong;

import java.time.LocalDateTime;
import java.util.Map;

public interface AdminMongReadPort {

    /** 정렬 키: mongId | createdAt | exp | payPoint | accountId */
    AdminPageVo<Mong> getMongsPort(AdminPageRequestVo pageRequest, Long accountId, MongStateCode stateCode, MongStatusCode statusCode, String query);

    /**
     * 인벤토리 목록. 앱용 {@code getInventoriesPort} 와 나눠 둔다.
     *
     * <p>앱 쪽은 총 건수가 없는 {@code PageResult} 를 돌려주는데, 관리자 웹은
     * {@code X-Total-Count} 를 쓴다. 앱 경로의 반환 타입을 바꾸면 앱 계약이 깨지므로 따로 판다.
     */
    AdminPageVo<Inventory> getInventoriesPort(AdminPageRequestVo pageRequest, Long mongId);

    Long countMongsPort();

    Long countMongsCreatedSincePort(LocalDateTime since);

    Map<MongStateCode, Long> countMongsByStatePort();

    Map<MongStatusCode, Long> countMongsByStatusPort();
}
