package xyz.eulix.platform.services.lock.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * @author VvV
 * @date 2023/8/6
 */
@Entity
@Getter
@Setter
@ToString
@Table(name = "distributed_reentrant_read_write_locks")
public class ReentrantReadWriteLockEntity {

    @Id
    @Column(name = "lock_key")
    private String lockKey;

    @Column(name = "expires_at")
    @NotNull(message = "Expiration timestamp is required")
    private Timestamp expiresAt;

    /**
     * 读锁总数量
     */
    @Column(name = "read_lock_count")
    private Integer readLockCount;

    @Column(name = "write_lock_count")
    private Integer writeLockCount;

    @Column(name = "write_lock_owner_uuid")
    private String writeLockOwnerUUID;

    /**
     * JSON string that stores the UUIDs and reentrant counts of all read locks
     */
    @Column(name = "read_holds_json")
    private String readHoldsJSON;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
