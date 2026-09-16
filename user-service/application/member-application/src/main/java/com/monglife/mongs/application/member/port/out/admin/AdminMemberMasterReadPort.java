package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterReadPort {

    List<AdminMapTypeVo> getMapTypesPort();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsPort();

    /** 공통 코드(monglife_comn_code) 존재 여부 */
    Boolean isExistsComnCodePort(String code);

    /** 공통 코드를 만들어 붙이고 맵 타입을 등록한다 */
    void createMapTypePort(AdminCreateMasterCommand command);

    /** 공통 코드를 만들어 붙이고 환전 상품을 등록한다 */
    void createExchangeStarPointProductPort(AdminCreateMasterCommand command);
}
