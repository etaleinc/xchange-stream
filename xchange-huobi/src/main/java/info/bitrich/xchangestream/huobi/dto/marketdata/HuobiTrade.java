package info.bitrich.xchangestream.huobi.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

public class HuobiTrade {
    private final String timestamp;
    private final long tradeId;
    private final BigDecimal price;
    private final BigDecimal size;
    private final String side;
    
    
    /**
     * @param timestamp
     * @param tradeId
     * @param price
     * @param size
     * @param side
     */
    public HuobiTrade(
        @JsonProperty("ts") String timestamp,
        @JsonProperty("id") long tradeId,
        @JsonProperty("price") BigDecimal price,
        @JsonProperty("amount") BigDecimal size,
        @JsonProperty("direction") String side) {

        this.timestamp = timestamp;
        this.tradeId = tradeId;
        this.price = price;
        this.size = size;
        this.side = side;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getTradeId() {
        return tradeId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSize() {
        return size;
    }

    public String getSide() {
        return side;
    }

    @Override
    public String toString() {
        return "HuobiTrade [timestamp="
          + timestamp
          + ", tradeId="
          + tradeId
          + ", price="
          + price
          + ", size="
          + size
          + ", side="
          + side
          + "]";
    }
}