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

    @Column(name = "mode")
    private String mode;

    /**
     * JSON string that stores the UUIDs and reentrant counts of all read locks
     */
    @Column(name = "lock_holds_json")
    private String lockHoldsJSON;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;
}
