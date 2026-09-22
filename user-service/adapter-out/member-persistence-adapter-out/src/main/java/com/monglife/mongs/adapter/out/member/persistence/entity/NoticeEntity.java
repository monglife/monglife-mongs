package com.monglife.mongs.adapter.out.member.persistence.entity;

import com.monglife.module.common.jpa.entity.BaseTimeEntity;
import com.monglife.mongs.domain.member.model.Notice;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners({ AuditingEntityListener.class })
@Table(name = "mongs_notice")
public class NoticeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notice_id")
    private Long noticeId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content")
    private String content;

    @Column(name = "writer_account_id")
    private Long writerAccountId;

    @Column(name = "writer_name")
    private String writerName;

    @Column(name = "is_hided")
    private Boolean isHided;

    @Builder
    public NoticeEntity(Long noticeId, String title, String content, Long writerAccountId, String writerName, Boolean isHided) {
        this.noticeId = noticeId;
        this.title = title;
        this.content = content;
        this.writerAccountId = writerAccountId;
        this.writerName = writerName;
        this.isHided = isHided;
    }

    /**
     * 관리자 공지 사항 수정
     */
    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    /**
     * 관리자 공지 사항 숨김 여부 수정
     */
    public void updateHided(Boolean isHided) {
        this.isHided = isHided;
    }

    /**
     * 엔티티 도메인 변환
     * @return 공지 사항 도메인 객체
     */
    public Notice toDomain() {
        return Notice.builder()
                .noticeId(this.noticeId)
                .title(this.title)
                .content(this.content)
                .writerAccountId(this.writerAccountId)
                .writerName(this.writerName)
                .isHided(this.isHided)
                .createdAt(this.getCreatedAt())
                .updatedAt(this.getUpdatedAt())
                .build();
    }
}
