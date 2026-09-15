package com.monglife.mongs.application.member.port.in.admin.service;

import com.monglife.mongs.application.member.port.exception.NotExistsOrderException;
import com.monglife.mongs.application.member.port.in.StoreUseCase;
import com.monglife.mongs.application.member.port.in.admin.AdminOrderUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetOrdersCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderDetailVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminOrderVo;
import com.monglife.mongs.application.member.port.in.command.ConsumeOrderCommand;
import com.monglife.mongs.application.member.port.out.GooglePaymentPort;
import com.monglife.mongs.application.member.port.out.OrderReadPort;
import com.monglife.mongs.application.member.port.out.admin.AdminOrderReadPort;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import com.monglife.mongs.domain.member.model.ExchangeStarPointProduct;
import com.monglife.mongs.domain.member.model.InAppOrder;
import com.monglife.mongs.domain.member.model.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminOrderService implements AdminOrderUseCase {

    private final AdminOrderReadPort adminOrderReadPort;

    private final OrderReadPort orderReadPort;

    private final GooglePaymentPort googlePaymentPort;

    private final StoreUseCase storeUseCase;

    @Override
    @Transactional
    public AdminPageVo<AdminOrderVo> getOrdersUseCase(AdminGetOrdersCommand command) {
        return adminOrderReadPort.getOrdersPort(command.getPageRequest(), command.getAccountId(), command.getProductId());
    }

    /**
     * 주문 상세. 구글 플레이 조회는 외부 호출이라 실패해도 주문 자체는 내려준다.
     */
    @Override
    @Transactional
    public AdminOrderDetailVo getOrderUseCase(Long orderId) {

        AdminOrderVo adminOrderVo = adminOrderReadPort.getOrderPort(orderId)
                .orElseThrow(NotExistsOrderException::new);

        Order order = orderReadPort.getOrderBySocialOrderIdPort(adminOrderVo.getSocialOrderId())
                .orElseThrow(NotExistsOrderException::new);

        Integer starPoint = orderReadPort.getExchangeStarPointProductPort(order.getProductId())
                .map(ExchangeStarPointProduct::getStarPoint)
                .orElse(null);

        InAppOrder inAppOrder = null;
        try {
            inAppOrder = googlePaymentPort.getInAppOrderPort(order.getProductId(), order.getSocialOrderId(), order.getPurchaseToken())
                    .orElse(null);
        } catch (Exception exception) {
            log.warn("[admin] google in-app order lookup failed orderId={}", orderId, exception);
        }

        return AdminOrderDetailVo.builder()
                .order(adminOrderVo)
                .starPoint(starPoint)
                .inAppOrder(inAppOrder)
                .build();
    }

    @Override
    @Transactional
    public Order reconsumeOrderUseCase(Long orderId) {

        AdminOrderVo adminOrderVo = adminOrderReadPort.getOrderPort(orderId)
                .orElseThrow(NotExistsOrderException::new);

        log.info("[admin] reconsume order orderId={} accountId={}", orderId, adminOrderVo.getAccountId());

        return storeUseCase.consumeOrderUseCase(ConsumeOrderCommand.builder()
                .socialOrderId(adminOrderVo.getSocialOrderId())
                .build());
    }
}
