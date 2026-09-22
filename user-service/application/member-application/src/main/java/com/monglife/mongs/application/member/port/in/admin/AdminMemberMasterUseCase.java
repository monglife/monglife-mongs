package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterUseCase {

    /** 등록·삭제 가능한 마스터 데이터 종류 */
    enum Kind {
        MAP_TYPE,
        EXCHANGE_STAR_POINT_PRODUCT,
    }

    List<AdminMapTypeVo> getMapTypesUseCase();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsUseCase();

    void createMasterUseCase(Kind kind, AdminCreateMasterCommand command);

    /**
     * 마스터 데이터 삭제. 맵은 mapTypeId(숫자), 환전 상품은 productId(문자)라 식별자를 문자로 받는다.
     */
    void deleteMasterUseCase(Kind kind, String id);
}
