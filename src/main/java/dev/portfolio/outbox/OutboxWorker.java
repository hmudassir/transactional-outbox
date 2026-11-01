package dev.portfolio.outbox;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxWorker {

	private final OutboxRepository outbox;

	private final LoggingMailSender sender;

	private final OutboxWorker self;

	public OutboxWorker(OutboxRepository outbox, LoggingMailSender sender, @Lazy OutboxWorker self) {
		this.outbox = outbox;
		this.sender = sender;
		this.self = self;
	}

	public void deliverPending() {
		for (OutboxMessage message : outbox.findTop20ByStatusOrderByIdAsc(OutboxMessage.Status.PENDING)) {
			self.deliverOne(message.getId());
		}
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void deliverOne(Long id) {
		int claimed = outbox.claim(id, OutboxMessage.Status.SENDING, OutboxMessage.Status.PENDING);
		if (claimed == 0) {
			return;
		}
		OutboxMessage message = outbox.findById(id).orElseThrow();
		try {
			sender.send(message.getRecipient());
			message.setStatus(OutboxMessage.Status.SENT);
		}
		catch (RuntimeException exception) {
			message.setStatus(OutboxMessage.Status.FAILED);
			message.setAttempts(message.getAttempts() + 1);
		}
	}
}
