package com.monglife.mongs.application.mong.port.out;

public interface MongEventPort {

    /**
     * 몽 생성 이벤트
     * @param accountId 계정 ID
     * @param mongCode 몽 타입 코드
     */
    void createMongEventPort(Long accountId, String mongCode);

    /**
     * 몽 진화 이벤트
     * @param accountId 계정 ID
     * @param mongCode 몽 타입 코드
     */
    void evolutionMongEventPort(Long accountId, String mongCode);

    /**
     * 랜덤 맵 뽑기 이벤트
     * @param accountId 계정 ID
     * @param mapCode 맵 타입 코드
     */
    void randomDrawMapEventPort(Long accountId, String mapCode);

    /**
     * 미션 리워드 스타 포인트 지급 이벤트
     *
     * <p>스타 포인트는 user-service 의 Player 소유라 character-service 가 직접 못 올린다.
     * 랜덤 맵 뽑기와 같은 단방향 이벤트다(롤백 없음).
     *
     * @param accountId 계정 ID
     * @param starPoint 지급 스타 포인트
     * @param missionCode 미션 코드 (소비 측 로그 추적용)
     */
    void missionRewardStarPointEventPort(Long accountId, Integer starPoint, String missionCode);
}
