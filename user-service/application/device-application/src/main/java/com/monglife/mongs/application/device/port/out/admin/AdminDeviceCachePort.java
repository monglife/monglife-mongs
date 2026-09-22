package com.monglife.mongs.application.device.port.out.admin;

public interface AdminDeviceCachePort {

    /** 오늘 누적 환전 걸음 수 조회. 키가 없으면 0 */
    int getTodayExchangedWalkingCountPort(Long accountId);

    /** 오늘 누적 환전 걸음 수 초기화(상한 리셋) */
    void resetTodayExchangedWalkingCountPort(Long accountId);
}
