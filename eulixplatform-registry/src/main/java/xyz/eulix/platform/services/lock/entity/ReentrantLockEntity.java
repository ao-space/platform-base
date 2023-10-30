package xyz.eulix.platform.services.lock.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * @author VvV
 * @date 2023/7/31
 */

@Entity
@Getter
@Setter
@Table(name = "distributed_reentrant_locks")
public class ReentrantLockEntity {

    @Id
    @Column(name = "lock_key")
    private String lockKey;

    @Column(name = "lock_value")
    @NotBlank
    private String lockValue;

    @Column(name = "expires_at")
    @NotNull
    private Timestamp expiresAt;

    @Column(name = "reentrant_count")
    @NotNull
    private Integer reentrantCount;
}

