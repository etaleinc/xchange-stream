package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.service.netty.JsonNettyStreamingService;
import org.knowm.xchange.exceptions.ExchangeException;

import java.io.IOException;

public class HuobiStreamingService extends JsonNettyStreamingService {
	private static final String ORDERBOOK_SUB_TEMPLATE = "{\"sub\":\"$CHANNEL\",\"id\":\"id1\"}";
	
	public static final String PING = "{'event':'ping'}";
	public static final String PONG = "pong";

    public HuobiStreamingService(String apiUrl) {
        super(apiUrl);
    }

    @Override
    protected String getChannelNameFromMessage(JsonNode message) throws IOException {
        return ORDERBOOK_SUB_TEMPLATE.replace("$CHANNEL", message.get("ch").asText());
    }

    @Override
    public String getSubscribeMessage(String channelName, Object... args) throws IOException {
        return channelName;
    }

    @Override
    public String getUnsubscribeMessage(String channelName) throws IOException {
    	return channelName;
    }

    @Override
    protected void handleMessage(JsonNode message) {
    	JsonNode pingMessage = message.get("ping");
        if (pingMessage != null) {
    		String msg = message.toString().replace("ping", "pong");
    		sendMessage(msg);
        	return;
        } else if (message.get("ch") != null) {
            if (message.get("ch").textValue().indexOf("depth") >= 0) {
            	super.handleMessage(message);
                return;
            } else if (message.get("ch").textValue().indexOf("trade.detail") >= 0) {
            	super.handleMessage(message);
            	return;
            }
        }
        //super.handleMessage(message);
    }
}
