package com.thirdeye3.stockviewer.services;

import java.util.Map;

import com.thirdeye3.stockviewer.dtos.Machine;

public interface MachineService {

	void fetchMachines();

	Integer validateMachine(Integer machineId, String machineUniqueCode);

	Map<String, Machine> getMachines();

	void informMachineToUpdate();

	boolean isUpdateMachineRequiredNeeded(Integer machineId, String machineUniqueCode);

}
