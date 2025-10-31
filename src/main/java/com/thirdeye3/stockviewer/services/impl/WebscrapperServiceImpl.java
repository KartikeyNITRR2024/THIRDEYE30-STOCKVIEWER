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

    private final ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> dataStoringMap = new ConcurrentHashMap<>();

    @Autowired private TimeManager timeManager;
    @Autowired private MachineService machineService;
    @Autowired private StocksPriceChangesCalculator stocksPriceChangesCalculator;
    @Autowired private StockService stockService;
    @Autowired private HoldedStockService holdedStockService;
    @Autowired private MessageBrokerService messageBrokerService;

    @Override
    public boolean processWebscrapper(List<Stock> stocks, Integer webscrapperId, String webscrapperCode) {
        machineService.validateMachine(webscrapperId, webscrapperCode);
        if (timeManager.allowPriceUpdate()) {
            logger.info("Allowed time window detected — updating stock prices in DB at {}", timeManager.getCurrentTime());
            stockService.updateMorningAndEveningPriceOfStocks(stocks);
        } 

        List<Stock> changedStocks = new CopyOnWriteArrayList<>();

        logger.info("Data Processing started at {}", timeManager.getCurrentTime());

        int processed = 0;
        for (Stock stock : stocks) {
            processed++;

            if (stock.getUniqueId() == null || stock.getUniqueId() <= 0 ||
                stock.getPrice() == null || stock.getPrice() <= 0) {
                continue;
            }

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

        logger.info("Data Processing finished at {} | Total processed: {}", 
                    timeManager.getCurrentTime(), processed);

        List<PriceChange> priceChangeList = changedStocks.stream()
                .flatMap(s -> s.getPriceChangeList().stream())
                .collect(Collectors.toList());
        messageBrokerService.sendMessages("thresold", priceChangeList);

        boolean updateRequired = machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
        return updateRequired;
    }

    @Override
    public void clearMap() {
        dataStoringMap.clear();
    }

    @Override
    public ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> getDataStoringMap() {
        return dataStoringMap;
    }

    @Override
    public boolean processWebscrapper(Map<Long, Stock> stocks, Integer webscrapperId, String webscrapperCode) {


        machineService.validateMachine(webscrapperId, webscrapperCode);

        logger.info("Processing HoldedStocks at {}", timeManager.getCurrentTime());

        List<HoldedStock> statusChangesStock = holdedStockService.getHoldedStocks().stream()
                .filter(holdedStock -> stocks.containsKey(holdedStock.getUniqueId()))
                .map(holdedStock -> stocksPriceChangesCalculator.calculateChanges(
                        stocks.get(holdedStock.getUniqueId()), holdedStock))
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());


        messageBrokerService.sendMessages("holdedstock", statusChangesStock);

        boolean updateRequired = machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
        return updateRequired;
    }
}
