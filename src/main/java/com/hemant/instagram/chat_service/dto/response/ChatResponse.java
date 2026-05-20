package com.hemant.instagram.chat_service.dto.response;

import com.hemant.instagram.chat_service.entity.Chat;
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
public class ChatResponse {
	private Long id;
	private Long userOneId;
	private Long userTwoId;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	public static ChatResponse from(Chat chat) {
		return ChatResponse.builder()
				.id(chat.getId())
				.userOneId(chat.getUserOneId())
				.userTwoId(chat.getUserTwoId())
				.createdAt(chat.getCreatedAt())
				.updatedAt(chat.getUpdatedAt())
				.build();
	}
}
