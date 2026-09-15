package com.monglife.mongs.adapter.in.admin.user.web.util;

import com.monglife.mongs.common.admin.vo.AdminPageRequestVo;

import java.util.Set;

/** page/size/sort 쿼리 파라미터 정규화. page 는 0 부터, size 상한 100 */
public final class PageQuery {

    public static final int DEFAULT_SIZE = 15;
    public static final int MAX_SIZE = 100;

    private PageQuery() {}

    public static int page(Integer page) {
        return page == null || page < 0 ? 0 : page;
    }

    public static int size(Integer size) {
        if (size == null || size < 1) return DEFAULT_SIZE;
        return Math.min(size, MAX_SIZE);
    }

    /** 빈 문자열은 null 로 (필터 "전체") */
    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * {@code sort=field,asc|desc} 파싱. 허용 필드가 아니면 기본값으로 떨어진다.
     */
    public static AdminPageRequestVo of(Integer page, Integer size, String sort, Set<String> allowed, String defaultKey, boolean defaultDesc) {
        String key = defaultKey;
        boolean desc = defaultDesc;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",", 2);
            String candidate = parts[0].trim();
            if (allowed.contains(candidate)) {
                key = candidate;
                desc = parts.length < 2 || !"asc".equalsIgnoreCase(parts[1].trim());
            }
        }
        return AdminPageRequestVo.builder()
                .page(page(page))
                .size(size(size))
                .sortKey(key)
                .sortDesc(desc)
                .build();
    }
}
