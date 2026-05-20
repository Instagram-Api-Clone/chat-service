package com.hemant.instagram.chat_service.service;

import com.hemant.instagram.chat_service.dto.response.ChatResponse;
import com.hemant.instagram.chat_service.entity.Chat;
import com.hemant.instagram.chat_service.exception.BadRequestException;
import com.hemant.instagram.chat_service.exception.ResourceNotFoundException;
import com.hemant.instagram.chat_service.repository.ChatRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRepository chatRepository;

	@Transactional
	public ChatResponse startChat(Long requesterId, Long targetUserId) {
		if (requesterId == null) {
			throw new BadRequestException("Requester id is required");
		}
		if (targetUserId == null) {
			throw new BadRequestException("Target user id is required");
		}
		if (requesterId.equals(targetUserId)) {
			throw new BadRequestException("Cannot start a chat with yourself");
		}

		Long userOneId = Math.min(requesterId, targetUserId);
		Long userTwoId = Math.max(requesterId, targetUserId);

		Optional<Chat> existingChat = chatRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId);
		if (existingChat.isPresent()) {
			return ChatResponse.from(existingChat.get());
		}

		Chat newChat = Chat.builder()
				.userOneId(userOneId)
				.userTwoId(userTwoId)
				.build();

		newChat = chatRepository.save(newChat);
		log.info("Chat started between {} and {}. Chat ID: {}", userOneId, userTwoId, newChat.getId());
		return ChatResponse.from(newChat);
	}

	@Transactional(readOnly = true)
	public Page<ChatResponse> getUserChats(Long userId, int page, int size) {
		if (userId == null) {
			throw new BadRequestException("User id is required");
		}
		if (page < 0) {
			throw new BadRequestException("Page must be greater than or equal to 0");
		}
		if (size <= 0 || size > 100) {
			throw new BadRequestException("Size must be between 1 and 100");
		}

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt"));
		Page<Chat> chats = chatRepository.findChatsByUserId(userId, pageable);
		return chats.map(ChatResponse::from);
	}

	@Transactional
	public void deleteChat(Long requesterId, Long targetUserId) {
		if (requesterId == null) {
			throw new BadRequestException("Requester id is required");
		}
		if (targetUserId == null) {
			throw new BadRequestException("Target user id is required");
		}

		Long userOneId = Math.min(requesterId, targetUserId);
		Long userTwoId = Math.max(requesterId, targetUserId);

		Chat chat = chatRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
				.orElseThrow(() -> new ResourceNotFoundException("Chat not found"));

		chatRepository.delete(chat);
		log.info("Chat deleted between requesterId={} targetUserId={}", requesterId, targetUserId);
	}
}
