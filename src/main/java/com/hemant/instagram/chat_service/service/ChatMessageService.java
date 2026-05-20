package com.hemant.instagram.chat_service.service;

import com.hemant.instagram.chat_service.dto.request.SendMessageRequest;
import com.hemant.instagram.chat_service.dto.response.ChatMessageResponse;
import com.hemant.instagram.chat_service.entity.Chat;
import com.hemant.instagram.chat_service.entity.ChatMessage;
import com.hemant.instagram.chat_service.configuration.KafkaTopicConfiguration;
import com.hemant.instagram.chat_service.event.NewChatMessageEvent;
import com.hemant.instagram.chat_service.exception.BadRequestException;
import com.hemant.instagram.chat_service.exception.ResourceNotFoundException;
import com.hemant.instagram.chat_service.repository.ChatMessageRepository;
import com.hemant.instagram.chat_service.repository.ChatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRepository chatRepository;
	private final KafkaTemplate<Long, NewChatMessageEvent> kafkaTemplate;

	@Transactional
	public ChatMessageResponse sendMessage(Long senderId, Long receiverId, SendMessageRequest request) {
		if (senderId == null) {
			throw new BadRequestException("Sender id is required");
		}
		if (receiverId == null) {
			throw new BadRequestException("Receiver id is required");
		}
		if (senderId.equals(receiverId)) {
			throw new BadRequestException("Cannot send message to yourself");
		}
		if (request == null || request.getContent() == null || request.getContent().trim().isEmpty()) {
			throw new BadRequestException("Message content cannot be blank");
		}

		Long userOneId = Math.min(senderId, receiverId);
		Long userTwoId = Math.max(senderId, receiverId);

		Chat chat = chatRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
				.orElseGet(() -> {
					Chat newChat = Chat.builder()
							.userOneId(userOneId)
							.userTwoId(userTwoId)
							.build();
					return chatRepository.save(newChat);
				});

		ChatMessage message = ChatMessage.builder()
				.chat(chat)
				.senderId(senderId)
				.receiverId(receiverId)
				.content(request.getContent().trim())
				.build();

		message = chatMessageRepository.save(message);
		
		// Update chat's updatedAt timestamp
		chat.setUpdatedAt(message.getCreatedAt());
		chatRepository.save(chat);

		NewChatMessageEvent event = NewChatMessageEvent.builder()
				.messageId(message.getId())
				.chatId(chat.getId())
				.senderId(senderId)
				.receiverId(receiverId)
				.content(message.getContent())
				.createdAt(message.getCreatedAt())
				.build();
		
		kafkaTemplate.send(KafkaTopicConfiguration.NEW_CHAT_MESSAGE_TOPIC, receiverId, event);

		log.info("Message sent messageId={} chatId={} senderId={}", message.getId(), chat.getId(), senderId);
		return ChatMessageResponse.from(message);
	}

	@Transactional(readOnly = true)
	public Page<ChatMessageResponse> getChatMessages(Long requesterId, Long receiverId, int page, int size) {
		if (requesterId == null) {
			throw new BadRequestException("Requester id is required");
		}
		if (receiverId == null) {
			throw new BadRequestException("Receiver id is required");
		}
		if (requesterId.equals(receiverId)) {
			throw new BadRequestException("Cannot view messages with yourself");
		}
		if (page < 0) {
			throw new BadRequestException("Page must be greater than or equal to 0");
		}
		if (size <= 0 || size > 100) {
			throw new BadRequestException("Size must be between 1 and 100");
		}

		Long userOneId = Math.min(requesterId, receiverId);
		Long userTwoId = Math.max(requesterId, receiverId);

		Chat chat = chatRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId).orElse(null);
		if (chat == null) {
			return Page.empty();
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<ChatMessage> messages = chatMessageRepository.findByChatIdOrderByCreatedAtDesc(chat.getId(), pageable);
		return messages.map(ChatMessageResponse::from);
	}

	@Transactional(readOnly = true)
	public Page<ChatMessageResponse> searchMessages(Long requesterId, Long receiverId, String keyword, int page, int size) {
		if (requesterId == null) {
			throw new BadRequestException("Requester id is required");
		}
		if (receiverId == null) {
			throw new BadRequestException("Receiver id is required");
		}
		if (requesterId.equals(receiverId)) {
			throw new BadRequestException("Cannot search messages with yourself");
		}
		if (keyword == null || keyword.trim().isEmpty()) {
			throw new BadRequestException("Search keyword is required");
		}
		if (page < 0) {
			throw new BadRequestException("Page must be greater than or equal to 0");
		}
		if (size <= 0 || size > 100) {
			throw new BadRequestException("Size must be between 1 and 100");
		}

		Long userOneId = Math.min(requesterId, receiverId);
		Long userTwoId = Math.max(requesterId, receiverId);

		Chat chat = chatRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId).orElse(null);
		if (chat == null) {
			return Page.empty();
		}

		Pageable pageable = PageRequest.of(page, size);
		Page<ChatMessage> messages = chatMessageRepository.findByChatIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(
				chat.getId(), keyword.trim(), pageable);
		return messages.map(ChatMessageResponse::from);
	}

	@Transactional
	public void deleteMessage(Long requesterId, Long messageId) {
		if (requesterId == null) {
			throw new BadRequestException("Requester id is required");
		}

		ChatMessage message = chatMessageRepository.findById(messageId)
				.orElseThrow(() -> new ResourceNotFoundException("Message not found"));

		if (!message.getSenderId().equals(requesterId)) {
			throw new BadRequestException("Cannot delete another user's message");
		}

		chatMessageRepository.delete(message);
		log.info("Message deleted messageId={} requesterId={}", messageId, requesterId);
	}
}
