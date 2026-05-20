package com.hemant.instagram.chat_service.dto.response;

import com.hemant.instagram.chat_service.entity.ChatMessage;
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
public class ChatMessageResponse {
	private Long id;
	private Long chatId;
	private Long senderId;
	private Long receiverId;
	private String content;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ChatMessageResponse from(ChatMessage message) {
		return ChatMessageResponse.builder()
				.id(message.getId())
				.chatId(message.getChat().getId())
				.senderId(message.getSenderId())
				.receiverId(message.getReceiverId())
				.content(message.getContent())
				.createdAt(message.getCreatedAt())
				.updatedAt(message.getUpdatedAt())
				.build();
	}
}
