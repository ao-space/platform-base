package xyz.eulix.platform.services.support.boundary.push;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

@Path("/")
@RegisterRestClient(configKey="upush-api")
public interface UPushService {

    @POST
    @Path("/api/send")
    PushMsgRes pushMessage(String umengNotification, @QueryParam("sign") String sign);
}
