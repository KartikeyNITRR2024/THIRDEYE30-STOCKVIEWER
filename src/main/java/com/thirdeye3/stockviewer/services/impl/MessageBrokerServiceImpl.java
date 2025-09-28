package com.thirdeye3.stockviewer.services.impl;

import org.springframework.stereotype.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.thirdeye3.stockviewer.configs.MessageBrokerConfig;
import com.thirdeye3.stockviewer.dtos.Response;
import com.thirdeye3.stockviewer.dtos.Stock;
import com.thirdeye3.stockviewer.exceptions.MessageBrokerException;
import com.thirdeye3.stockviewer.externalcontollers.MessageBrokerClient;
import com.thirdeye3.stockviewer.services.MessageBrokerService;

@Service
public class MessageBrokerServiceImpl implements MessageBrokerService {

    private static final Logger logger = LoggerFactory.getLogger(MessageBrokerServiceImpl.class);
    
    @Autowired
    private MessageBrokerConfig messageBrokerConfig;
    
    @Autowired 
    private MessageBrokerClient messageBroker;
    
    @Override
    public void sendMessages(String topicName, Object messages)
    {
    	if(!messageBrokerConfig.getTopics().containsKey(topicName))
    	{
    		throw new MessageBrokerException("Does not have any topic with topic name "+topicName);
    	}
    	try {
    		Response<String> response = messageBroker.setMessages(topicName, messageBrokerConfig.getTopics().get(topicName).getTopicKey(), messages);
    		if (response.isSuccess()) {
                logger.info("Successfully send messages to message broker with topic name "+topicName);
            }
    		else
    		{
    		    throw new MessageBrokerException("Failed to send messages to message broker with topic name "+topicName+" "+response.getErrorMessage());
    		}
    	} catch (Exception e) {
    		throw new MessageBrokerException("Failed to send messages to message broker with topic name "+topicName+" "+e.getMessage());
        }
    }
}
