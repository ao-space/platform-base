package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.QuestionnaireFeedbackEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class QaFeedbackEntityRepository implements PanacheRepository<QuestionnaireFeedbackEntity> {
    // 根据user_domain查询资源
    private static final String FIND_BY_USERDOMAIN = "user_domain=?1";

    // 根据survey_id查询资源
    private static final String FIND_BY_SURVEY_ID = "payload_survey_id=?1";

    // 根据user_domain、survey_id、answer_id查询资源
    private static final String FIND_BY_USERDOMAIN_SURVEY_ANSWER_ID = "user_domain=?1 AND payload_survey_id=?2 AND payload_answer_id=?3";

    // 根据user_domain更新资源
    private static final String UPDATE_BY_USER_DOMAIN = "user_domain=?1, updated_at=now() where user_domain=?2";

    public List<QuestionnaireFeedbackEntity> findByUserDomain(String userDomain) {
        return this.find(FIND_BY_USERDOMAIN, userDomain).list();
    }

    public void deleteBySurveyId(Long payloadSurveyId) {
        this.delete(FIND_BY_SURVEY_ID, payloadSurveyId);
    }

    public Optional<QuestionnaireFeedbackEntity> findByUserDomainAndSurveyAndAnswerId(String userDomain, Long payloadSurveyId, Long payloadAnswerId) {
        return this.find(FIND_BY_USERDOMAIN_SURVEY_ANSWER_ID, userDomain, payloadSurveyId, payloadAnswerId).singleResultOptional();
    }

    public void updateUserDomainByUserDomain(String userDomain, String userDomainOld) {
        this.update(UPDATE_BY_USER_DOMAIN, userDomain, userDomainOld);
    }
}
