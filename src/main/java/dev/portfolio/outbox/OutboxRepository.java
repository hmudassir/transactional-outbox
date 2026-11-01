package dev.portfolio.outbox;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {

	List<OutboxMessage> findTop20ByStatusOrderByIdAsc(OutboxMessage.Status status);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
			update OutboxMessage m
			set m.status = :sending
			where m.id = :id and m.status = :pending
			""")
	int claim(
			@Param("id") Long id,
			@Param("sending") OutboxMessage.Status sending,
			@Param("pending") OutboxMessage.Status pending);
}
