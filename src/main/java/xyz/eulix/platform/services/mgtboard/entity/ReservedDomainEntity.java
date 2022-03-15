package xyz.eulix.platform.services.mgtboard.entity;

import lombok.*;
import xyz.eulix.platform.services.support.model.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@ToString(callSuper = true)
@Entity
@Table(name = "reserved_domain")
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class ReservedDomainEntity extends BaseEntity {
    @NotNull
    @Column(name = "regex")
    private String regex;

    @NotNull
    @Column(name = "description")
    private String description;
}
