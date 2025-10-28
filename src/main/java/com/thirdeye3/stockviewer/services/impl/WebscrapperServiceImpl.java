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
        logger.info("==== Starting Webscrapper Processing ====");
        logger.info("Webscrapper ID: {} | Code: {}", webscrapperId, webscrapperCode);
        logger.info("Received {} stock entries", stocks != null ? stocks.size() : 0);

        // Validate machine
        machineService.validateMachine(webscrapperId, webscrapperCode);
        logger.info("Machine validation completed successfully.");

        // Update DB prices if allowed
        if (timeManager.allowPriceUpdate()) {
            logger.info("Allowed time window detected — updating stock prices in DB at {}", timeManager.getCurrentTime());
            stockService.updateMorningAndEveningPriceOfStocks(stocks);
        } else {
            logger.info("Price update skipped — not in allowed time window.");
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

            if (processed % 100 == 0) {
                logger.info("Processed {} stocks so far...", processed);
            }
        }

        logger.info("Data Processing finished at {} | Total processed: {}", 
                    timeManager.getCurrentTime(), processed);

        List<PriceChange> priceChangeList = changedStocks.stream()
                .flatMap(s -> s.getPriceChangeList().stream())
                .collect(Collectors.toList());

        logger.info("Total detected price changes: {}", priceChangeList.size());
        messageBrokerService.sendMessages("thresold", priceChangeList);
        logger.info("Sent {} messages to broker topic 'thresold'", priceChangeList.size());

        boolean updateRequired = machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
        logger.info("Machine update required: {}", updateRequired);
        logger.info("==== Webscrapper Processing Completed ====");

        return updateRequired;
    }

    @Override
    public void clearMap() {
        logger.info("Clearing data map. Current size: {}", dataStoringMap.size());
        dataStoringMap.clear();
        logger.info("Data map cleared successfully at {}", timeManager.getCurrentTime());
    }

    @Override
    public ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> getDataStoringMap() {
        logger.info("Returning data map with {} entries", dataStoringMap.size());
        return dataStoringMap;
    }

    @Override
    public boolean processWebscrapper(Map<Long, Stock> stocks, Integer webscrapperId, String webscrapperCode) {
        logger.info("==== Starting HoldedStock Processing ====");
        logger.info("Webscrapper ID: {} | Incoming map size: {}", webscrapperId, stocks.size());

        machineService.validateMachine(webscrapperId, webscrapperCode);
        logger.info("Machine validation successful for HoldedStock processing.");

        logger.info("Processing HoldedStocks at {}", timeManager.getCurrentTime());

        List<HoldedStock> statusChangesStock = holdedStockService.getHoldedStocks().stream()
                .filter(holdedStock -> stocks.containsKey(holdedStock.getUniqueId()))
                .map(holdedStock -> stocksPriceChangesCalculator.calculateChanges(
                        stocks.get(holdedStock.getUniqueId()), holdedStock))
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        logger.info("HoldedStock processing finished at {}", timeManager.getCurrentTime());
        logger.info("Detected {} holded stock changes", statusChangesStock.size());

        messageBrokerService.sendMessages("holdedstock", statusChangesStock);
        logger.info("Sent {} messages to broker topic 'holdedstock'", statusChangesStock.size());

        boolean updateRequired = machineService.isUpdateMachineRequiredNeeded(webscrapperId, webscrapperCode);
        logger.info("Machine update required (HoldedStock): {}", updateRequired);
        logger.info("==== HoldedStock Processing Completed ====");

        return updateRequired;
    }
}
