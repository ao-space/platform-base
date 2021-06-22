package xyz.eulix.platform.services.notify.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.support.model.BaseEntity;
import xyz.eulix.platform.services.support.validator.ValueOfEnum;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Arrays;
import java.util.Optional;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "notify_device")
public class NotifyDevice extends BaseEntity {
    @NotBlank
    @Column(name = "client_uuid")
    private String clientUUID;

    @NotBlank
    @Column(name = "client_reg_key")
    private String clientRegKey;

    @NotBlank
    @Column(name = "device_id", unique = true)
    private String deviceId;

    @NotBlank
    @Column(name = "platform")
    private String platform;

    @Column(name = "device_token")
    private String deviceToken;

    @NotNull
    @ValueOfEnum(enumClass = State.class, valueMethod = "getValue")
    @Column(name = "state")
    private Integer state;

    public boolean iOS() {
        return platform.toLowerCase().contentEquals("ios");
    }

    @Getter
    public enum State {
        ACTIVE(0),
        INACTIVE(1),
        ;

        private final int value;

        State(int value) {
            this.value = value;
        }

        public static State valueOf(final int value) {
            Optional<State> any = Arrays.stream(values()).filter(
                    state -> state.value == value).findAny();

            return any.orElseThrow(
                    () -> new IllegalArgumentException("this value is illegal for device state - " + value));
        }

        public boolean isActive() {
            return this == ACTIVE;
        }

    }
}