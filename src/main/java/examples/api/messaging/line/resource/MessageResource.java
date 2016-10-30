package examples.api.messaging.line.resource;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.model.PushMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;

@Path("message")
public class MessageResource {

    private static final String CHANNEL_ACCESS_TOKEN = "CHANNEL_ACCESS_TOKEN";

    @POST
    public Response pushMessage(@QueryParam("to") String to, @QueryParam("textMessage") String textMessage) {
        String channelToken = System.getenv(CHANNEL_ACCESS_TOKEN);
        push(channelToken, to, textMessage);
        return Response.ok().build();
    }

    public void push(String channelToken, String to, String textMessage) {
        try {
            retrofit2.Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(channelToken)
                    .build()
                    .pushMessage(new PushMessage(to, new TextMessage(textMessage)))
                    .execute();
            response.message();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
