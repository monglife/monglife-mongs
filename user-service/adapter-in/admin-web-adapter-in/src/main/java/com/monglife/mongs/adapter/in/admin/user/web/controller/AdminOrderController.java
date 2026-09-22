package com.monglife.mongs.adapter.in.admin.user.web.controller;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.dto.response.ResponseDto;
import com.monglife.module.common.logging.annotation.EntryLoggingPoint;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminOrderDetailResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.dto.response.AdminOrderResponseDto;
import com.monglife.mongs.adapter.in.admin.user.web.enums.AdapterInAdminUserWebResponse;
import com.monglife.mongs.adapter.in.admin.user.web.util.AdminPage;
import com.monglife.mongs.adapter.in.admin.user.web.util.PageQuery;
import com.monglife.mongs.application.member.port.in.admin.AdminOrderUseCase;
import com.monglife.mongs.application.member.port.in.admin.command.AdminGetOrdersCommand;
import com.monglife.mongs.domain.member.model.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private static final Set<String> SORT_KEYS = Set.of("orderId", "createdAt", "price");

    private final AdminOrderUseCase adminOrderUseCase;

    @EntryLoggingPoint
    @GetMapping
    public ResponseEntity<PageResponseDto<List<AdminOrderResponseDto>>> getOrders(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) String productId,
            @RequestParam(required = false) String sort
    ) {
        AdminGetOrdersCommand command = AdminGetOrdersCommand.builder()
                .pageRequest(PageQuery.of(page, size, sort, SORT_KEYS, "orderId", true))
                .accountId(accountId)
                .productId(PageQuery.blankToNull(productId))
                .build();

        return AdminPage.toResponse(AdapterInAdminUserWebResponse.GET_ORDERS, adminOrderUseCase.getOrdersUseCase(command), AdminOrderResponseDto::of);
    }

    @EntryLoggingPoint
    @GetMapping("/{orderId}")
    public ResponseEntity<ResponseDto<AdminOrderDetailResponseDto>> getOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.GET_ORDER.toResponseDto(AdminOrderDetailResponseDto.of(adminOrderUseCase.getOrderUseCase(orderId))));
    }

    /**
     * 미소비 주문 재처리. 구글 플레이에서 결제·미소비를 확인한 뒤 스타 포인트를 지급하고 소비 처리한다.
     */
    @EntryLoggingPoint
    @PostMapping("/{orderId}/reconsume")
    public ResponseEntity<ResponseDto<Map<String, Object>>> reconsumeOrder(@PathVariable Long orderId) {
        Order order = adminOrderUseCase.reconsumeOrderUseCase(orderId);
        return ResponseEntity.ok(AdapterInAdminUserWebResponse.RECONSUME_ORDER.toResponseDto(Map.of(
                "orderId", order.getOrderId(),
                "accountId", order.getAccountId(),
                "socialOrderId", order.getSocialOrderId()
        )));
    }
}
