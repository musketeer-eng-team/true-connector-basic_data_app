package it.eng.idsa.dataapp.service.impl;

import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.PreProxyService;
import it.eng.idsa.dataapp.service.ProxyService;
import it.eng.idsa.dataapp.service.RecreateFileService;

import it.eng.idsa.multipart.domain.MultipartMessage;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class PreProxyServiceImpl implements PreProxyService {
	private static final String MULTIPART = "multipart";
	private static final String MESSAGE = "message";
	private static final String PAYLOAD = "payload";
	private static final String REQUESTED_ARTIFACT = "requestedArtifact";
	private static final String FORWARD_TO = "Forward-To";
	private static final String FORWARD_TO_INTERNAL = "Forward-To-Internal";
	private static final String MESSAGE_AS_HEADERS = "messageAsHeaders";

	private static final Logger logger = LoggerFactory.getLogger(ProxyService.class);

	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	private MultiPartMessageService multiPartMessageService;
	private RecreateFileService recreateFileService;
	private String dataLakeDirectory;
	private URI proxyURI;

	public PreProxyServiceImpl(RestTemplateBuilder restTemplateBuilder, ECCProperties eccProperties,
                               MultiPartMessageService multiPartMessageService, RecreateFileService recreateFileService,
                               @Value("${application.dataLakeDirectory}") String dataLakeDirectory) throws URISyntaxException {
		this.restTemplate = restTemplateBuilder.build();
		this.eccProperties = eccProperties;
		this.multiPartMessageService = multiPartMessageService;
		this.recreateFileService = recreateFileService;
		this.dataLakeDirectory = dataLakeDirectory;
		this.proxyURI = new URI("https://localhost:8083/proxy");
	}
	
	@Override
	public JSONObject parsePreProxyRequest(String requestURI, String body, HttpHeaders httpHeaders, HttpMethod method) throws ParseException {

		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(body);;
		JSONObject parsedJsonObject = new JSONObject();
		JSONObject payloadObject = new JSONObject();

		String[] splitRequestURI = requestURI.split("/");

		parsedJsonObject.put(PAYLOAD, payloadObject);
		payloadObject.put("body", method.name());
		payloadObject.put("http-method", method.name());
		payloadObject.put("endpoint", splitRequestURI[splitRequestURI.length - 1]);
		// Set headers
		JSONObject headers = new JSONObject();
		httpHeaders.forEach((k, v) -> {
			headers.put(k, v.get(0));
		});
		payloadObject.put("headers", headers);

		parsedJsonObject.put(MULTIPART, splitRequestURI[splitRequestURI.length - 2]);
		parsedJsonObject.put(FORWARD_TO, jsonObject.get(FORWARD_TO));
		parsedJsonObject.put(FORWARD_TO_INTERNAL, jsonObject.get(FORWARD_TO_INTERNAL));
		parsedJsonObject.put(REQUESTED_ARTIFACT, jsonObject.get(REQUESTED_ARTIFACT));

		String message = "{\"@context\": {\"ids\": \"https://w3id.org/idsa/core/\" },\"@type\": \"ids:ArtifactRequestMessage\",\"ids:issued\": {\"@value\": \"2020-11-25T16:43:27.051+01:00\",\"@type\": \"http://www.w3.org/2001/XMLSchema#dateTimeStamp\" },\"ids:modelVersion\": \"4.0.0\",\"ids:issuerConnector\": {\"@id\": \"http://w3id.org/engrd/connector/\" },\"ids:requestedArtifact\": {\"@id\": \"http://w3id.org/engrd/connector/artifact/1\" },\"ids:senderAgent\": \"https://sender.agent.com\"}";
		JSONObject partJson = (JSONObject) parser.parse(message);
		String parsedMessage =  partJson != null ? partJson.toJSONString().replace("\\/","/") : null;
		parsedJsonObject.put(MESSAGE, parser.parse(parsedMessage));

		String messageAsHeaders = "{\n" +
				"        \"IDS-RequestedArtifact\":\"http://w3id.org/engrd/connector/artifact/1\",\n" +
				"        \"IDS-Messagetype\":\"ids:ArtifactRequestMessage\",\n" +
				"        \"IDS-ModelVersion\":\"4.0.0\",\n" +
				"        \"IDS-Issued\":\"2021-01-15T13:09:42.306Z\",\n" +
				"        \"IDS-Id\":\"https://w3id.org/idsa/autogen/artifactResponseMessage/eb3ab487-dfb0-4d18-b39a-585514dd044f\",\n" +
				"        \"IDS-IssuerConnector\":\"http://w3id.org/engrd/connector/\"\n" +
				"        }";
		parsedJsonObject.put(MESSAGE_AS_HEADERS, parser.parse(messageAsHeaders));

		return parsedJsonObject;
	}

	@Override
	public ResponseEntity<?> forwardToProxy(JSONObject parsedPreProxyRequest, HttpHeaders httpHeaders) {

		HttpEntity<String> requestEntity = new HttpEntity<String>(parsedPreProxyRequest.toJSONString(), httpHeaders);

		ResponseEntity<String> resp = restTemplate.exchange(proxyURI, HttpMethod.POST, requestEntity, String.class);

		return resp;
	}

	@Override
	public ResponseEntity<?> retrievePayload(ResponseEntity responseEntity) {

		MultipartMessage responseMessage = MultipartMessageProcessor.parseMultipartMessage((String) responseEntity.getBody());
		String body = responseMessage.getPayloadContent();

		HttpHeaders httpHeaders = new HttpHeaders();
		responseEntity.getHeaders().forEach((k, v) -> {
			httpHeaders.add(k, v.get(0));
		});

		httpHeaders.setContentType(MediaType.APPLICATION_JSON);

		return ResponseEntity.status(responseEntity.getStatusCode()).headers(httpHeaders).body(body);
	}
}
