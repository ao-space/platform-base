package xyz.eulix.platform.services.support.model;

import lombok.Data;

/**
 * Used to define a REST response for querying the server {@code status}.
 */
@Data(staticConstructor = "of")
public class StatusResult {
  private final String status;
  private final String version;
}
