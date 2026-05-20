package com.hemant.instagram.chat_service.support;

import com.hemant.instagram.chat_service.entity.Chat;
import com.hemant.instagram.chat_service.entity.ChatMessage;
import java.time.LocalDateTime;

public final class ChatTestFactory {

	private ChatTestFactory() {
	}

	public static Chat chat(Long id, Long userOneId, Long userTwoId) {
		return Chat.builder()
				.id(id)
				.userOneId(userOneId)
				.userTwoId(userTwoId)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}

	public static ChatMessage chatMessage(Long id, Chat chat, Long senderId, Long receiverId, String content) {
		return ChatMessage.builder()
				.id(id)
				.chat(chat)
				.senderId(senderId)
				.receiverId(receiverId)
				.content(content)
				.createdAt(LocalDateTime.now())
				.updatedAt(LocalDateTime.now())
				.build();
	}
}
