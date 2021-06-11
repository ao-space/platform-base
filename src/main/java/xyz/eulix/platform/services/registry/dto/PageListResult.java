package xyz.eulix.platform.services.registry.dto;

import lombok.Data;

import java.util.List;

/**
 * Used to define a group of properties to wrap the paged list result within a REST response.
 * @param <T> the element type of list
 */
@Data(staticConstructor = "of")
public class PageListResult<T> {
  private final List<T> list;
  private final PageInfo pageInfo;
}
