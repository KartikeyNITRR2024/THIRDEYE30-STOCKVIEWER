package com.thirdeye3.stockviewer.services.impl;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thirdeye3.stockviewer.dtos.HoldedStock;
import com.thirdeye3.stockviewer.dtos.PriceChange;
import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.services.*;
import com.thirdeye3.stockviewer.utils.StocksPriceChangesCalculator;
import com.thirdeye3.stockviewer.utils.TimeManager;

@Service
public class WebscrapperServiceImpl implements WebscrapperService {

    private static final Logger logger = LoggerFactory.getLogger(WebscrapperServiceImpl.class);

    private ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> dataStoringMap = new ConcurrentHashMap<>();

    @Autowired
    private TimeManager timeManager;

    @Autowired
    private MachineService machineService;

    @Autowired
    private StocksPriceChangesCalculator stocksPriceChangesCalculator;

    @Autowired
    private StockService stockService;

    @Autowired
    private HoldedStockService holdedStockService;

    @Autowired
    private MessageBrokerService messageBrokerService;

    @Override
    public boolean processWebscrapper(List<Stock> stocks, Integer webscrapperId, String webscrapperCode) {
        machineService.validateMachine(webscrapperId, webscrapperCode);

        if (timeManager.allowPriceUpdate()) {
            logger.info("Updating stocks price in database.");
            stockService.updateMorningAndEveningPriceOfStocks(stocks);
        }

        List<Stock> changedStocks = new CopyOnWriteArrayList<>();

        logger.info("DataProcessing Starting time is : {}", timeManager.getCurrentTime());

        for (Stock stock : stocks) {
            if (stock.getUniqueId() != null && stock.getUniqueId() > 0
                    && stock.getPrice() != null && stock.getPrice() > 0) {

                CopyOnWriteArrayList<Stock> stockList = dataStoringMap.computeIfAbsent(
                        stock.getUniqueId(), k -> new CopyOnWriteArrayList<>());

                CompletableFuture<Stock> future =
                        stocksPriceChangesCalculator.calculateChanges(stock, stockList);

                Stock changedStock = future.join();
                if (changedStock != null) {
                    changedStocks.add(changedStock);
                }

                if (stockList.size() >= 100) {
                    stockList.remove(0);
                }
                stockList.add(new Stock(stock.getCurrentTime(), stock.getPrice()));
            }
        }

        logger.info("DataProcessing Ending time is : {}", timeManager.getCurrentTime());

        List<PriceChange> priceChangeList = changedStocks.stream()
                .flatMap(s -> s.getPriceChangeList().stream())
                .collect(Collectors.toList());

        logger.info("Total detected changes: {}", priceChangeList.size());
        messageBrokerService.sendMessages("thresold", priceChangeList);

        return machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
    }

    @Override
    public void clearMap() {
        logger.info("Refreshing all data.");
        dataStoringMap.clear();
    }
    
    @Override
    public ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> getDataStoringMap() {
		return dataStoringMap;
	}

	@Override
    public boolean processWebscrapper(Map<Long, Stock> stocks, Integer webscrapperId, String webscrapperCode) {
        machineService.validateMachine(webscrapperId, webscrapperCode);

        logger.info("DataProcessing Starting time is : {}", timeManager.getCurrentTime());

        List<HoldedStock> statusChangesStock = holdedStockService.getHoldedStocks().stream()
                .filter(holdedStock -> stocks.containsKey(holdedStock.getUniqueId()))
                .map(holdedStock -> stocksPriceChangesCalculator.calculateChanges(
                        stocks.get(holdedStock.getUniqueId()), holdedStock))
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("DataProcessing Ending time is : {}", timeManager.getCurrentTime());

        logger.info("Total stocks with detected changes: {}", statusChangesStock.size());
        messageBrokerService.sendMessages("holdedstock", statusChangesStock);

        return machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
    }
}
