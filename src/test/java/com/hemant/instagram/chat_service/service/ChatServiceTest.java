package com.hemant.instagram.chat_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hemant.instagram.chat_service.dto.response.ChatResponse;
import com.hemant.instagram.chat_service.entity.Chat;
import com.hemant.instagram.chat_service.exception.BadRequestException;
import com.hemant.instagram.chat_service.exception.ResourceNotFoundException;
import com.hemant.instagram.chat_service.repository.ChatRepository;
import com.hemant.instagram.chat_service.support.ChatTestFactory;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@Mock
	private ChatRepository chatRepository;

	@InjectMocks
	private ChatService chatService;

	@Test
	void startChat_shouldReturnExistingChatIfPresent() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.of(chat));

		ChatResponse response = chatService.startChat(20L, 10L);

		assertThat(response.getId()).isEqualTo(1L);
		verify(chatRepository, never()).save(any());
	}

	@Test
	void startChat_shouldCreateNewChatIfNoneExists() {
		Chat newChat = ChatTestFactory.chat(1L, 10L, 20L);
		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.empty());
		when(chatRepository.save(any(Chat.class))).thenReturn(newChat);

		ChatResponse response = chatService.startChat(20L, 10L);

		assertThat(response.getId()).isEqualTo(1L);
		verify(chatRepository).save(any(Chat.class));
	}

	@Test
	void startChat_shouldRejectNullRequesterId() {
		assertThatThrownBy(() -> chatService.startChat(null, 10L))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Requester id is required");
	}

	@Test
	void startChat_shouldRejectNullTargetUserId() {
		assertThatThrownBy(() -> chatService.startChat(20L, null))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Target user id is required");
	}

	@Test
	void startChat_shouldRejectSameUser() {
		assertThatThrownBy(() -> chatService.startChat(10L, 10L))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Cannot start a chat with yourself");
	}

	@Test
	void getUserChats_shouldReturnPagedChats() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		Page<Chat> page = new PageImpl<>(List.of(chat));
		when(chatRepository.findChatsByUserId(eq(10L), any(Pageable.class))).thenReturn(page);

		Page<ChatResponse> response = chatService.getUserChats(10L, 0, 10);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().get(0).getId()).isEqualTo(1L);
	}

	@Test
	void getUserChats_shouldRejectNullUserId() {
		assertThatThrownBy(() -> chatService.getUserChats(null, 0, 10))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("User id is required");
	}

	@Test
	void deleteChat_shouldDeleteSuccessfully() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.of(chat));

		chatService.deleteChat(20L, 10L);

		verify(chatRepository).delete(chat);
	}

	@Test
	void deleteChat_shouldThrowWhenChatNotFound() {
		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 99L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> chatService.deleteChat(10L, 99L))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessage("Chat not found");
	}
}
