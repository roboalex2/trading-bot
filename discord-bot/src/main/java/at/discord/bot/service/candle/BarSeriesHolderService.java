package at.discord.bot.service.candle;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BarSeriesHolderService {

    private Map<String, BarSeries> symbolSeries = new HashMap<>();

    public synchronized void updateBarSeries(String symbol, BaseBar baseBar) {
        BarSeries barSeries = symbolSeries.computeIfAbsent(symbol, key -> new BaseBarSeries("1s", new ArrayList<Bar>(List.of(baseBar))));
        barSeries.setMaximumBarCount(200000);

        if (barSeries.getLastBar().getEndTime().toEpochSecond() >= baseBar.getEndTime().toEpochSecond()) {
            barSeries.getLastBar().addPrice(baseBar.getClosePrice());
        } else {
            barSeries.addBar(baseBar);
        }
    }

    public BarSeries getBarSeries(String symbol) {
        return symbolSeries.get(symbol);
    }
}
