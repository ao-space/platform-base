package xyz.eulix.platform.services.support.model;

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
 *   <li><tt>id</tt>: used to define the primary key of database.</li>
 *   <li><tt>created_at</tt>: used to define the create time of entity.
 *   it will be added at first creation.</li>
 *   <li><tt>updated_at</tt>: used to define the update time of entity,
 *   it will be updated for every update.</li>
 *   <li><tt>version</tt>: used to define current version of entity,
 *   it will used to avoid conflict in an optimistic way.</li>
 * </il>
 * <p>
 * By default, these properties can be maintained properly in an ORM framework.
 */
@Getter @Setter @ToString
@MappedSuperclass
public abstract class BaseEntity implements Serializable{
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
