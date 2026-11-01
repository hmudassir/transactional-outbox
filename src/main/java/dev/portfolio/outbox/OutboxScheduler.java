package dev.portfolio.outbox;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Profile("!test")
public class OutboxScheduler {

	private final OutboxWorker worker;

	public OutboxScheduler(OutboxWorker worker) {
		this.worker = worker;
	}

	@Scheduled(fixedDelay = 5_000)
	public void deliver() {
		worker.deliverPending();
	}
}
