package com.thirdeye3.stockviewer.services.impl;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.TimeGap;
import com.thirdeye3.stockviewer.exceptions.PropertyFetchException;
import com.thirdeye3.stockviewer.externalcontollers.PropertyManagerClient;
import com.thirdeye3.stockviewer.services.PropertyService;

@Service
public class PropertyServiceImpl implements PropertyService {

    private static final Logger logger = LoggerFactory.getLogger(PropertyServiceImpl.class);

    @Autowired
    private PropertyManagerClient propertyManager;

    private Map<String, Object> properties = null;
    private Integer noOFWebscrapperMarket = null;
    private Integer noOFWebscrapperUser = null;
    private Long sizeToLoadStock = null;
    private LocalTime morningPriceUpdaterStartTime = null;
    private LocalTime morningPriceUpdaterEndTime = null;
    private LocalTime eveningPriceUpdaterStartTime = null;
    private LocalTime eveningPriceUpdaterEndTime = null;
    private Long bufferTimeGapInSeconds = null;
    private List<TimeGap> gaps = null;


    @Override
    public void fetchProperties() {
        Response<Map<String, Object>> response = propertyManager.getProperties();
        if (response.isSuccess()) {
            properties = response.getResponse();
            sizeToLoadStock = ((Number) properties.getOrDefault("SIZE_TO_LOAD_STOCK", 50L)).longValue();
            noOFWebscrapperMarket = (Integer) properties.getOrDefault("NO_OF_WEBSCRAPPER_MARKET", 0);
            noOFWebscrapperUser = (Integer) properties.getOrDefault("NO_OF_WEBSCRAPPER_USER", 0);
            morningPriceUpdaterStartTime = LocalTime.of(
                    (int) properties.getOrDefault("MP_START_HOUR",9),
                    (int) properties.getOrDefault("MP_START_MINUTE",15),
                    (int) properties.getOrDefault("MP_START_SECOND",00)
            );

            morningPriceUpdaterEndTime = LocalTime.of(
                    (int) properties.getOrDefault("MP_END_HOUR",9),
                    (int) properties.getOrDefault("MP_END_MINUTE",16),
                    (int) properties.getOrDefault("MP_END_SECOND",30)
            );
            
            eveningPriceUpdaterStartTime = LocalTime.of(
                    (int) properties.getOrDefault("EP_START_HOUR",3),
                    (int) properties.getOrDefault("EP_START_MINUTE",30),
                    (int) properties.getOrDefault("EP_START_SECOND",00)
            );

            eveningPriceUpdaterEndTime = LocalTime.of(
                    (int) properties.getOrDefault("EP_END_HOUR",3),
                    (int) properties.getOrDefault("EP_END_MINUTE",30),
                    (int) properties.getOrDefault("EP_END_SECOND",30)
            );
            bufferTimeGapInSeconds = (long) properties.getOrDefault("BUFFER_TIME_GAP_IN_SECONDS",10L);
            String gapsString = properties.getOrDefault("FILTER_FOR_TIME_THRESOLD", "0,60,1,61,120,5,121,240,10,241,400,20").toString();
            gaps = IntStream.range(0, gapsString.split(",").length / 3)
            	    .mapToObj(i -> {
            	        String[] p = gapsString.split(",");
            	        return new TimeGap(
            	            Long.parseLong(p[i * 3].trim()),
            	            Long.parseLong(p[i * 3 + 1].trim()),
            	            Long.parseLong(p[i * 3 + 2].trim())
            	        );
            	    })
            	    .toList();
            logger.info("Request {}, {}, {}, {}", properties, noOFWebscrapperMarket, noOFWebscrapperUser, gaps);
        } else {
            properties = new HashMap<>();
            sizeToLoadStock=50L;
            noOFWebscrapperMarket = 0;
            noOFWebscrapperUser = 0;
            morningPriceUpdaterStartTime = LocalTime.of(9,15,00);
            morningPriceUpdaterEndTime = LocalTime.of(9,16,30);
            eveningPriceUpdaterStartTime = LocalTime.of(3,30,00);
            eveningPriceUpdaterEndTime = LocalTime.of(3,30,30);
            bufferTimeGapInSeconds = 10L;
            gaps = List.of(new TimeGap(0, 60, 1), new TimeGap(61, 120, 5), new TimeGap(121, 240, 10), new TimeGap(241, 400, 20));
            logger.error("Failed to fetch properties");
            throw new PropertyFetchException("Unable to fetch properties from Property Manager");
        }
    }
    
    @Override
	public Long getSizeToLoadStock() {
    	if(sizeToLoadStock == null)
    	{
    		fetchProperties();
    	}
		return sizeToLoadStock;
	}

    @Override
    public Integer getNoOFWebscrapperMarket() {
        if (noOFWebscrapperMarket == null) {
        	fetchProperties();
        }
        return noOFWebscrapperMarket;
    }

    @Override
    public Integer getNoOFWebscrapperUser() {
        if (noOFWebscrapperUser == null) {
        	fetchProperties();
        }
        return noOFWebscrapperUser;
    }

    @Override
	public LocalTime getMorningPriceUpdaterStartTime() {
    	if (morningPriceUpdaterStartTime == null) {
        	fetchProperties();
        }
		return morningPriceUpdaterStartTime;
	}

    @Override
	public LocalTime getMorningPriceUpdaterEndTime() {
    	if (morningPriceUpdaterEndTime == null) {
        	fetchProperties();
        }
		return morningPriceUpdaterEndTime;
	}

    @Override
	public LocalTime getEveningPriceUpdaterStartTime() {
    	if (eveningPriceUpdaterStartTime == null) {
        	fetchProperties();
        }
		return eveningPriceUpdaterStartTime;
	}

    @Override
	public LocalTime getEveningPriceUpdaterEndTime() {
    	if (eveningPriceUpdaterEndTime == null) {
        	fetchProperties();
        }
		return eveningPriceUpdaterEndTime;
	}

    @Override
	public Long getBufferTimeGapInSeconds() {
    	if (bufferTimeGapInSeconds == null) {
        	fetchProperties();
        }
		return bufferTimeGapInSeconds;
	}

    @Override
	public List<TimeGap> getGaps() {
    	if (gaps == null) {
        	fetchProperties();
        }
		return gaps;
	}
    
    
    
    
}

