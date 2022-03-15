package xyz.eulix.platform.services.registry.entity;

import lombok.*;
import xyz.eulix.platform.services.registry.dto.registry.SubdomainStateEnum;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "subdomain")
// 用于正则匹配保留域名对应的已经注册域名. 由于需要使用 mysql 的 regexp/rlike 关键字来查询, 所以使用 NamedNativeQueries.
@NamedNativeQueries({
        @NamedNativeQuery(name = "SubdomainEntity.findByRegexp",query = "select * from subdomain where subdomain REGEXP :regexp", resultClass=SubdomainEntity.class)
})
public class SubdomainEntity extends BaseEntity {

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @Column(name = "user_id")
  private String userId;

  @NotBlank
  @Column(name = "subdomain")
  private String subdomain;

  @Column(name = "user_domain")
  private String userDomain;

  @NotNull
  @Column(name = "state")
  @ValueOfEnum(enumClass = SubdomainStateEnum.class, valueMethod = "getState")
  private Integer state;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;
}
