package com.monglife.mongs.application.member.port.in.admin;

import com.monglife.mongs.application.member.port.in.admin.vo.AdminExchangeStarPointProductVo;
import com.monglife.mongs.application.member.port.in.admin.vo.AdminMapTypeVo;

import java.util.List;

public interface AdminMemberMasterUseCase {

    List<AdminMapTypeVo> getMapTypesUseCase();

    List<AdminExchangeStarPointProductVo> getExchangeStarPointProductsUseCase();
}
