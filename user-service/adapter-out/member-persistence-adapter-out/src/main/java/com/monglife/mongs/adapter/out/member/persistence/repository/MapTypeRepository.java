package com.monglife.mongs.adapter.out.member.persistence.repository;

import com.monglife.mongs.adapter.out.member.persistence.entity.MapTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MapTypeRepository extends JpaRepository<MapTypeEntity, Long> {

    Optional<MapTypeEntity> findByComnCode(String mapCode);
}
