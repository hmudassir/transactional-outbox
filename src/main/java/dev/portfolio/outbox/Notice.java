package dev.portfolio.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notices")
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 254)
	private String recipient;

	@Column(nullable = false, length = 1000)
	private String body;

	protected Notice() {
	}

	public Notice(String recipient, String body) {
		this.recipient = recipient;
		this.body = body;
	}

	public Long getId() {
		return id;
	}

	public String getRecipient() {
		return recipient;
	}

	public String getBody() {
		return body;
	}
}
