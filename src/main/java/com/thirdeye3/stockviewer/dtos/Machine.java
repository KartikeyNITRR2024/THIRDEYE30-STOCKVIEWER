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
public class Machine {
	private Long id;
    private Integer typeOfMachine;
    private String machineUniqueCode;
    private Integer machineNo;
}

