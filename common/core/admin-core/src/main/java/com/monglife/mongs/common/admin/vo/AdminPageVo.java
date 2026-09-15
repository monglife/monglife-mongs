package com.monglife.mongs.common.admin.vo;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

/**
 * 관리자 목록 조회 결과.
 *
 * <p>앱용 {@code PageResult} 는 총 건수가 없어 관리자 웹의 페이지네이션(총 건수 헤더 {@code X-Total-Count})에
 * 맞지 않는다. 관리자 API 는 이 객체를 돌려주고 웹 어댑터가 응답 헤더로 옮긴다.
 */
@Getter
public class AdminPageVo<T> {

    /** 0 부터 시작하는 페이지 번호 */
    private final Integer page;

    private final Integer size;

    private final Long total;

    private final List<T> items;

    @Builder
    public AdminPageVo(Integer page, Integer size, Long total, List<T> items) {
        this.page = page;
        this.size = size;
        this.total = total;
        this.items = items;
    }

    public Integer getTotalPage() {
        return size == null || size == 0 ? 0 : (int) Math.ceil((double) total / size);
    }

    public Boolean getIsLastPage() {
        return (long) (page + 1) * size >= total;
    }

    public <R> AdminPageVo<R> map(Function<T, R> mapper) {
        return AdminPageVo.<R>builder()
                .page(this.page)
                .size(this.size)
                .total(this.total)
                .items(this.items.stream().map(mapper).toList())
                .build();
    }
}
