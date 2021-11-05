package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.FeedbackReq;
import xyz.eulix.platform.services.mgtboard.dto.QuestionnaireReq;
import xyz.eulix.platform.services.mgtboard.dto.QuestionnaireRes;
import xyz.eulix.platform.services.mgtboard.dto.QuestionnaireUpdateReq;
import xyz.eulix.platform.services.support.model.PageListResult;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuestionnaireService {
    private static final Logger LOG = Logger.getLogger("app.log");

    public QuestionnaireRes saveQuestionnaire(QuestionnaireReq questionnaireReq) {
        return null;
    }

    public QuestionnaireRes updateQuestionnaire(Long questionnaireId, QuestionnaireUpdateReq updateReq) {
        return null;
    }

    public void deleteQuestionnaire(Long questionnaireId) {
    }

    public QuestionnaireRes getQuestionnaire(Long questionnaireId) {
        return null;
    }

    public PageListResult<QuestionnaireRes> listQuestionnaire(Integer currentPage, Integer pageSize) {
        return null;
    }

    public QuestionnaireRes feedbackSave(FeedbackReq feedbackReq) {
        return null;
    }
}
