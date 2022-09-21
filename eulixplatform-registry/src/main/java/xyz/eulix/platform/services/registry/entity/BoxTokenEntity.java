package xyz.eulix.platform.services.registry.entity;

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UpdateTimestamp;
import xyz.eulix.platform.common.support.model.BaseEntity;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "box_token")
public class BoxTokenEntity extends BaseEntity {
  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @NotBlank
  @Column(name = "service_id")
  private String serviceId;

  @NotBlank
  @Column(name = "service_name")
  private String serviceName;

  @NotBlank
  @Column(name = "box_reg_key")
  private String boxRegKey;

  @Column(name = "expires_at")
  private OffsetDateTime expiresAt;
}
