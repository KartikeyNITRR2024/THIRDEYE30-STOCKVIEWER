package com.thirdeye3.stockviewer.services.impl;

import com.thirdeye3.stockviewer.services.PropertyService;
import com.thirdeye3.stockviewer.exceptions.StockException;
import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.services.StockService;
import com.thirdeye3.stockviewer.externalcontollers.StockManagerClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

@Service
public class StockServiceImpl implements StockService {
	
	private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);
	
	@Autowired
	private StockManagerClient stockManager;
	
	@Autowired
	private PropertyService propertyService;
	
    private Map<Long, Stock> stocks = null;
	
	@Override
    @Async("WebscrapperAsynchThread")
	public void updateMorningAndEveningPriceOfStocks(List<Stock> stocks1)
	{
		try {
			
			if (stocks == null || stocks.isEmpty()) {
	            fetchStocks();
	        }
			
			for(Stock stock : stocks1)
			{
				stock.setTodaysOpeningPrice(stock.getPrice());
				stock.setLastNightClosingPrice(stock.getPrice());
				if(stocks.get(stock.getUniqueId()).getTodaysOpeningPrice() == null)
				{
					stocks.get(stock.getUniqueId()).setTodaysOpeningPrice(stock.getPrice());
				}
			}
			Response<String> response = stockManager.updatePriceOfStock(stocks1);
	        if (response.isSuccess()) {   
	            logger.info("Morning and evening price of stocks is updated successfully.");
	        } else {
	            logger.error("Failed to update stocks price.");
	        }
		}
		catch(Exception ex)
		{
			logger.error("Failed to updated stock price in database " + ex.getMessage());
		}
	}
	
	@Override
    public void fetchStocks() {
        Response<Long> response = stockManager.getStockSize();
        if (response.isSuccess()) {
            Long totalStockSize = response.getResponse();
            Long totalPages = (long) Math.ceil((double) totalStockSize / propertyService.getSizeToLoadStock());
            logger.info("Total stocks: " + totalStockSize + " | Fetching in " + totalPages + " pages...");
            stocks = new HashMap<>();
            for (long page = 0; page < totalPages; page++) {
                Response<List<Stock>> response1 = stockManager.getStocks(page, propertyService.getSizeToLoadStock());
                if(response1.isSuccess())
                {
                	List<Stock> stockList = response1.getResponse();
	                for (Stock stock : stockList) {
	                    stocks.put(stock.getUniqueId(), stock);
	                }
                }
                else
                {
                	logger.error("Unable to fetch stock of page "+page);
                }
            }
            logger.info("Fetched " + stocks.size() + " stocks successfully.");
        } else {
        	stocks = new HashMap<>();
            throw new StockException("Unable to fetch properties from Property Manager");
        }
    }

    @Override
    public Stock getStockById(Long id) {
    	if(stocks == null)
    	{
    		fetchStocks();
    	}
        return stocks.get(id);
    }
}
