package com.monglife.mongs.adapter.in.admin.user.web.util;

import com.monglife.core.dto.response.PageResponseDto;
import com.monglife.core.enums.response.Response;
import com.monglife.mongs.common.admin.vo.AdminPageVo;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.function.Function;

/**
 * 관리자 페이지 응답. {@code PageResponseDto(page,size,totalPage,isLastPage)} + {@code X-Total-Count} 헤더.
 * 관리자 웹(discovery admin-web)이 읽는 형식과 같다.
 */
public final class AdminPage {

    public static final String TOTAL_COUNT_HEADER = "X-Total-Count";

    private AdminPage() {}

    public static <T, R> ResponseEntity<PageResponseDto<List<R>>> toResponse(Response response, AdminPageVo<T> page, Function<T, R> mapper) {
        List<R> items = page.getItems().stream().map(mapper).toList();
        return ResponseEntity.ok()
                .header(TOTAL_COUNT_HEADER, String.valueOf(page.getTotal()))
                .body(response.toPageResponseDto(items, page.getPage(), page.getSize(), page.getTotalPage(), page.getIsLastPage()));
    }
}
