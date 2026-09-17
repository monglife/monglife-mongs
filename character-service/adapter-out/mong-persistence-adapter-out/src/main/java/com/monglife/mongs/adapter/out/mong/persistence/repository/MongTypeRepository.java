package com.monglife.mongs.adapter.out.mong.persistence.repository;

import com.monglife.mongs.adapter.out.mong.persistence.entity.MongTypeEntity;
import com.monglife.mongs.adapter.out.mong.persistence.repository.dsl.MongTypeDslRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MongTypeRepository extends JpaRepository<MongTypeEntity, String>, MongTypeDslRepository {

    Optional<MongTypeEntity> findByComnCode(String mongCode);

    /** 이 레포지토리의 ID 타입은 String(코드)이라 PK 로는 findById 를 쓸 수 없다 */
    Optional<MongTypeEntity> findByMongTypeId(Long mongTypeId);

    List<MongTypeEntity> findByLevel(Integer level);
}
