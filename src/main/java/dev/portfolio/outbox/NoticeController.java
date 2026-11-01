package dev.portfolio.outbox;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
public class NoticeController {

	private final NoticeService notices;

	public NoticeController(NoticeService notices) {
		this.notices = notices;
	}

	@PostMapping("/api/notices")
	public org.springframework.http.ResponseEntity<NoticeResponse> create(@Valid @RequestBody NoticeRequest request) {
		Notice notice = notices.accept(request.recipient().trim(), request.body().trim());
		return org.springframework.http.ResponseEntity.status(HttpStatus.ACCEPTED)
				.body(new NoticeResponse(notice.getId(), "PENDING"));
	}

	public record NoticeRequest(
			@NotBlank @Email @Size(max = 254) String recipient,
			@NotBlank @Size(max = 1000) String body) {
	}

	public record NoticeResponse(Long id, String delivery) {
	}
}
