package com.hemant.instagram.chat_service.controller;

import com.hemant.instagram.chat_service.dto.request.CreateChatRequest;
import com.hemant.instagram.chat_service.dto.response.ChatResponse;
import com.hemant.instagram.chat_service.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chats")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	@Operation(summary = "Start Chat")
	@PostMapping
	public ResponseEntity<ChatResponse> startChat(
			@RequestHeader("X-User-Id") Long requesterId,
			@Valid @RequestBody CreateChatRequest request) {
		return new ResponseEntity<>(chatService.startChat(requesterId, request.getTargetUserId()), HttpStatus.CREATED);
	}

	@Operation(summary = "Get User Chats")
	@GetMapping
	public ResponseEntity<Page<ChatResponse>> getUserChats(
			@RequestHeader("X-User-Id") Long requesterId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(chatService.getUserChats(requesterId, page, size));
	}

	@Operation(summary = "Delete Chat")
	@DeleteMapping("/users/{targetUserId}")
	public ResponseEntity<Void> deleteChat(
			@RequestHeader("X-User-Id") Long requesterId,
			@PathVariable Long targetUserId) {
		chatService.deleteChat(requesterId, targetUserId);
		return ResponseEntity.noContent().build();
	}
}
