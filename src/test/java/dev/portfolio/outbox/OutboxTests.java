package dev.portfolio.outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OutboxTests {

	@Autowired
	private NoticeService notices;

	@Autowired
	private OutboxWorker worker;

	@Autowired
	private OutboxRepository outbox;

	@Autowired
	private NoticeRepository noticeRepository;

	@BeforeEach
	void empty() {
		outbox.deleteAll();
		noticeRepository.deleteAll();
	}

	@Test
	void aFailedRequestLeavesNoOutboxRow() {
		assertThatThrownBy(() -> notices.accept("ada@example.com", "rollback"))
				.isInstanceOf(IllegalArgumentException.class);
		assertThat(noticeRepository.count()).isZero();
		assertThat(outbox.count()).isZero();
	}

	@Test
	void workerSendsTheSavedMessageOnce() {
		Notice notice = notices.accept("ada@example.com", "Your note is saved");
		assertThat(outbox.findAll()).singleElement().extracting(OutboxMessage::getStatus)
				.isEqualTo(OutboxMessage.Status.PENDING);

		worker.deliverPending();
		worker.deliverPending();

		OutboxMessage message = outbox.findAll().stream()
				.filter(row -> row.getNoticeId().equals(notice.getId()))
				.findFirst()
				.orElseThrow();
		assertThat(message.getStatus()).isEqualTo(OutboxMessage.Status.SENT);
	}

	@Test
	void aRejectedProviderMarksTheRowFailed() {
		notices.accept("ada@fail.test", "try later");
		worker.deliverPending();
		OutboxMessage message = outbox.findAll().stream()
				.filter(row -> row.getRecipient().equals("ada@fail.test"))
				.findFirst()
				.orElseThrow();
		assertThat(message.getStatus()).isEqualTo(OutboxMessage.Status.FAILED);
		assertThat(message.getAttempts()).isEqualTo(1);
	}
}
