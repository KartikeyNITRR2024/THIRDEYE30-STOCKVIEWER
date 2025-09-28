package com.thirdeye3.stockviewer.dtos;

import java.sql.Timestamp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class PriceChange {
   private Long groupId;
   private Long uniqueId;
   private Double priceChange;
   private Integer type;
   private Long timeGap;
   private Double oldPrice;
   private Double newPrice;
   private Timestamp currTime;
}
