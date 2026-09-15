package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderVo;
import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AdminOrderReadPort {

    /** 정렬 키: orderId | createdAt | price */
    AdminPageVo<AdminOrderVo> getOrdersPort(AdminPageRequestVo pageRequest, Long accountId, String productId);

    Optional<AdminOrderVo> getOrderPort(Long orderId);

    Long countOrdersPort();

    Long countOrdersSincePort(LocalDateTime since);

    Double sumOrderPriceSincePort(LocalDateTime since);
}
