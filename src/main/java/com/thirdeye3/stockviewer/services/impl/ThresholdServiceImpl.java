package com.thirdeye3.stockviewer.services.impl;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.ThresholdGroupDto;
import com.thirdeye3.stockviewer.exceptions.ThresholdFetchException;
import com.thirdeye3.stockviewer.externalcontollers.UserManagerClient;
import com.thirdeye3.stockviewer.services.ThresholdService;

@Service
public class ThresholdServiceImpl implements ThresholdService {

    private static final Logger logger = LoggerFactory.getLogger(ThresholdServiceImpl.class);

    @Autowired
    private UserManagerClient userManager;

    private ConcurrentMap<Long, ThresholdGroupDto> thresholdGroups = null;

    @Override
    public void fetchThresholds() {
        logger.info("Fetching thresholds from UserManager");
        Response<Map<Long, ThresholdGroupDto>> response = userManager.getAllThresoldGroups();
        if (response.isSuccess()) {
        	thresholdGroups = new ConcurrentHashMap<>();
            response.getResponse().forEach((thresoldGroupId, thresoldGroupsCopy) -> {
            	thresholdGroups.put(thresoldGroupId, thresoldGroupsCopy);
                logger.debug("Fetched thresholds for thresoldGroupId={} : {}", thresoldGroupId, thresoldGroupsCopy);
            });
            logger.info("Successfully fetched thresholds for {} thresoldGroupId", thresholdGroups);
        } else {
            logger.error("Failed to fetch thresholds from UserManager");
            throw new ThresholdFetchException("Unable to fetch thresholds from Property Manager");
        }
    }

    @Override
    public ConcurrentMap<Long, ThresholdGroupDto> getthresholdGroup() {
        if (thresholdGroups == null) {
            logger.info("Threshold cache is empty. Fetching thresholds...");
            fetchThresholds();
        } else {
            logger.debug("Returning cached thresholds");
        }
        return thresholdGroups;
    }

    @Override
    public void updateOrAddThresoldByUserId(ThresholdGroupDto thresholdGroupDto) {
        if (thresholdGroups == null) {
            logger.info("Threshold cache is empty. Fetching thresholds...");
            fetchThresholds();
        }

        if (thresholdGroupDto.getThresholds() == null || thresholdGroupDto.getThresholds().isEmpty()) {
        	thresholdGroups.remove(thresholdGroupDto.getId());
            logger.info("Removed all thresholds for groupid={}", thresholdGroupDto.getId());
        } else {
        	thresholdGroups.put(thresholdGroupDto.getId(), thresholdGroupDto);
            logger.info("Updated/Added thresholds for groupid={} : {}", thresholdGroupDto.getId(), thresholdGroupDto);
        }
    }
}
