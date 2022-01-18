package xyz.eulix.platform.services.mgtboard.service;

import org.jboss.logging.Logger;
import xyz.eulix.platform.services.mgtboard.dto.*;
import xyz.eulix.platform.services.mgtboard.entity.QuestionnaireEntity;
import xyz.eulix.platform.services.mgtboard.entity.QuestionnaireFeedbackEntity;
import xyz.eulix.platform.services.mgtboard.repository.QaEntityRepository;
import xyz.eulix.platform.services.mgtboard.repository.QaFeedbackEntityRepository;
import xyz.eulix.platform.services.support.CommonUtils;
import xyz.eulix.platform.services.support.model.PageInfo;
import xyz.eulix.platform.services.support.model.PageListResult;
import xyz.eulix.platform.services.support.serialization.OperationUtils;
import xyz.eulix.platform.services.support.service.ServiceError;
import xyz.eulix.platform.services.support.service.ServiceOperationException;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@ApplicationScoped
public class QuestionnaireService {
    private static final Logger LOG = Logger.getLogger("app.log");

    @Inject
    QaEntityRepository qaEntityRepository;

    @Inject
    QaFeedbackEntityRepository qaFeedbackEntityRepository;

    @Inject
    OperationUtils operationUtils;

    /**
     * 保存问卷
     *
     * @param qaReq 问卷
     * @return 问卷
     */
    @Transactional
    public QuestionnaireRes saveQuestionnaire(QuestionnaireReq qaReq) {
        // 查询问卷是否存在
        Optional<QuestionnaireEntity> qaEntityOp = qaEntityRepository.findBySurveyId(qaReq.getPayloadSurveyId());
        if (!qaEntityOp.isEmpty()) {
            LOG.warnv("payload survey already exist, payloadSurveyId:{0}", qaReq.getPayloadSurveyId());
            throw new ServiceOperationException(ServiceError.PAYLOAD_SURVEY_ALREADY_EXIST);
        }
        QuestionnaireEntity qaEntity = qaReqToEntity(qaReq);
        qaEntityRepository.persist(qaEntity);
        return qaEntityToRes(qaEntity);
    }

    /**
     * 更新问卷
     *
     * @param qaId 问卷id
     * @param updateReq 更新内容
     * @return 问卷
     */
    @Transactional
    public QuestionnaireRes updateQuestionnaire(Long qaId, QuestionnaireUpdateReq updateReq) {
        QuestionnaireEntity qaEntity = qaEntityRepository.findById(qaId);
        if (qaEntity == null) {
            LOG.warnv("questionnaire does not exist, questionnaireId:{0}", qaId);
            throw new ServiceOperationException(ServiceError.QUESTIONNAIRE_NOT_EXIST);
        }
        qaEntityRepository.updateById(qaId, updateReq.getTitle(), updateReq.getStartAt(), updateReq.getEndAt());
        return QuestionnaireRes.of(qaId,
                updateReq.getTitle(),
                qaEntity.getContent(),
                updateReq.getStartAt(),
                updateReq.getEndAt(),
                qaEntity.getPayloadSurveyId());
    }

    /**
     * 删除问卷
     *
     * @param qaId 问卷id
     */
    @Transactional
    public void deleteQuestionnaire(Long qaId) {
        Optional<QuestionnaireEntity> qaEntityOp = qaEntityRepository.findByIdOptional(qaId);
        if (!qaEntityOp.isEmpty()) {
            QuestionnaireEntity qaEntity = qaEntityOp.get();
            // 删除问卷
            qaEntityRepository.deleteById(qaId);
            // 删除反馈
            qaFeedbackEntityRepository.deleteBySurveyId(qaEntity.getPayloadSurveyId());
        }
    }

    /**
     * 查询问卷详情
     *
     * @param qaId 问卷id
     * @return 问卷
     */
    public QuestionnaireRes getQuestionnaire(Long qaId) {
        QuestionnaireEntity qaEntity = qaEntityRepository.findById(qaId);
        if (qaEntity == null) {
            LOG.warnv("questionnaire does not exist, questionnaireId:{0}", qaId);
            throw new ServiceOperationException(ServiceError.QUESTIONNAIRE_NOT_EXIST);
        }
        return qaEntityToRes(qaEntity);
    }

    /**
     * 查询问卷列表
     *
     * @param userDomain 用户域名
     * @param currentPage 当前页
     * @param pageSize 每页数量
     * @return 问卷列表
     */
    public PageListResult<QuestionnaireRes> listQuestionnaire(String userDomain, Integer currentPage, Integer pageSize) {
        List<QuestionnaireRes> qaResList = new ArrayList<>();
        // 判断，如果为空，则设置为1
        if (currentPage == null || currentPage <= 0) {
            currentPage = 1;
        }
        if (pageSize == null) {
            pageSize = 1000;
        }
        // 1.查询列表
        List<QuestionnaireEntity> qaEntities = qaEntityRepository.findAll().page(currentPage - 1, pageSize).list();
        qaEntities.forEach(qaEntity -> qaResList.add(qaEntityToRes(qaEntity)));
        // 2.记录总数
        Long totalCount = qaEntityRepository.count();
        // 3.查询用户反馈
        if (!CommonUtils.isNullOrEmpty(userDomain)) {
            List<QuestionnaireFeedbackEntity> qaFeedbackEntities = qaFeedbackEntityRepository.findByUserDomain(userDomain);
            Map<Long, QuestionnaireFeedbackEntity> qaFeedbackEntityMap = qaFeedbackEntities.stream().collect(
                    Collectors.toMap(QuestionnaireFeedbackEntity::getPayloadSurveyId, Function.identity(), (k1, k2) -> k2));
            // 设置状态
            qaResList.forEach(qaRes -> {
                OffsetDateTime now = OffsetDateTime.now();
                if (CommonUtils.isNotNull(qaRes.getStartAt()) && now.isBefore(qaRes.getStartAt())) {
                    qaRes.setState(QuestionnaireStateEnum.NOT_START.getName());
                } else if (CommonUtils.isNotNull(qaRes.getEndAt()) && now.isAfter(qaRes.getEndAt())) {
                    qaRes.setState(QuestionnaireStateEnum.HAS_END.getName());
                } else {
                    qaRes.setState(QuestionnaireStateEnum.IN_PROCESS.getName());
                }
                // 已反馈
                QuestionnaireFeedbackEntity qaFeedbackEntity = qaFeedbackEntityMap.get(qaRes.getPayloadSurveyId());
                if (qaFeedbackEntity != null) {
                    qaRes.setState(QuestionnaireStateEnum.COMPLETED.getName());
                }
            });
            // 排序：已结束（内部结束时间正序） > 待反馈 （内部结束时间正序） > 已反馈（内部结束时间倒序） > 未开始（内部开始时间正序），其中已结束状态页面显示的是待反馈。
            qaResList.sort(new QaComparator());
            qaResList.forEach(qaRes -> {
                if (QuestionnaireStateEnum.HAS_END.getName().equals(qaRes.getState())) {
                    qaRes.setState(QuestionnaireStateEnum.IN_PROCESS.getName());
                }
            });
        }
        return PageListResult.of(qaResList, PageInfo.of(totalCount, currentPage, pageSize));
    }

    @Transactional
    public FeedbackRes feedbackSave(FeedbackReq feedbackReq) {
        if (CommonUtils.isNull(feedbackReq.getPayload())) {
            LOG.infov("save questionnaire feedback begin, id:{0}, object:{1}, action:{2}, created_at:{3}", feedbackReq.getId(),
                    feedbackReq.getObject(), feedbackReq.getAction(), feedbackReq.getCreatedAt());
            return qaFeedbackReqToRes(feedbackReq);
        }
        LOG.infov("save questionnaire feedback begin, id:{0}, object:{1}, action:{2}, created_at:{3}, payload_survey_id:{4}, " +
                "payload_answer_id:{5}", feedbackReq.getId(), feedbackReq.getObject(), feedbackReq.getAction(), feedbackReq.getCreatedAt(),
                feedbackReq.getPayload().getSurveyId(), feedbackReq.getPayload().getAnswerId());
        FeedbackPayloadReq payload = feedbackReq.getPayload();
        // 查询问卷是否存在
        Optional<QuestionnaireEntity> qaEntityOp = qaEntityRepository.findBySurveyId(payload.getSurveyId());
        if (qaEntityOp.isEmpty()) {
            LOG.warnv("questionnaire does not exist, payloadSurveyId:{0}", payload.getSurveyId());
            throw new ServiceOperationException(ServiceError.QUESTIONNAIRE_NOT_EXIST);
        }
        // 反馈是否已存在
        Optional<QuestionnaireFeedbackEntity> qaFeedbackEntityOp = qaFeedbackEntityRepository.findByUserDomainAndSurveyAndAnswerId(payload.getOpenId(),
                payload.getSurveyId(), payload.getAnswerId());
        if (!qaFeedbackEntityOp.isEmpty()) {
            LOG.warnv("payload answer already exist, userDomain:{0}, payloadSurveyId:{1}, payloadAnswerId:{w}", payload.getOpenId(),
                    payload.getSurveyId(), payload.getAnswerId());
            throw new ServiceOperationException(ServiceError.PAYLOAD_ANSWER_ALREADY_EXIST);
        }
        OffsetDateTime answerAt = CommonUtils.dateTimeToOffsetDateTime(payload.getEndedAt());
        String answerDetail = operationUtils.objectToJson(payload.getAnswer());
        QuestionnaireFeedbackEntity qaFeedbackEntity = QuestionnaireFeedbackEntity.of(payload.getOpenId(),
                payload.getSurveyId(),
                payload.getAnswerId(),
                answerAt,
                answerDetail,
                null);
        qaFeedbackEntityRepository.persist(qaFeedbackEntity);
        LOG.infov("save questionnaire feedback succeed, id:{0}", feedbackReq.getId());
        return qaFeedbackReqToRes(feedbackReq);
    }

    private QuestionnaireRes qaEntityToRes(QuestionnaireEntity questionnaireEntity) {
        return QuestionnaireRes.of(questionnaireEntity.getId(),
                questionnaireEntity.getTitle(),
                questionnaireEntity.getContent(),
                questionnaireEntity.getStartAt(),
                questionnaireEntity.getEndAt(),
                questionnaireEntity.getPayloadSurveyId());
    }

    private QuestionnaireEntity qaReqToEntity(QuestionnaireReq questionnaireReq) {
        return QuestionnaireEntity.of(questionnaireReq.getTitle(),
                questionnaireReq.getContent(),
                questionnaireReq.getStartAt(),
                questionnaireReq.getEndAt(),questionnaireReq.getPayloadSurveyId());
    }

    private FeedbackRes qaFeedbackReqToRes(FeedbackReq feedbackReq) {
        return FeedbackRes.of(feedbackReq.getId(),
                feedbackReq.getObject(),
                feedbackReq.getAction(),
                feedbackReq.getCreatedAt());
    }

    private class QaComparator implements Comparator<QuestionnaireRes> {
        @Override
        public int compare(QuestionnaireRes q1, QuestionnaireRes q2) {
            QuestionnaireStateEnum qaEnum1 = QuestionnaireStateEnum.fromValue(q1.getState());
            QuestionnaireStateEnum qaEnum2 = QuestionnaireStateEnum.fromValue(q2.getState());
            // 按状态排列
            int result = qaEnum1.getOrder() - qaEnum2.getOrder();
            if (result == 0){
                switch (qaEnum1) {
                    case HAS_END:
                    case IN_PROCESS:
                        // 内部结束时间正序
                        if (CommonUtils.isNull(q1.getEndAt())) {
                            return 1;
                        } else if (CommonUtils.isNull(q2.getEndAt())){
                            return -1;
                        } else {
                            return q1.getEndAt().compareTo(q2.getEndAt());
                        }
                    case COMPLETED:
                        // 内部结束时间倒序
                        if (CommonUtils.isNull(q1.getEndAt())) {
                            return -1;
                        } else if (CommonUtils.isNull(q2.getEndAt())){
                            return 1;
                        } else {
                            return q2.getEndAt().compareTo(q1.getEndAt());
                        }
                    case NOT_START:
                        // 内部开始时间正序
                        if (CommonUtils.isNull(q1.getStartAt())) {
                            return 1;
                        } else if (CommonUtils.isNull(q2.getStartAt())){
                            return -1;
                        } else {
                            return q1.getStartAt().compareTo(q2.getStartAt());
                        }
                    default:
                        throw new UnsupportedOperationException();
                }
            }
            return result;
        }
    }
}
