package com.thirdeye3.stockviewer.externalcontollers;

import java.util.Map;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.thirdeye3.stockviewer.configs.FeignConfig;
import com.thirdeye3.stockviewer.dtos.MachineInfo;
import com.thirdeye3.stockviewer.dtos.Response;

@FeignClient(
		name = "THIRDEYE30-PROPERTYMANAGER",
		configuration = FeignConfig.class
)
public interface PropertyManagerClient {

    @GetMapping("/pm/machines/webscrapper")
    Response<MachineInfo> getMachines();
    
    @GetMapping("/pm/properties")
    Response<Map<String, Object>> getProperties();
}
