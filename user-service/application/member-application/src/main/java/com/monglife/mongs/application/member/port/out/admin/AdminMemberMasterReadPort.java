package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.application.member.port.in.admin.command.AdminCreateMasterCommand;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterReadPort {

    List<AdminMapTypeVo> getMapTypesPort();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsPort();

    /**
     * 같은 종류에 같은 코드가 이미 있는지. 중복 판정은 공통 코드가 아니라 <b>종류별</b>로 한다 —
     * 지웠다가 같은 코드로 다시 넣을 수 있어야 하고, 공통 코드는 삭제해도 남기기 때문이다.
     */
    Boolean isExistsMapTypePort(String code);

    Boolean isExistsExchangeStarPointProductPort(String productId);

    /** 공통 코드가 없으면 만들어 붙이고, 이미 있으면 그대로 쓴다 */
    void createMapTypePort(AdminCreateMasterCommand command);

    void createExchangeStarPointProductPort(AdminCreateMasterCommand command);

    /**
     * 마스터 데이터 삭제. 표의 행만 지우고 공통 코드는 남긴다 —
     * 주문·도감이 코드를 참조하고 있어 함께 지우면 그 행들이 깨진다.
     */
    Boolean deleteMapTypePort(Long id);

    Boolean deleteExchangeStarPointProductPort(String productId);
}
