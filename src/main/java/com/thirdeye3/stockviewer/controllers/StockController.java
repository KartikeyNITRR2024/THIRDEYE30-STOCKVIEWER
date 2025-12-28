package com.thirdeye3.stockviewer.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.services.WebscrapperService;


@RestController
@RequestMapping("/sv/processing")
public class StockController {
	
	@Autowired
	WebscrapperService webscrapperService;

    @GetMapping("/info")
    public Response<Map<Long, Integer>> getProcessingStockInfo() {
        return new Response<Map<Long, Integer>>(true, 0, null, webscrapperService.getProcessingInfo());
    }
    
    @GetMapping("/details")
    public Response<Map<Long, List<Stock>>> getProcessingStockDetail() {
        return new Response<Map<Long, List<Stock>>>(true, 0, null, webscrapperService.getProcessingDetails());
    }
    
    @GetMapping("/details/{id}")
    public Response<List<Stock>> getProcessingStockDetail(@PathVariable("id") Long stockId) {
        return new Response<List<Stock>>(true, 0, null, webscrapperService.getProcessingDetailsById(stockId));
    }
}

