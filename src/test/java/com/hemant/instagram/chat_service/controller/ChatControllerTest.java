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
import com.hemant.instagram.chat_service.dto.request.CreateChatRequest;
import com.hemant.instagram.chat_service.dto.response.ChatResponse;
import com.hemant.instagram.chat_service.service.ChatService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ChatController.class)
class ChatControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private ChatService chatService;

	@Test
	void startChat_shouldReturnCreated() throws Exception {
		CreateChatRequest request = CreateChatRequest.builder().targetUserId(20L).build();
		ChatResponse response = ChatResponse.builder().id(1L).userOneId(10L).userTwoId(20L).build();

		when(chatService.startChat(10L, 20L)).thenReturn(response);

		mockMvc.perform(post("/chats")
						.header("X-User-Id", "10")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id").value(1));

		verify(chatService).startChat(10L, 20L);
	}

	@Test
	void getUserChats_shouldReturnPagedChats() throws Exception {
		ChatResponse response = ChatResponse.builder().id(1L).userOneId(10L).userTwoId(20L).build();
		Page<ChatResponse> page = new PageImpl<>(List.of(response));

		when(chatService.getUserChats(eq(10L), eq(0), eq(20))).thenReturn(page);

		mockMvc.perform(get("/chats").header("X-User-Id", "10"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.content[0].id").value(1));
	}

	@Test
	void deleteChat_shouldReturnNoContent() throws Exception {
		mockMvc.perform(delete("/chats/users/20").header("X-User-Id", "10"))
				.andExpect(status().isNoContent());

		verify(chatService).deleteChat(10L, 20L);
	}
}
