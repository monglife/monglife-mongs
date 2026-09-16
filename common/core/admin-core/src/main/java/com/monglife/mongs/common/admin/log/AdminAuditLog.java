package com.monglife.mongs.common.admin.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 관리자 조작 감사 로그.
 *
 * <p>클래스 로거를 쓰면 안 된다. stg / prd 의 logback 은 root 를 WARN 으로 두고
 * {@code LogstashLogger} / {@code ConsoleLogger} 만 INFO 로 열어 두기 때문에,
 * 클래스 로거의 info 는 운영에서 조용히 버려진다.
 *
 * <p>이 로거로 남긴 줄은 local·dev 에서는 콘솔에, stg·prd 에서는 Logstash 를 거쳐
 * Elasticsearch 로 들어간다(=Kibana 에서 조회된다).
 */
public final class AdminAuditLog {

    private static final Logger log = LoggerFactory.getLogger("LogstashLogger");

    private static final String PREFIX = "[admin] ";

    private AdminAuditLog() {}

    public static void write(String format, Object... args) {
        log.info(PREFIX + format, args);
    }
}
