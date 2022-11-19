/*
 * Copyright (c) 2022 Institute of Software Chinese Academy of Sciences (ISCAS)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.eulix.platform.common.support.model;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * Used to define a base entity from which you can derive your own entity.
 * It provides many basic properties as a domain entity.
 * <p>
 * These properties are:
 * <il>
 * <li><tt>id</tt>: used to define the primary key of database.</li>
 * <li><tt>created_at</tt>: used to define the create time of entity.
 * it will be added at first creation.</li>
 * <li><tt>updated_at</tt>: used to define the update time of entity,
 * it will be updated for every update.</li>
 * <li><tt>version</tt>: used to define current version of entity,
 * it will used to avoid conflict in an optimistic way.</li>
 * </il>
 * <p>
 * By default, these properties can be maintained properly in an ORM framework.
 */
@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class BaseEntity implements Serializable {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ExcelProperty(index = 0)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column(name = "version")
    @Version
    private Integer version;
}
