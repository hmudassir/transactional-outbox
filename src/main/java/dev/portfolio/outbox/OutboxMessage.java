package dev.portfolio.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "email_outbox")
public class OutboxMessage {

	public enum Status {
		PENDING,
		SENDING,
		SENT,
		FAILED
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "notice_id", nullable = false)
	private Long noticeId;

	@Column(nullable = false, length = 254)
	private String recipient;

	@Column(nullable = false, length = 1000)
	private String body;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private Status status;

	@Column(nullable = false)
	private int attempts;

	protected OutboxMessage() {
	}

	public OutboxMessage(Long noticeId, String recipient, String body) {
		this.noticeId = noticeId;
		this.recipient = recipient;
		this.body = body;
		this.status = Status.PENDING;
		this.attempts = 0;
	}

	public Long getId() {
		return id;
	}

	public Long getNoticeId() {
		return noticeId;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getBody() {
		return body;
	}

	public Status getStatus() {
		return status;
	}

	public int getAttempts() {
		return attempts;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setAttempts(int attempts) {
		this.attempts = attempts;
	}
}
