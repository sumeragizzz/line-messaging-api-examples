package examples.api.messaging.line.resource;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.linecorp.bot.client.LineMessagingServiceBuilder;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.CallbackRequest;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.MessageContent;
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
    public Response post(@Context HttpServletRequest request) {
        String channelSecret = System.getenv(CHANNEL_SECRET);
        String channelToken = System.getenv(CHANNEL_ACCESS_TOKEN);

        LineBotCallbackRequestParser lineBotCallbackRequestParser = new LineBotCallbackRequestParser(
                new LineSignatureValidator(channelSecret.getBytes()));
        try {
            CallbackRequest callbackRequest = lineBotCallbackRequestParser.handle(request);
            filterEvents(callbackRequest.getEvents(), MessageEvent.class).stream()
                    .forEach(event -> filterMessageContent(event.getMessage(), TextMessageContent.class)
                            .ifPresent(messageContent -> reply(channelToken, event.getReplyToken(), messageContent.getText())));
        } catch (LineBotCallbackException | IOException e) {
            e.printStackTrace();
        }
        return Response.ok().build();
    }

    private <T extends Event> List<T> filterEvents(List<Event> events, Class<T> clazz) {
        return events.stream().filter(t -> clazz.isAssignableFrom(t.getClass())).map(t -> clazz.cast(t)).collect(Collectors.toList());
    }

    private <T extends MessageContent> Optional<T> filterMessageContent(MessageContent messageContent, Class<T> clazz) {
        return Optional.ofNullable(messageContent).filter(t -> clazz.isAssignableFrom(t.getClass())).map(t -> clazz.cast(t));
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
