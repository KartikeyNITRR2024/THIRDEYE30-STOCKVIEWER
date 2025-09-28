package com.thirdeye3.stockviewer.dtos;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ThresholdGroupDto {
	private Long id;
    private List<ThresholdDto> thresholds;
    private Boolean allStocks;
    private String stockList;
}
