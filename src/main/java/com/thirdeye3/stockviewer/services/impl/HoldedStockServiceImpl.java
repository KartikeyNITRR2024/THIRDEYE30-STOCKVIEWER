package com.thirdeye3.stockviewer.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thirdeye3.stockviewer.dtos.HoldedStock;
import com.thirdeye3.stockviewer.services.HoldedStockService;
import org.springframework.stereotype.Service;

@Service
public class HoldedStockServiceImpl implements HoldedStockService {
	private static final Logger logger = LoggerFactory.getLogger(HoldedStockServiceImpl.class);
	
	private List<HoldedStock> holdedStocks = null;
	
	 @Override
	 public void fetchHoldedStocks() {
		 if(true)
		 {

			 
		 }
//		 else
//		 {
//			 logger.error("Failed to fetch thresholds");
//	         throw new ThresholdFetchException("Unable to fetch thresholds from Property Manager");
//		 }
	 }
	 
	 @Override
	 public List<HoldedStock> getHoldedStocks()
	 {
		 if(holdedStocks == null)
		 {
			 fetchHoldedStocks();
		 }
		 return holdedStocks;
	 }
}
