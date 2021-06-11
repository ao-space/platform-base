package xyz.eulix.platform.services.registry.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter @Setter @ToString(callSuper = true)
@Entity @Table(name = "registries")
public class RegistryEntity extends BaseEntity {

  @NotBlank
  @Column(name = "box_reg_key")
  private String boxRegKey;

  @NotBlank
  @Column(name = "client_reg_key")
  private String clientRegKey;

  @NotBlank
  @Column(name = "box_uuid")
  private String boxUUID;

  @NotBlank
  @Column(name = "client_uuid")
  private String clientUUID;

  @NotNull
  @ValueOfEnum(enumClass = State.class)
  @Column(name = "state")
  private State state;

  @NotBlank
  @Column(name = "subdomain")
  private String subdomain;

  /**
   * The tunnel sever info (Json format):
   * {
   *   "ts_url" : "",
   *   "ts_port" : "",
   *   "ts_auth": ""
   * }
   */
  @NotBlank
  @Column(name = "ts_info")
  private String tsInfo;

  public enum State {
    READY, REVOKED,
    ;
    public boolean isRevoked() {
      return this == REVOKED;
    }

    public boolean isReady() {
      return this == READY;
    }
  }
}
