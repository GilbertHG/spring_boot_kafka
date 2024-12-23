package com.sg.fairtech.kafkaappconsumer1.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductRequestCheckDataProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafkaapp.kafka.topic.request_check_data}")
    private String TOPIC;

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
    }

}
