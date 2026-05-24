package com.hemant.instagram.chat_service.controller;

import com.hemant.instagram.chat_service.dto.request.SendMessageRequest;
import com.hemant.instagram.chat_service.dto.response.ChatMessageResponse;
import com.hemant.instagram.chat_service.service.ChatMessageService;
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
@RequestMapping("/chat-messages")
@RequiredArgsConstructor
public class ChatMessageController {

	private final ChatMessageService chatMessageService;

	@Operation(summary = "Send Message")
	@PostMapping("/users/{receiverId}")
	public ResponseEntity<ChatMessageResponse> sendMessage(
			@RequestHeader("X-User-Id") Long senderId,
			@PathVariable Long receiverId,
			@Valid @RequestBody SendMessageRequest request) {
		return new ResponseEntity<>(chatMessageService.sendMessage(senderId, receiverId, request), HttpStatus.CREATED);
	}

	@Operation(summary = "Get Chat Messages")
	@GetMapping("/users/{receiverId}")
	public ResponseEntity<Page<ChatMessageResponse>> getChatMessages(
			@RequestHeader("X-User-Id") Long requesterId,
			@PathVariable Long receiverId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(chatMessageService.getChatMessages(requesterId, receiverId, page, size));
	}

	@Operation(summary = "Search Messages")
	@GetMapping("/users/{receiverId}/search")
	public ResponseEntity<Page<ChatMessageResponse>> searchMessages(
			@RequestHeader("X-User-Id") Long requesterId,
			@PathVariable Long receiverId,
			@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "20") int size) {
		return ResponseEntity.ok(chatMessageService.searchMessages(requesterId, receiverId, keyword, page, size));
	}

	@Operation(summary = "Delete Message")
	@DeleteMapping("/{messageId}")
	public ResponseEntity<Void> deleteMessage(
			@RequestHeader("X-User-Id") Long requesterId,
			@PathVariable Long messageId) {
		chatMessageService.deleteMessage(requesterId, messageId);
		return ResponseEntity.noContent().build();
	}
}
