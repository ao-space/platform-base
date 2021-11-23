package xyz.eulix.platform.services.mgtboard.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import xyz.eulix.platform.services.mgtboard.entity.QuestionnaireEntity;

import javax.enterprise.context.ApplicationScoped;
import java.time.OffsetDateTime;
import java.util.Optional;

@ApplicationScoped
public class QaEntityRepository implements PanacheRepository<QuestionnaireEntity> {
    // 根据id更新资源
    private static final String UPDATE_BY_ID = "title=?1, startAt=?2, endAt=?3, updated_at=now() where id=?4";

    // 根据survey_id查询资源
    private static final String FIND_BY_SURVEY_ID = "payload_survey_id=?1";

    public void updateById(Long qaId, String title, OffsetDateTime startAt, OffsetDateTime endAt) {
        this.update(UPDATE_BY_ID, title, startAt, endAt, qaId);
    }

    public Optional<QuestionnaireEntity> findBySurveyId(Long surveyId) {
        return this.find(FIND_BY_SURVEY_ID, surveyId).singleResultOptional();
    }
}
