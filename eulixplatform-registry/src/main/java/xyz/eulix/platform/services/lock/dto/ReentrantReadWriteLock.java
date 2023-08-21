package xyz.eulix.platform.services.lock.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author VvV
 * @date 2023/8/7
 */
@Getter
@Setter
@ToString
public class ReentrantReadWriteLock {
    private String lockKey;

    private Timestamp expiresAt;

    private Integer readLockCount;

    private Integer writeLockCount;

    private String writeLockOwnerUUID;

    private Map<String, Integer> readHoldsMap;

    private Integer version;
}
