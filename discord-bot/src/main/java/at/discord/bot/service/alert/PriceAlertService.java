package at.discord.bot.service.alert;

import at.discord.bot.persistent.PriceAlertRepository;
import at.discord.bot.persistent.model.PriceAlertEntity;
import at.discord.bot.service.binance.symbol.SymbolPriceMonitorService;
import at.discord.bot.service.candle.BarSeriesHolderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceAlertService {

    private final PriceAlertRepository priceAlertRepository;
    private final BarSeriesHolderService barSeriesHolderService;
    private final SymbolPriceMonitorService symbolPriceMonitorService;
    private final AlertMessagingService alertMessagingService;

    @Transactional
    public synchronized boolean registerAlert(String symbol, BigDecimal price) {

        List<PriceAlertEntity> bySymbolAndPrice = priceAlertRepository.findBySymbolAndPrice(symbol, price.toString());
        if (!bySymbolAndPrice.isEmpty()) {
            return false;
        }

        PriceAlertEntity priceAlertEntity = new PriceAlertEntity();
        priceAlertEntity.setPrice(price.toString());
        priceAlertEntity.setLastCheckedPrice(price.toString());
        priceAlertEntity.setSymbol(symbol);
        priceAlertRepository.save(priceAlertEntity);
        symbolPriceMonitorService.registerSymbol(symbol);
        return true;
    }

    @Transactional
    public synchronized boolean removeAlert(String symbol, BigDecimal price) {

        List<PriceAlertEntity> bySymbolAndPrice = priceAlertRepository.findBySymbolAndPrice(symbol, price.toString());
        if (bySymbolAndPrice.isEmpty()) {
            return false;
        }

        priceAlertRepository.deleteAll(bySymbolAndPrice);
        return true;
    }

    @Transactional
    public synchronized List<PriceAlertEntity> listAlerts() {
        return priceAlertRepository.findAll();
    }

    @Scheduled(cron = "*/20 * * * * *")
    @Transactional
    public void checkForPriceAlerts() {
        List<PriceAlertEntity> allAlerts = priceAlertRepository.findAll();
        allAlerts.forEach(this::processPriceAlert);
    }

    private void processPriceAlert(PriceAlertEntity priceAlertEntity) {
        String symbol = priceAlertEntity.getSymbol();
        Num triggerPrice = DecimalNum.valueOf(new BigDecimal(priceAlertEntity.getPrice()));
        Num previousPrice = DecimalNum.valueOf(new BigDecimal(priceAlertEntity.getLastCheckedPrice()));

        BarSeries barSeries = barSeriesHolderService.getBarSeries(symbol);
        if (barSeries == null) {
            symbolPriceMonitorService.registerSymbol(symbol);
            return;
        }
        Num currentPrice = barSeries.getLastBar().getClosePrice();

        if (triggerPrice.isLessThanOrEqual(currentPrice) && previousPrice.isLessThan(triggerPrice)) {
            alertMessagingService.sendCrossAboveAlert(currentPrice, priceAlertEntity);
        } else if (triggerPrice.isGreaterThanOrEqual(currentPrice) && previousPrice.isGreaterThan(triggerPrice)) {
            alertMessagingService.sendCrossBelowAlert(currentPrice, priceAlertEntity);
        }

        priceAlertEntity.setLastCheckedPrice(currentPrice.toString());
        priceAlertRepository.save(priceAlertEntity);
    }

}
