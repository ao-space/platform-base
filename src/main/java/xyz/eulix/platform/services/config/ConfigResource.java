package xyz.eulix.platform.services.config;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

@Path("/config")
public class ConfigResource {

    @Inject
    ApplicationProperties properties;

    @GET
    @Path("supersonic")
    @Produces(MediaType.TEXT_PLAIN)
    public String supersonic() {
        final int mach = properties.getDisplayMach().orElse(1);
        final BigDecimal speed = BigDecimal.valueOf(properties.getSpeedOfSound())
            .multiply(properties.getDisplayUnitFactor())
            .multiply(BigDecimal.valueOf(mach));
        return String.format("Mach %d is %.3f %s",
            mach,
            speed,
            properties.getDisplayUnitName()
        );
    }
}
