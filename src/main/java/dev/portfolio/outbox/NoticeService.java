package dev.portfolio.outbox;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticeService {

	private final NoticeRepository notices;

	private final OutboxRepository outbox;

	public NoticeService(NoticeRepository notices, OutboxRepository outbox) {
		this.notices = notices;
		this.outbox = outbox;
	}

	@Transactional
	public Notice accept(String recipient, String body) {
		if ("rollback".equals(body)) {
			notices.save(new Notice(recipient, body));
			throw new IllegalArgumentException("refusing this notice");
		}
		Notice notice = notices.save(new Notice(recipient, body));
		outbox.save(new OutboxMessage(notice.getId(), recipient, body));
		return notice;
	}
}
