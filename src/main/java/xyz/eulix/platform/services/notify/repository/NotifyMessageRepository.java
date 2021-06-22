package xyz.eulix.platform.services.notify.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import io.quarkus.panache.common.Sort;
import xyz.eulix.platform.services.notify.entity.NotifyMessage;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

import static xyz.eulix.platform.services.notify.entity.NotifyMessage.State.SENT;


@ApplicationScoped
public class NotifyMessageRepository implements PanacheRepository<NotifyMessage> {

    /**
     * Save the message,
     *
     * @param message the specified message
     */
    public void saveMessage(NotifyMessage message) {
        this.persist(message);
    }

    /**
     * Returns  offline message count of the specified clientUUID.
     *
     * @param clientUUID the specified clientUUID
     * @return offline message count of the specified clientUUID.
     */
    public Long offlineMessageCount(String clientUUID) {
        return this.count("client_uuid=?1 AND state!=?2", clientUUID, SENT.getValue());
    }

    /**
     * Returns offline message of the specified clientUUID.
     *
     * @param clientUUID the specified clientUUID
     * @param page page
     * @param pageSize pageSize
     * @return offline message of the specified clientUUID.
     */
    public List<NotifyMessage> listOfflineMessage(String clientUUID, int page, int pageSize) {
        return this.find("client_uuid=?1 AND state!=?2 ", Sort.by("created_at"), clientUUID, SENT.getValue()).page(page, pageSize).list();
    }

    /**
     * Set the message state to Sent,
     * it will transfer the message from any state to <tt>SENT</tt>.
     *
     * @param messageId the specified message id
     */
    public void markMessageSent(String messageId) {
        this.update(
                " state=?1 WHERE message_id=?2",
                SENT.getValue(), messageId);
    }
}
