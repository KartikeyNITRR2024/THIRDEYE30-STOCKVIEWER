package com.thirdeye3.stockviewer.controllers;

import java.util.List;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.dtos.WebscrapperRequest;
import com.thirdeye3.stockviewer.services.WebscrapperService;


@RestController
@RequestMapping("/sv/webscrapper")
public class WebscrapperController {
	
    @Value("${thirdeye.webViewerType}")
    private Integer webViewerType;

	@Autowired
	WebscrapperService webscrapperService;

    @PostMapping("/{id}/{code}")
    public Response<Boolean> getForWebscrapper(
            @PathVariable("id") Integer webscrapperId,
            @PathVariable("code") String webscrapperCode,
            @RequestBody WebscrapperRequest webscrapperRequest) {
    	boolean updateMachine = false;
    	if(webViewerType == 1)
    	{
    		updateMachine = webscrapperService.processWebscrapper(webscrapperRequest.getStockList(), webscrapperId, webscrapperCode);
    	}
    	else if(webViewerType == 2)
    	{
    		updateMachine = webscrapperService.processWebscrapper(webscrapperRequest.getStockMap(), webscrapperId, webscrapperCode);
    	}
        return new Response<Boolean>(true, 0, null, updateMachine);
    }
}
