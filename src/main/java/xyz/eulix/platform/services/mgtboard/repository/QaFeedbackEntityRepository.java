package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.QuestionnaireFeedbackEntity;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class QaFeedbackEntityRepository implements PanacheRepository<QuestionnaireFeedbackEntity> {
    // 根据subdomain查询资源
    private static final String FIND_BY_SUBDOMAIN = "subdomain=?1";

    // 根据survey_id查询资源
    private static final String FIND_BY_SURVEY_ID = "payload_survey_id=?1";

    // 根据subdomain、survey_id、answer_id查询资源
    private static final String FIND_BY_SUBDOMAIN_SURVEY_ANSWER_ID = "subdomain=?1 AND payload_survey_id=?2 AND payload_answer_id=?3";

    public List<QuestionnaireFeedbackEntity> findBySubdomain(String subdomain) {
        return this.find(FIND_BY_SUBDOMAIN, subdomain).list();
    }

    public void deleteBySurveyId(Long payloadSurveyId) {
        this.delete(FIND_BY_SURVEY_ID, payloadSurveyId);
    }

    public Optional<QuestionnaireFeedbackEntity> findBySubdomainAndSurveyAndAnswerId(String subdomain, Long payloadSurveyId, Long payloadAnswerId) {
        return this.find(FIND_BY_SUBDOMAIN_SURVEY_ANSWER_ID, subdomain, payloadSurveyId, payloadAnswerId).singleResultOptional();
    }
}
