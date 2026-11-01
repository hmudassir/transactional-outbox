package dev.portfolio.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingMailSender {

	private static final Logger log = LoggerFactory.getLogger(LoggingMailSender.class);

	public void send(String recipient) {
		if (recipient.endsWith("@fail.test")) {
			throw new IllegalStateException("provider rejected the message");
		}
		log.info("Delivered notice to {}", recipient);
	}
}
