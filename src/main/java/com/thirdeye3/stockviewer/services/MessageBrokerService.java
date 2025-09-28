package com.thirdeye3.stockviewer.services;

public interface MessageBrokerService {

	void sendMessages(String topicName, Object messagess);

}
