package xyz.eulix.platform.services.notify.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import xyz.eulix.platform.services.registry.entity.BaseEntity;
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
@Table(name = "notify_message")
public class NotifyMessage extends BaseEntity {
    @NotBlank
    @Column(name = "client_uuid")
    private String clientUUID;

    @NotBlank
    @Column(name = "message_id", unique = true)
    private String messageId;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "ext_parameters")
    private String extParameters;

    @NotNull
    @ValueOfEnum(enumClass = State.class, valueMethod = "getValue")
    @Column(name = "state")
    private Integer state;

    @Getter
    public enum State {
        SENDING(0),
        SENT(1),
        RETRY(2),
        ;

        private final int value;

        State(int value) {
            this.value = value;
        }

        public static State valueOf(final int value) {
            Optional<State> any = Arrays.stream(values()).filter(
                    state -> state.value == value).findAny();

            return any.orElseThrow(
                    () -> new IllegalArgumentException("this value is illegal for message state - " + value));
        }

        public boolean isRetry() {
            return this == RETRY;
        }

        public boolean isSent() {
            return this == SENT;
        }
    }
}
