package info.bitrich.xchangestream.huobi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.bitrich.xchangestream.core.StreamingMarketDataService;
import info.bitrich.xchangestream.huobi.dto.HuobiOrderbook;
import info.bitrich.xchangestream.huobi.dto.marketdata.HuobiTrade;
import io.reactivex.Observable;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.Order.OrderType;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.marketdata.Trade;
import org.knowm.xchange.dto.marketdata.Trades;
import org.knowm.xchange.dto.marketdata.Trades.TradeSortType;
import org.knowm.xchange.huobi.dto.marketdata.HuobiDepth;
import org.knowm.xchange.huobi.dto.marketdata.HuobiTicker;
import org.knowm.xchange.huobi.dto.marketdata.results.HuobiTickerResult;
import org.knowm.xchange.dto.trade.*;

import java.math.BigDecimal;
import java.util.*;

public class HuobiStreamingMarketDataService implements StreamingMarketDataService {
	private static final String ORDERBOOK_SUB_TEMPLATE = "{\"sub\":\"market.$CURRENCYPAIR.depth.step0\",\"id\":\"id1\"}";
	private static final String TRADE_SUB_TEMPLATE = "{\"sub\":\"market.$CURRENCYPAIR.trade.detail\",\"id\":\"id1\"}";	
    
    private final HuobiStreamingService service;

    private final ObjectMapper mapper = new ObjectMapper();
    private final Map<CurrencyPair, HuobiOrderbook> orderbooks = new HashMap<>();
    
    HuobiStreamingMarketDataService(HuobiStreamingService service) {
        this.service = service;
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * market.ethbtc.depth.step0
     * 
     * @param currencyPair Currency pair of the order book
     * @param args         if the first arg is {@link FuturesContract} means future, the next arg is amount
     * @return
     */
    @Override
    public Observable<OrderBook> getOrderBook(CurrencyPair currencyPair, Object... args) {
		String channel = ORDERBOOK_SUB_TEMPLATE.replace("$CURRENCYPAIR", (currencyPair.base.toString().toLowerCase() + currencyPair.counter.toString().toLowerCase()));

        return service.subscribeChannel(channel)
                .map(s -> {
                    HuobiOrderbook huobiOrderbook = orderbooks.get(currencyPair);
                    if (huobiOrderbook == null) {
                        huobiOrderbook = new HuobiOrderbook();
                        orderbooks.put(currencyPair, huobiOrderbook);                        
                    } 
                     
                    if (s.get("tick").has("asks")) {
                        if (s.get("tick").get("asks").size() > 0) {
                            BigDecimal[][] askLevels = mapper.treeToValue(s.get("tick").get("asks"), BigDecimal[][].class);
                            huobiOrderbook.updateLevels(askLevels, Order.OrderType.ASK);
                        }
                    }

                    if (s.get("tick").has("bids")) {
                        if (s.get("tick").get("bids").size() > 0) {
                            BigDecimal[][] bidLevels = mapper.treeToValue(s.get("tick").get("bids"), BigDecimal[][].class);
                            huobiOrderbook.updateLevels(bidLevels, Order.OrderType.BID);
                        }
                    }

                    long ts = s.get("tick").get("ts").asLong();
                    return adaptOrderBook(huobiOrderbook.toHuobiDepth(ts), currencyPair, new Date(ts));
                });
    }

    /**
     * @param currencyPair Currency pair of the ticker
     * @param args
     * @return
     */
    @Override
    public Observable<Ticker> getTicker(CurrencyPair currencyPair, Object... args) {
    	//TODO
    	return null;
    }


    /**
     * market.ethbtc.trade.detail
     * 
     *
     * @param currencyPair Currency pair of the trades
     * @param args         the first arg {@link FuturesContract}
     * @return
     */
    @Override
    public Observable<Trade> getTrades(CurrencyPair currencyPair, Object... args) {
		String channel = TRADE_SUB_TEMPLATE.replace("$CURRENCYPAIR", (currencyPair.base.toString().toLowerCase() + currencyPair.counter.toString().toLowerCase()));

        return service.subscribeChannel(channel)
                .map(s -> {
                	HuobiTrade[] huobiTrades = mapper.treeToValue(s.get("tick").get("data"), HuobiTrade[].class);

                    return adaptTrades(huobiTrades, currencyPair);
                }).flatMapIterable(Trades::getTrades);
    }
    
    public static Trades adaptTrades(HuobiTrade[] huobiTrades, CurrencyPair currencyPair) {
        List<Trade> trades = new ArrayList<>(huobiTrades.length);

        for (int i = 0; i < huobiTrades.length; i++) {
          HuobiTrade trade = huobiTrades[i];

          OrderType type = trade.getSide().equals("buy") ? OrderType.BID : OrderType.ASK;

          Trade t =
              new Trade(
                  type,
                  trade.getSize(),
                  currencyPair,
                  trade.getPrice(),
                  new Date(Long.parseLong(trade.getTimestamp())),
                  String.valueOf(trade.getTradeId()));
          trades.add(t);
        }

        return new Trades(trades, huobiTrades[0].getTradeId(), TradeSortType.SortByID);
      }
    
    
	public static OrderBook adaptOrderBook(HuobiDepth book, CurrencyPair currencyPair, Date date) {
		List<LimitOrder> asks = toLimitOrderList(book.getId(), book.getAsks(), OrderType.ASK, currencyPair);
		List<LimitOrder> bids = toLimitOrderList(book.getId(), book.getBids(), OrderType.BID, currencyPair);

		return new OrderBook(date, asks, bids);
	}

	private static List<LimitOrder> toLimitOrderList(long ts, SortedMap<BigDecimal, BigDecimal> levels, OrderType orderType,
			CurrencyPair currencyPair) {

		List<LimitOrder> allLevels = new ArrayList<>();

		if (levels != null) {
			for (BigDecimal level : levels.keySet()) {
				allLevels.add(new LimitOrder(orderType, levels.get(level), currencyPair, null, new Date(ts), level));
			}
		}

		return allLevels;
	}
}
