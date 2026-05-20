package com.hemant.instagram.chat_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.hemant.instagram.chat_service.configuration.KafkaTopicConfiguration;
import com.hemant.instagram.chat_service.dto.request.SendMessageRequest;
import com.hemant.instagram.chat_service.dto.response.ChatMessageResponse;
import com.hemant.instagram.chat_service.entity.Chat;
import com.hemant.instagram.chat_service.entity.ChatMessage;
import com.hemant.instagram.chat_service.event.NewChatMessageEvent;
import com.hemant.instagram.chat_service.exception.BadRequestException;
import com.hemant.instagram.chat_service.exception.ResourceNotFoundException;
import com.hemant.instagram.chat_service.repository.ChatMessageRepository;
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
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private ChatRepository chatRepository;

	@Mock
	private KafkaTemplate<Long, NewChatMessageEvent> kafkaTemplate;

	@InjectMocks
	private ChatMessageService chatMessageService;

	@Test
	void sendMessage_shouldSaveMessageAndUpdateChat() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		ChatMessage message = ChatTestFactory.chatMessage(100L, chat, 10L, 20L, "Hello");
		SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.of(chat));
		when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

		ChatMessageResponse response = chatMessageService.sendMessage(10L, 20L, request);

		assertThat(response.getId()).isEqualTo(100L);
		assertThat(response.getContent()).isEqualTo("Hello");
		verify(chatMessageRepository).save(any(ChatMessage.class));
		verify(chatRepository).save(chat);
		verify(kafkaTemplate).send(eq(KafkaTopicConfiguration.NEW_CHAT_MESSAGE_TOPIC), eq(20L), any(NewChatMessageEvent.class));
	}

	@Test
	void sendMessage_shouldCreateChatIfNotFound() {
		ChatMessage message = ChatTestFactory.chatMessage(100L, ChatTestFactory.chat(1L, 10L, 20L), 10L, 20L, "Hello");
		SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.empty());
		when(chatRepository.save(any(Chat.class))).thenReturn(ChatTestFactory.chat(1L, 10L, 20L));
		when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(message);

		ChatMessageResponse response = chatMessageService.sendMessage(10L, 20L, request);

		assertThat(response.getId()).isEqualTo(100L);
		verify(chatRepository).findByUserOneIdAndUserTwoId(10L, 20L);
		// save is called twice: once for new chat, once for updating updatedAt
	}

	@Test
	void sendMessage_shouldRejectIfSelf() {
		SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();

		assertThatThrownBy(() -> chatMessageService.sendMessage(10L, 10L, request))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Cannot send message to yourself");

		verify(chatMessageRepository, never()).save(any());
	}

	@Test
	void getChatMessages_shouldReturnPagedMessages() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		ChatMessage message = ChatTestFactory.chatMessage(100L, chat, 10L, 20L, "Hello");
		Page<ChatMessage> page = new PageImpl<>(List.of(message));

		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.of(chat));
		when(chatMessageRepository.findByChatIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class))).thenReturn(page);

		Page<ChatMessageResponse> response = chatMessageService.getChatMessages(10L, 20L, 0, 10);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().get(0).getId()).isEqualTo(100L);
	}

	@Test
	void searchMessages_shouldReturnMatchingMessages() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		ChatMessage message = ChatTestFactory.chatMessage(100L, chat, 10L, 20L, "Hello world");
		Page<ChatMessage> page = new PageImpl<>(List.of(message));

		when(chatRepository.findByUserOneIdAndUserTwoId(10L, 20L)).thenReturn(Optional.of(chat));
		when(chatMessageRepository.findByChatIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(eq(1L), eq("world"), any(Pageable.class))).thenReturn(page);

		Page<ChatMessageResponse> response = chatMessageService.searchMessages(10L, 20L, "world", 0, 10);

		assertThat(response.getTotalElements()).isEqualTo(1);
		assertThat(response.getContent().get(0).getContent()).isEqualTo("Hello world");
	}

	@Test
	void deleteMessage_shouldDeleteIfSender() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		ChatMessage message = ChatTestFactory.chatMessage(100L, chat, 10L, 20L, "Hello");

		when(chatMessageRepository.findById(100L)).thenReturn(Optional.of(message));

		chatMessageService.deleteMessage(10L, 100L);

		verify(chatMessageRepository).delete(message);
	}

	@Test
	void deleteMessage_shouldRejectIfNotSender() {
		Chat chat = ChatTestFactory.chat(1L, 10L, 20L);
		ChatMessage message = ChatTestFactory.chatMessage(100L, chat, 10L, 20L, "Hello");

		when(chatMessageRepository.findById(100L)).thenReturn(Optional.of(message));

		assertThatThrownBy(() -> chatMessageService.deleteMessage(20L, 100L))
				.isInstanceOf(BadRequestException.class)
				.hasMessage("Cannot delete another user's message");

		verify(chatMessageRepository, never()).delete(any());
	}
}
