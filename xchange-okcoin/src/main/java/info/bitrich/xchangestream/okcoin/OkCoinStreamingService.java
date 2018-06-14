package info.bitrich.xchangestream.okcoin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.okcoin.dto.WebSocketMessage;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;

public class OkCoinStreamingService extends JsonNettyStreamingService {
	public static final String PING = "{'event':'ping'}";
	public static final String PONG = "pong";

    public OkCoinStreamingService(String apiUrl) {
        super(apiUrl);
          
        long delay  = 5000L;
        long period = 5000L;
        java.util.Timer timer = new java.util.Timer("PingTimer");
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            public void run() {
            	sendMessage(PING);
            }
        }, delay, period);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        return message.get("channel").asText();
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        WebSocketMessage webSocketMessage = new WebSocketMessage("addChannel", channelName, 0);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(webSocketMessage);
    }

    @Override
    public String getUnsubscribeMessage(String channelName) throws IOException {
        WebSocketMessage webSocketMessage = new WebSocketMessage("removeChannel", channelName, 0);

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(webSocketMessage);
    }

    @Override
    protected void handleMessage(JsonNode message) {
    	JsonNode eventMessage = message.get("event");
    	
        if (eventMessage != null && PONG.equals(eventMessage.textValue())) {
        	return;
        } else if (message.get("data") != null) {
            if (message.get("data").has("result")) {
                boolean success = message.get("data").get("result").asBoolean();
                if (!success) {
                    super.handleError(message, new ExchangeException("Error code: " + message.get("errorcode").asText()));
                }
                return;
            }
        }
        super.handleMessage(message);
    }
}
