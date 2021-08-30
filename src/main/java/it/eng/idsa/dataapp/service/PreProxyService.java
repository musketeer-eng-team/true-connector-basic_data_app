package it.eng.idsa.dataapp.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public interface PreProxyService {
	
	JSONObject parsePreProxyRequest(String requestURI, String body, HttpHeaders httpHeaders, HttpMethod method) throws ParseException;

	ResponseEntity<?> forwardToProxy(JSONObject parsedPreProxyRequest, HttpHeaders httpHeaders);

	ResponseEntity<?> retrievePayload(ResponseEntity<?> forwardToProxy);
}
