package com.thirdeye3.stockviewer.externalcontollers;

import com.thirdeye3.stockviewer.configs.FeignConfig;
import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.Stock;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
		name = "THIRDEYE30-STOCKMANAGER",
		configuration = FeignConfig.class
)
public interface StockManagerClient {

    @PostMapping("/sm/stocks/updatePrice")
    Response<String> updatePriceOfStock(@RequestBody List<Stock> stockList);
    
    @GetMapping("/sm/stocks/{page}/{size}")
    Response<List<Stock>> getStocks(
            @PathVariable("page") long page,
            @PathVariable("size") long size
    );

    @GetMapping("/sm/stocks/size")
    Response<Long> getStockSize();
}
