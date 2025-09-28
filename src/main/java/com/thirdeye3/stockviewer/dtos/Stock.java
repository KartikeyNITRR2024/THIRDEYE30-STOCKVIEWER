package com.thirdeye3.stockviewer.dtos;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Stock {
	private Long uniqueId;
    private Double lastNightClosingPrice;
    private Double todaysOpeningPrice;
	private Timestamp currentTime;
	private Double price;
	private List<PriceChange> priceChangeList;
	
    public Stock(Timestamp currentTime, Double price) {
		this.currentTime = currentTime;
		this.price = price;
	}
}
