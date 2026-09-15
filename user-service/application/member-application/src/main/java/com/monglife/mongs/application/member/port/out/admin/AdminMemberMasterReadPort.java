package com.monglife.mongs.application.member.port.out.admin;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterReadPort {

    List<AdminMapTypeVo> getMapTypesPort();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsPort();
}
