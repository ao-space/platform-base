package xyz.eulix.platform.services.support.model;

import lombok.Data;

/**
 * Used to define a group of properties to wrap the page info within a REST query request.
 */
@Data(staticConstructor = "of")
public class PageInfo {
  private final long total;
  private final int page;
  private final int pageSize;
}
