package com.thirdeye3.stockviewer.utils;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.dtos.TimeGap;
import com.thirdeye3.stockviewer.services.PropertyService;
import com.thirdeye3.stockviewer.services.WebscrapperService;

@Component
public class DataStoringMapFilter {
	
	private static final Logger logger = LoggerFactory.getLogger(DataStoringMapFilter.class);

	
	@Autowired
	PropertyService propertyService;
	
	@Autowired
	WebscrapperService webscrapperService;
	
	@Autowired
	TimeManager timeManager;

	@Async("WebscrapperAsynchThread")
    public void filter() {
		int count = 0;
        Timestamp currentTime = timeManager.getCurrentTime();
        for (Long stockId : webscrapperService.getDataStoringMap().keySet()) {
            CopyOnWriteArrayList<Stock> list = webscrapperService.getDataStoringMap().get(stockId);
            
            Iterator<Stock> iterator = list.iterator();
            while (iterator.hasNext()) {
                Stock stock = iterator.next();
                long timeGapInMinutes = timeManager.getMinutesGapBetweenTime(currentTime, stock.getCurrentTime());
                boolean removeStock = false;

                List<TimeGap> gaps = propertyService.getGaps();
                for (int i = 0; i < gaps.size(); i++) {
                    TimeGap timeGap = gaps.get(i);
                    if (timeGap.getStartTimeInMinutes() <= timeGapInMinutes 
                        && timeGapInMinutes <= timeGap.getEndTimeInMinutes()) {
                        if (timeManager.getMinute(stock.getCurrentTime()) % timeGap.getGapInMinutes() != 0) {
                            removeStock = true;
                            break;
                        }
                    }
                }

                if (removeStock) {
                	count++;
                    list.remove(stock);
                }
            }
        }
        
        logger.info("Removed "+count+" stocks data");
    }
}
