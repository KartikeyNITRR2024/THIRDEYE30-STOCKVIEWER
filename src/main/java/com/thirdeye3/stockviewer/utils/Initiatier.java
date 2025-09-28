package com.thirdeye3.stockviewer.utils;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.thirdeye3.stockviewer.services.MachineService;
import com.thirdeye3.stockviewer.services.PropertyService;
import com.thirdeye3.stockviewer.services.StockService;
import com.thirdeye3.stockviewer.services.ThresholdService;
import com.thirdeye3.stockviewer.services.WebscrapperService;
import com.thirdeye3.stockviewer.utils.Initiatier;

import jakarta.annotation.PostConstruct;

@Component
public class Initiatier {
	
    private static final Logger logger = LoggerFactory.getLogger(Initiatier.class);
	
	@Autowired
	PropertyService propertyService;
	
	@Autowired
	MachineService machineService;
	
	@Autowired
	ThresholdService thresholdService;
	
	@Autowired
	WebscrapperService webscrapperService;
	
	@Autowired
	StockService stockService;
	
    @Value("${thirdeye.priority}")
    private Integer priority;
    
	@PostConstruct
    public void init() throws Exception{
        logger.info("Initializing Initiatier...");
    	TimeUnit.SECONDS.sleep(priority * 3);
        propertyService.fetchProperties();
        machineService.fetchMachines();
        machineService.informMachineToUpdate();
        thresholdService.fetchThresholds();
        stockService.fetchStocks();
        logger.info("Initiatier initialized.");
    }
	
	public void refreshMemory()
	{
		logger.info("Going to refersh memory...");
		webscrapperService.clearMap();
        stockService.fetchStocks();
		logger.info("Memory refreshed.");
	}

}