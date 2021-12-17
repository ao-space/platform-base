package xyz.eulix.platform.services.auth.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "pkey_auth")
public class PkeyAuthEntity extends BaseEntity {
    // 二维码pkey值
    @NotBlank
    @Column(name = "pkey")
    private String pkey;

    // pkey超时时间
    @NotNull
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;

    // 登录box端的key
    @Column(name = "bkey")
    private String bkey;

    // 盒子域名
    @Column(name = "user_domain")
    private String userDomain;

    // 盒子公钥
    @Column(name = "box_pub_key")
    private String boxPubKey;
}
