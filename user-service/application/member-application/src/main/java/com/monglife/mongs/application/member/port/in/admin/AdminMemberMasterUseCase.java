package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterUseCase {

    /** 등록 가능한 마스터 데이터 종류 */
    enum Kind {
        MAP_TYPE,
        EXCHANGE_STAR_POINT_PRODUCT,
    }

    List<AdminMapTypeVo> getMapTypesUseCase();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsUseCase();

    void createMasterUseCase(Kind kind, AdminCreateMasterCommand command);
}
