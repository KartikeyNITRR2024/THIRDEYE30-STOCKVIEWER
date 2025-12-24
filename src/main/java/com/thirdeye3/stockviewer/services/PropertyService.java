package com.thirdeye3.stockviewer.services;

import java.time.LocalTime;
import java.util.List;

import com.thirdeye3.stockviewer.dtos.TimeGap;

public interface PropertyService {

	void fetchProperties();

	Integer getNoOFWebscrapperMarket();

	Integer getNoOFWebscrapperUser();

	LocalTime getMorningPriceUpdaterEndTime();

	LocalTime getMorningPriceUpdaterStartTime();

	LocalTime getEveningPriceUpdaterStartTime();

	LocalTime getEveningPriceUpdaterEndTime();

	Long getBufferTimeGapInSeconds();

	Long getSizeToLoadStock();

	List<TimeGap> getGaps();

	LocalTime getMarketEnd();

	LocalTime getMarketStart();

}
