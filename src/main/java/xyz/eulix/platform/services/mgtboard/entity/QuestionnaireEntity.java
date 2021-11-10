package xyz.eulix.platform.services.mgtboard.entity;

import lombok.*;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;


@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "questionnaire")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class QuestionnaireEntity extends BaseEntity {

    @NotNull
    @Column(name = "title")
    private String title;

    // 内容(地址)
    @NotNull
    @Column(name = "content")
    private String content;

    @Column(name = "start_at")
    private OffsetDateTime startAt;

    @Column(name = "end_at")
    private OffsetDateTime endAt;

    // 第三方问卷id
    @NotNull
    @Column(name = "payload_survey_id")
    private Long payloadSurveyId;

}
