package com.hemant.instagram.chat_service.repository;

import com.hemant.instagram.chat_service.entity.Chat;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

	Optional<Chat> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

	@Query("SELECT c FROM Chat c WHERE c.userOneId = :userId OR c.userTwoId = :userId")
	Page<Chat> findChatsByUserId(@Param("userId") Long userId, Pageable pageable);
}
