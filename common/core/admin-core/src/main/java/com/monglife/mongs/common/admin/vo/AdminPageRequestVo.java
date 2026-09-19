package com.monglife.mongs.common.admin.vo;

import lombok.Builder;
import lombok.Getter;

/**
 * 관리자 목록 조회 조건(페이지·정렬). 검색 필터는 각 커맨드가 따로 가진다.
 */
@Getter
public class AdminPageRequestVo {

    /** 0 부터 시작하는 페이지 번호 */
    private final Integer page;

    private final Integer size;

    /** 정렬 키. 허용 값은 각 조회 포트가 정한다 */
    private final String sortKey;

    private final Boolean sortDesc;

    @Builder
    public AdminPageRequestVo(Integer page, Integer size, String sortKey, Boolean sortDesc) {
        this.page = page;
        this.size = size;
        this.sortKey = sortKey;
        this.sortDesc = sortDesc;
    }

    public Long getOffset() {
        return (long) page * size;
    }
}
