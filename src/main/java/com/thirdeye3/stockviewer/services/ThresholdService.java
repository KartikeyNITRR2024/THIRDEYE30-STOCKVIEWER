package com.thirdeye3.stockviewer.services;

import java.util.List;
import java.util.concurrent.ConcurrentMap;
import com.thirdeye3.stockviewer.dtos.ThresholdGroupDto;

public interface ThresholdService {

	void fetchThresholds();

	void updateOrAddThresoldByUserId(ThresholdGroupDto thresholdGroupDto);

	ConcurrentMap<Long, ThresholdGroupDto> getthresholdGroup();

}
