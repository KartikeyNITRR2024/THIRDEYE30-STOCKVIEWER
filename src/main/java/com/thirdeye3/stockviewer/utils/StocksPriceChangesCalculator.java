package com.thirdeye3.stockviewer.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.thirdeye3.stockviewer.dtos.*;
import com.thirdeye3.stockviewer.services.*;

@Component
public class StocksPriceChangesCalculator {

    private static final Logger logger = LoggerFactory.getLogger(StocksPriceChangesCalculator.class);

    @Autowired private PropertyService propertyService;
    @Autowired private ThresholdService thresholdService;
    @Autowired private StockService stockService;
    @Autowired private TimeManager timeManager;

    private boolean searchStockInGroupList(String data, Long long1) {
        String targetStr = String.format("%05d", long1);
        int n = data.length() / 5;

        int left = 0;
        int right = n - 1;

        while (left <= right) {
            int mid = (left + right) / 2;
            int startIndex = mid * 5;
            String midValue = data.substring(startIndex, startIndex + 5);

            int comparison = midValue.compareTo(targetStr);
            if (comparison == 0) {
                return true;
            } else if (comparison < 0) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return false;
    }

    public Stock findNearestTimestamp(List<Stock> pastData, Timestamp searchTime) {
        int left = 0;
        int right = pastData.size() - 1;

        while (left <= right) {
            int mid = left + (right - left) / 2;
            Timestamp midTime = pastData.get(mid).getCurrentTime();
            if (midTime.before(searchTime)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }

        if (left < pastData.size() && right >= 0) {
            long diffLeft = Math.abs(pastData.get(left).getCurrentTime().getTime() - searchTime.getTime());
            long diffRight = Math.abs(pastData.get(right).getCurrentTime().getTime() - searchTime.getTime());
            return diffLeft < diffRight ? pastData.get(left) : pastData.get(right);
        } else if (left < pastData.size()) {
            return pastData.get(left);
        } else if (right >= 0) {
            return pastData.get(right);
        } else {
            return null;
        }
    }

    @Async("WebscrapperAsynchThread")
    public CompletableFuture<Stock> calculateChanges(Stock stock, List<Stock> pastData) {
        if (pastData == null) {
            logger.info("Skipping stock {} — past data is null", stock.getUniqueId());
            return CompletableFuture.completedFuture(null);
        }

        Long limitInTimeSeconds = propertyService.getBufferTimeGapInSeconds() / 2;

        for (Long thresholdGroupId : thresholdService.getthresholdGroup().keySet()) {
            ThresholdGroupDto thresholdGroup = thresholdService.getthresholdGroup().get(thresholdGroupId);
            if (!thresholdGroup.getAllStocks() && 
                !searchStockInGroupList(thresholdGroup.getStockList(), stock.getUniqueId())) {
                continue;
            }

            for (ThresholdDto threshold : thresholdGroup.getThresholds()) {
                Double gapIs = null;
                Boolean timeLimit = true;
                Stock stockToCheckFor = stockService.getStockById(stock.getUniqueId());
                Long currentTimeGap = threshold.getTimeGapInSeconds();

                if (stockToCheckFor == null) {
  
                    continue;
                }

                try {
                    if (threshold.getTimeGapInSeconds() != -1 && threshold.getTimeGapInSeconds() != -2) {
                        Timestamp timeToCheckFor = new Timestamp(
                                stock.getCurrentTime().getTime() - threshold.getTimeGapInSeconds() * 1000);
                        stockToCheckFor = findNearestTimestamp(pastData, timeToCheckFor);

                        if (stockToCheckFor == null) continue;

                        Long upperLimit = threshold.getTimeGapInSeconds() + limitInTimeSeconds;
                        Long lowerLimit = threshold.getTimeGapInSeconds() - limitInTimeSeconds;
                        currentTimeGap = (stock.getCurrentTime().getTime() - 
                                          stockToCheckFor.getCurrentTime().getTime()) / 1000;

                        timeLimit = currentTimeGap <= upperLimit && currentTimeGap >= lowerLimit;
                        gapIs = stock.getPrice() - stockToCheckFor.getPrice();

                        if (threshold.getType() == 0) {
                            gapIs = (gapIs / stockToCheckFor.getPrice()) * 100;
                        }
                    } 
                    else if (threshold.getTimeGapInSeconds() == -1 && 
                             stockToCheckFor.getLastNightClosingPrice() != null) {
                        gapIs = stock.getPrice() - stockToCheckFor.getLastNightClosingPrice();
                        if (threshold.getType() == 0) {
                            gapIs = (gapIs / stockToCheckFor.getLastNightClosingPrice()) * 100;
                        }
                    } 
                    else if (threshold.getTimeGapInSeconds() == -2 && 
                             timeManager.isBetweenMorningEndAndEveningEnd() &&
                             stockToCheckFor.getTodaysOpeningPrice() != null) {
                        gapIs = stock.getPrice() - stockToCheckFor.getTodaysOpeningPrice();
                        if (threshold.getType() == 0) {
                            gapIs = (gapIs / stockToCheckFor.getTodaysOpeningPrice()) * 100;
                        }
                    }

                    if (currentTimeGap != null && gapIs != null && 
                        gapIs >= threshold.getPriceGap() && timeLimit) {
                        if (stock.getPriceChangeList() == null) {
                            stock.setPriceChangeList(new ArrayList<>());
                        }

                        PriceChange priceChange = new PriceChange(
                                thresholdGroupId,
                                stock.getUniqueId(),
                                BigDecimal.valueOf(gapIs)
                                          .setScale(3, RoundingMode.HALF_UP)
                                          .doubleValue(),
                                threshold.getType(),
                                currentTimeGap,
                                stockToCheckFor.getPrice(),
                                stock.getPrice(),
                                stock.getCurrentTime());

                        stock.getPriceChangeList().add(priceChange);
          
                    }
                } catch (Exception e) {
                    logger.info("Error calculating change for stock {} in group {}: {}", 
                                stock.getUniqueId(), thresholdGroupId, e.getMessage());
                }
            }
        }

        if (stock.getPriceChangeList() != null && !stock.getPriceChangeList().isEmpty()) {
            return CompletableFuture.completedFuture(stock);
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("WebscrapperAsynchThread")
    public CompletableFuture<HoldedStock> calculateChanges(Stock currentStock, HoldedStock holdedStock) {
        Double currentStatus = currentStock.getPrice();

        if (holdedStock.getType() == 0) {
            currentStatus = ((currentStatus - holdedStock.getBuyingPrice()) / 
                             holdedStock.getBuyingPrice()) * 100;
        }

        Long currentstatusId = Long.MIN_VALUE;
        int n = holdedStock.getStatus().size();

        for (int i = 0; i < n - 1; i++) {
            if (currentStatus >= holdedStock.getStatus().get(i).getStatus() &&
                currentStatus < holdedStock.getStatus().get(i + 1).getStatus()) {
                currentstatusId = holdedStock.getStatus().get(i).getStatusId();
            }
        }

        if (currentStatus >= holdedStock.getStatus().get(n - 1).getStatus()) {
            currentstatusId = holdedStock.getStatus().get(n - 1).getStatusId();
        }

        if (!currentstatusId.equals(holdedStock.getCurrentStatus())) {
                     holdedStock.setCurrentStatus(currentstatusId);
            return CompletableFuture.completedFuture(holdedStock);
        }

     
        return CompletableFuture.completedFuture(null);
    }
}
