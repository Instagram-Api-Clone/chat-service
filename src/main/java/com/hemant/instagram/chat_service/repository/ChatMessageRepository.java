package com.hemant.instagram.chat_service.repository;

import com.hemant.instagram.chat_service.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	Page<ChatMessage> findByChatIdOrderByCreatedAtDesc(Long chatId, Pageable pageable);

	Page<ChatMessage> findByChatIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Long chatId, String keyword, Pageable pageable);
}
