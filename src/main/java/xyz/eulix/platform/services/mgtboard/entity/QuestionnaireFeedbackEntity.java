package xyz.eulix.platform.services.mgtboard.entity;

import lombok.*;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "questionnaire_feedback")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class QuestionnaireFeedbackEntity extends BaseEntity {

    @NotNull
    @Column(name = "subdomain")
    private String subdomain;

    // 第三方问卷id
    @NotNull
    @Column(name = "payload_survey_id")
    private Long payloadSurveyId;

    // 第三方问卷答案id
    @NotNull
    @Column(name = "payload_answer_id")
    private Long payloadAnswerId;

    @Column(name = "payload_answer_at")
    private OffsetDateTime payloadAnswerAt;

    // 用户答案详情,json格式
    @Column(name = "payload_answer_detail")
    private String payloadAnswerDetail;

    // 预留json格式
    @Column(name = "extra")
    private String extra;
}
