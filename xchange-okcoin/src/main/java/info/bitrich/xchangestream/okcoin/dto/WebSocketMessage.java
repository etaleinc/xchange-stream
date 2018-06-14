package info.bitrich.xchangestream.okcoin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WebSocketMessage {
    private final String event;
    private final String channel;
    private final int binary;

    public WebSocketMessage(@JsonProperty("event") String event, @JsonProperty("channel") String channel,
    		@JsonProperty("binary") int binary) {
        this.event = event;
        this.channel = channel;
        this.binary = binary;
    }

    public String getEvent() {
        return event;
    }

    public String getChannel() {
        return channel;
    }
    
    public int getBinary() {
        return binary;
    }
}
