package examples.api.messaging.line.resource;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.servlet.LineBotCallbackException;
import com.linecorp.bot.servlet.LineBotCallbackRequestParser;

@Path("callback")
public class CallbackResource {

    private static final String CHANNEL_SECRET = "CHANNEL_SECRET";
    private static final String CHANNEL_ACCESS_TOKEN = "CHANNEL_ACCESS_TOKEN";

    @POST
    @Consumes("application/json")
    @SuppressWarnings("unchecked")
    public Response post(@Context HttpServletRequest req) {
        String channelSecret = System.getenv(CHANNEL_SECRET);
        String channelToken = System.getenv(CHANNEL_ACCESS_TOKEN);

        LineBotCallbackRequestParser lineBotCallbackRequestParser = new LineBotCallbackRequestParser(
                new LineSignatureValidator(channelSecret.getBytes()));
        try {
            lineBotCallbackRequestParser.handle(req)
                    .getEvents()
                    .stream()
                    .filter(t -> t instanceof MessageEvent && MessageEvent.class.cast(t).getMessage() instanceof TextMessageContent)
                    .map(t -> (MessageEvent<TextMessageContent>) t)
                    .findFirst()
                    .ifPresent(t -> reply(channelToken, t.getReplyToken(), t.getMessage().getText()));

        } catch (LineBotCallbackException | IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    private void reply(String channelToken, String replyToken, String textMessage) {
        ReplyMessage replyMessage = new ReplyMessage(replyToken, new TextMessage(textMessage));
        try {
            retrofit2.Response<BotApiResponse> response = LineMessagingServiceBuilder
                    .create(channelToken)
                    .build()
                    .replyMessage(replyMessage)
                    .execute();
            response.message();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
