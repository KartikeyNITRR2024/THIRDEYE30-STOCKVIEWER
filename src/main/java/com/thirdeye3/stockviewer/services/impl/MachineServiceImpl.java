package com.thirdeye3.stockviewer.services.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.thirdeye3.stockviewer.dtos.Machine;
import com.thirdeye3.stockviewer.dtos.MachineInfo;
import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.exceptions.InvalidMachineException;
import com.thirdeye3.stockviewer.externalcontollers.PropertyManagerClient;
import com.thirdeye3.stockviewer.services.impl.MachineServiceImpl;
import com.thirdeye3.stockviewer.services.MachineService;

@Service
public class MachineServiceImpl implements MachineService {
	
    @Autowired
    private PropertyManagerClient propertyManager;

    private Map<String, Machine> machines = null;
    private static final Logger logger = LoggerFactory.getLogger(MachineServiceImpl.class);
    private Set<String> machinesToInform = new HashSet<>();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void fetchMachines() {
        Response<MachineInfo> response = propertyManager.getMachines();
        if (response.isSuccess()) {
            MachineInfo machineInfo = response.getResponse();
            machines = machineInfo.getMachineDtos();
            logger.info("Request {}, {}, {}", machines);
        } else {
            machines = new HashMap<>();
            logger.error("Request error");
        }
    }

    @Override
    public Integer validateMachine(Integer machineId, String machineUniqueCode) {
        if (machines == null) {
            fetchMachines();
        }
        String key = machineId + machineUniqueCode;
        if (!machines.containsKey(key)) {
            throw new InvalidMachineException("Invalid machine with ID: " + machineId + " and code: " + machineUniqueCode);
        }
        return machines.get(key).getMachineNo();
    }

    @Override
    public Map<String, Machine> getMachines() {
        return machines;
    }
    
    @Override
    public void informMachineToUpdate() {
    	if (machines == null) {
            fetchMachines();
        }
    	lock.writeLock().lock();
		try {
			logger.info("Request to update machine.");
			machinesToInform = new HashSet<>(machines.keySet());
		} finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean isUpdateMachineRequiredNeeded(Integer machineId, String machineUniqueCode)
    {
    	boolean check = false;
    	if(machinesToInform.contains(machineId + machineUniqueCode))
    	{
    		lock.writeLock().lock();
    		try {
    			logger.info("Updating machine {}", machineId + machineUniqueCode);
    			check = true;
    			machinesToInform.remove(machineId + machineUniqueCode);
    		} finally {
                lock.writeLock().unlock();
            }
    	}
    	return check;
    }
}
