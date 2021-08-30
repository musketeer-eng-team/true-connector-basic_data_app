package it.eng.idsa.dataapp.web.rest;

import java.io.IOException;
import java.util.Optional;

import org.apache.http.entity.mime.MIME;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;

import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.util.MessageUtil;
import it.eng.idsa.multipart.builder.MultipartMessageBuilder;
import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Controller
@ConditionalOnProperty(name = "application.dataapp.http.config", havingValue = "mixed")
public class DataControllerBodyBinary {

	private static final Logger logger = LoggerFactory.getLogger(DataControllerBodyBinary.class);
	
	private MultiPartMessageService multiPartMessageService;
	private MessageUtil messageUtil;
	
	public DataControllerBodyBinary(MultiPartMessageService multiPartMessageService,
			MessageUtil messageUtil) {
		this.multiPartMessageService = multiPartMessageService;
		this.messageUtil = messageUtil;
	}
	
	@PostMapping(value = "/data")
	public ResponseEntity<?> routerBinary(@RequestHeader HttpHeaders httpHeaders,
			@RequestPart(value = "header") String headerMessage,
			@RequestHeader(value = "Response-Type", required = false) String responseType,
			@RequestPart(value = "payload", required = false) String payload) throws IOException, ParseException {

		logger.info("Multipart/mixed request");
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
			JSONObject requestBody = messageUtil.createRequestBody(payload);
			responsePayload = messageUtil.createResponsePayload(headerMessage, requestBody);
		}else {
			responsePayload = "Rejected message";
		}
		if (responsePayload.contains("ids:rejectionReason")) {
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

		return ResponseEntity.ok()
				.header("foo", "bar")
				.header(MIME.CONTENT_TYPE, contentType)
				.body(responseMessageString);
	}
}
