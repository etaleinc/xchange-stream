package info.bitrich.xchangestream.huobi.dto;

import org.knowm.xchange.dto.Order;
import org.knowm.xchange.huobi.dto.marketdata.HuobiDepth;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Lukas Zaoralek on 16.11.17.
 */
public class HuobiOrderbook {
    private final BigDecimal zero = new BigDecimal(0);

    private final SortedMap<BigDecimal, BigDecimal[]> asks;
    private final SortedMap<BigDecimal, BigDecimal[]> bids;

    public HuobiOrderbook() {
        asks = new TreeMap<>(java.util.Collections.reverseOrder());
        bids = new TreeMap<>();
    }


    public void createFromDepth(HuobiDepth depth) {
    	SortedMap<BigDecimal,BigDecimal> depthAsks = depth.getAsks();
    	SortedMap<BigDecimal,BigDecimal> depthBids = depth.getBids();

        createFromDepthLevels(depthAsks, Order.OrderType.ASK);
        createFromDepthLevels(depthBids, Order.OrderType.BID);
    }

    public void createFromDepthLevels(SortedMap<BigDecimal,BigDecimal> depthLevels, Order.OrderType side) {
        SortedMap<BigDecimal, BigDecimal[]> orderbookLevels = side == Order.OrderType.ASK ? asks : bids;
        for (BigDecimal level : depthLevels.keySet()) {
            orderbookLevels.put(level, new BigDecimal[]{level, depthLevels.get(level)});
        }
    }

    public void updateLevels(BigDecimal[][] depthLevels, Order.OrderType side) {
        SortedMap<BigDecimal, BigDecimal[]> orderBookSide = side == Order.OrderType.ASK ? asks : bids;
        orderBookSide.clear();

        for (BigDecimal[] level : depthLevels) {
        	orderBookSide.put(level[0], level);
        }
    }

    public List<BigDecimal[]> getSide(Order.OrderType side) {
        SortedMap<BigDecimal, BigDecimal[]> orderbookLevels = side == Order.OrderType.ASK ? asks : bids;
        Collection<BigDecimal[]> levels = orderbookLevels.values();
        return new ArrayList(levels);
    }

    public List<BigDecimal[]> getAsks() {
        return getSide(Order.OrderType.ASK);
    }

    public List<BigDecimal[]> getBids() {
        return getSide(Order.OrderType.BID);
    }

    public HuobiDepth toHuobiDepth(long id) {
        return new HuobiDepth(id, getBids(), getAsks());
    }
}
