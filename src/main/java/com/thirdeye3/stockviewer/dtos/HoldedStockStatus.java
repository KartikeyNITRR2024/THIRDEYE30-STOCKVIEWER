package com.thirdeye3.stockviewer.dtos;

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
public class HoldedStockStatus {
    private Long id;
    private Long holdedStockId;
    private Double status;
    private Long statusId;
}
