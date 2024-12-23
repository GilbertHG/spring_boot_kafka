package com.sg.fairtech.kafkaapp.kafka.producer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductCheckDataProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafkaapp.kafka.topic.check_data}")
    private String TOPIC;

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
    }

}
