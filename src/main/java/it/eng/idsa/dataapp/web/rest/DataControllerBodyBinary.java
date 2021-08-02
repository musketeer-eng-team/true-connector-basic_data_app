package it.eng.idsa.dataapp.web.rest;

import java.util.Optional;

import org.apache.http.entity.mime.MIME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import com.fasterxml.jackson.core.JsonProcessingException;

import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.PayloadService;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "mixed")
public class DataControllerBodyBinary {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyBinary.class);
	
	private MultiPartMessageService multiPartMessageService;
	private PayloadService payloadService;
	
	public DataControllerBodyBinary(MultiPartMessageService multiPartMessageService,
			PayloadService payloadService) {
		this.multiPartMessageService = multiPartMessageService;
		this.payloadService = payloadService;
	}
	
	@PostMapping(value = "/data")
	public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
			@RequestPart(value = "header") String headerMessage,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestPart(value = "payload", required = false) String payload) throws JsonProcessingException {

		logger.info("Multipart/mixed request");

		// Convert de.fraunhofer.iais.eis.Message to the String
//		String headerSerialized = new Serializer().serializePlainJson(headerMessage);
		logger.info("header=" + headerMessage);
		logger.info("headers=" + httpHeaders);
		if (payload != null) {
			logger.info("payload length = " + payload.length());
		} else {
			logger.info("Payload is empty");
		}

		String headerResponse = multiPartMessageService.getResponseHeader(headerMessage);
		String responsePayload = null;
		if (!headerResponse.contains("ids:rejectionReason")) {
			responsePayload = payloadService.createPayload(multiPartMessageService.getIDSMessage(headerMessage), payload);
		}else {
			responsePayload = "Rejected message";
		}
		if (responsePayload != null && responsePayload.contains("ids:rejectionReason")) {
			headerResponse = responsePayload;
			responsePayload = "Rejected message";
		}
		MultipartMessage responseMessage = new MultipartMessageBuilder()
				.withHeaderContent(headerResponse)
				.withPayloadContent(responsePayload)
				.build();
		String responseMessageString = MultipartMessageProcessor.multipartMessagetoString(responseMessage, false);
		
		Optional<String> boundary = MultipartMessageProcessor.getMessageBoundaryFromMessage(responseMessageString);
		String contentType = "multipart/mixed; boundary=" + boundary.orElse("---aaa") + ";charset=UTF-8";

		logger.info("Sending response to ECC");
		return ResponseEntity.ok()
				.header("foo", "bar")
				.header(MIME.CONTENT_TYPE, contentType)
				.body(responseMessageString);
	}
}
