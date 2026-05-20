package com.hemant.instagram.chat_service.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {

	public static final String NEW_CHAT_MESSAGE_TOPIC = "new-chat-message-topic";

	@Bean
	public NewTopic newChatMessageTopic() {
		return TopicBuilder.name(NEW_CHAT_MESSAGE_TOPIC)
				.partitions(3)
				.replicas(1)
				.build();
	}
}
