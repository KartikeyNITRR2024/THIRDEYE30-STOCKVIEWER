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
public class HoldedStock {
   private Long id;
   private Long userId;
   private Long uniqueId;
   private Long currentStatus;
   private Integer type;
   private Double buyingPrice;
   private List<HoldedStockStatus> status;
}
