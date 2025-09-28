package com.thirdeye3.stockviewer.services;

import java.util.List;

import com.thirdeye3.stockviewer.dtos.HoldedStock;

public interface HoldedStockService {

	void fetchHoldedStocks();

	List<HoldedStock> getHoldedStocks();

}
