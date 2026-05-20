package com.hemant.instagram.chat_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hemant.instagram.chat_service.dto.request.SendMessageRequest;
import com.hemant.instagram.chat_service.dto.response.ChatMessageResponse;
import com.hemant.instagram.chat_service.service.ChatMessageService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatMessageController.class)
class ChatMessageControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChatMessageService chatMessageService;

	@Test
	void sendMessage_shouldReturnCreated() throws Exception {
		SendMessageRequest request = SendMessageRequest.builder().content("Hello").build();
		ChatMessageResponse response = ChatMessageResponse.builder().id(100L).chatId(1L).content("Hello").build();

		when(chatMessageService.sendMessage(eq(10L), eq(1L), any(SendMessageRequest.class))).thenReturn(response);

		mockMvc.perform(post("/chat-messages/users/1")
						.header("X-User-Id", "10")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(100))
				.andExpect(jsonPath("$.content").value("Hello"));

		verify(chatMessageService).sendMessage(eq(10L), eq(1L), any(SendMessageRequest.class));
	}

	@Test
	void getChatMessages_shouldReturnPagedMessages() throws Exception {
		ChatMessageResponse response = ChatMessageResponse.builder().id(100L).chatId(1L).content("Hello").build();
		Page<ChatMessageResponse> page = new PageImpl<>(List.of(response));

		when(chatMessageService.getChatMessages(eq(10L), eq(1L), eq(0), eq(20))).thenReturn(page);

		mockMvc.perform(get("/chat-messages/users/1").header("X-User-Id", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].content").value("Hello"));
	}

	@Test
	void searchMessages_shouldReturnMatchingMessages() throws Exception {
		ChatMessageResponse response = ChatMessageResponse.builder().id(100L).chatId(1L).content("Hello").build();
		Page<ChatMessageResponse> page = new PageImpl<>(List.of(response));

		when(chatMessageService.searchMessages(eq(10L), eq(1L), eq("Hel"), eq(0), eq(20))).thenReturn(page);

		mockMvc.perform(get("/chat-messages/users/1/search")
						.header("X-User-Id", "10")
						.param("keyword", "Hel"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].content").value("Hello"));
	}

	@Test
	void deleteMessage_shouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/chat-messages/100").header("X-User-Id", "10"))
				.andExpect(status().isNoContent());

		verify(chatMessageService).deleteMessage(10L, 100L);
	}
}
