package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminGetOrdersCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderDetailVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderVo;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.Order;

public interface AdminOrderUseCase {

    AdminPageVo<AdminOrderVo> getOrdersUseCase(AdminGetOrdersCommand command);

    AdminOrderDetailVo getOrderUseCase(Long orderId);

    /** 미소비 주문 재소비. 기존 주문 소비 유스케이스를 그대로 태운다 */
    Order reconsumeOrderUseCase(Long orderId);
}
