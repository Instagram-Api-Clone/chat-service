package com.hemant.instagram.chat_service.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewChatMessageEvent {
	private Long messageId;
	private Long chatId;
	private Long senderId;
	private Long receiverId;
	private String content;
	private LocalDateTime createdAt;
}
