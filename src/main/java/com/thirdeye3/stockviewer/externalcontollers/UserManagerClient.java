package com.thirdeye3.stockviewer.externalcontollers;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.ThresholdGroupDto;
import com.thirdeye3.stockviewer.configs.FeignConfig;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(
		name = "THIRDEYE30-USERMANAGER",
        configuration = FeignConfig.class
)
public interface UserManagerClient {
	
    @GetMapping("/um/admin/threshold-groups/active/1")
    Response<Map<Long, ThresholdGroupDto>> getAllThresoldGroups();
}
