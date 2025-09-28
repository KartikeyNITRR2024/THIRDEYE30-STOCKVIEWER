package com.thirdeye3.stockviewer.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.thirdeye3.stockviewer.dtos.Stock;

public interface WebscrapperService {

	void clearMap();
	boolean processWebscrapper(List<Stock> stocks, Integer webscrapperId, String webscrapperCode);
	boolean processWebscrapper(Map<Long, Stock> stocks, Integer webscrapperId, String webscrapperCode);
	ConcurrentMap<Long, CopyOnWriteArrayList<Stock>> getDataStoringMap();


}
