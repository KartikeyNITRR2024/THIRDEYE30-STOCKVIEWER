package com.thirdeye3.stockviewer.services;

import java.util.List;

import com.thirdeye3.stockviewer.dtos.Stock;

public interface StockService {

	void updateMorningAndEveningPriceOfStocks(List<Stock> stocks);

	void fetchStocks();

	Stock getStockById(Long id);

}
