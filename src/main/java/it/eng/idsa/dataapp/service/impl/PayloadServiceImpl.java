package it.eng.idsa.dataapp.service.impl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.Connector;
import de.fraunhofer.iais.eis.ContractAgreementMessage;
import de.fraunhofer.iais.eis.ContractRequestMessage;
import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Resource;
import de.fraunhofer.iais.eis.ResourceCatalog;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import it.eng.idsa.dataapp.configuration.ECCProperties;
import it.eng.idsa.dataapp.service.MultiPartMessageService;
import it.eng.idsa.dataapp.service.PayloadService;
import it.eng.idsa.multipart.processor.MultipartMessageProcessor;

@Service
public class PayloadServiceImpl implements PayloadService {
	
	private static final Logger logger = LoggerFactory.getLogger(PayloadServiceImpl.class);
	
	private Path dataLakeDirectory;
	
	private MultiPartMessageService multiPartMessageService;
	private RestTemplate restTemplate;
	private ECCProperties eccProperties;
	
	public PayloadServiceImpl(@Value("${application.dataLakeDirectory}") Path dataLakeDirectory,
			MultiPartMessageService multiPartMessageService,
			RestTemplate restTemplate,
			ECCProperties eccProperties) {
		this.dataLakeDirectory = dataLakeDirectory;
		this.multiPartMessageService = multiPartMessageService;
		this.restTemplate = restTemplate;
		this.eccProperties = eccProperties;
	}
	
	@Override
	public String createPayload(Message headerMessage, String payload) {
		if (headerMessage instanceof ContractRequestMessage) {
			return createContractAgreement(dataLakeDirectory, payload);
		} else if (headerMessage instanceof ContractAgreementMessage) {
			return null;
		} else if (headerMessage instanceof DescriptionRequestMessage) {
			if (((DescriptionRequestMessage) headerMessage).getRequestedElement() != null) {
				String element = getRequestedElement(
						((DescriptionRequestMessage) headerMessage).getRequestedElement(), 
						getSelfDescription());
				if (StringUtils.isNotBlank(element)) {
					return element;
				} else {
					try {
						return MultipartMessageProcessor.serializeToJsonLD(
								multiPartMessageService.createRejectionCommunicationLocalIssues(headerMessage));
					} catch (IOException e) {
						logger.error("Could not serialize rejection", e);
					}
					return null;
				}
			} else {
				return getSelfDescriptionAsString();
			}
		} else if(headerMessage instanceof ArtifactRequestMessage) {
			return createArtifactResponsePayload(((ArtifactRequestMessage) headerMessage).getRequestedArtifact());
		} else {
			return createDefaultPayload();
		}
	}

	
	private String createArtifactResponsePayload(@NotNull URI requestedArtifact) {
		if(requestedArtifact.equals(URI.create("http://w3id.org/engrd/connector/artifact/igor"))) {
			return createData("Igor", "Balog", "24.12.1980.", "Novi Sad", "team lead");
		} else if(requestedArtifact.equals(URI.create("http://w3id.org/engrd/connector/artifact/david"))) {
			return createData("David", "Jovanovic", "12.05.1993.", "Negotin", "developer");
		} else if(requestedArtifact.equals(URI.create("http://w3id.org/engrd/connector/artifact/petar"))) {
			return createData("Petar", "Crepulja", "01.01.1976.", "Belgrade", "developer");
		} else if(requestedArtifact.equals(URI.create("http://w3id.org/engrd/connector/artifact/mattia"))) {
			return createData("Mattia Giuseppe", "Marzano", "22 6 1992.", "Italy", "researcher");
		} else if(requestedArtifact.equals(URI.create("http://w3id.org/engrd/connector/artifact/gabriele"))) {
			return createData("Gabriele", "De Luca", "26.11.1984.", "Lecce", "technical manager");
		}
		return createDefaultPayload();
	}

	private String createContractAgreement(Path dataLakeDirectory, String payload) {
		String contractAgreement = null;
		byte[] bytes;
		String fileName;
		if(payload.contains("http://w3id.org/engrd/connector/artifact/igor")) {
			fileName = "contract_agreement_igor.json";
		} else if (payload.contains("http://w3id.org/engrd/connector/artifact/david")) {
			fileName = "contract_agreement_david.json";
		} else if (payload.contains("http://w3id.org/engrd/connector/artifact/petar")) {
			fileName = "contract_agreement_petar.json";
		} else if (payload.contains("http://w3id.org/engrd/connector/artifact/mattia")) {
			fileName = "contract_agreement_mattia.json";
		} else if (payload.contains("http://w3id.org/engrd/connector/artifact/gabriele")) {
			fileName = "contract_agreement_gabriele.json";
		} else {
			fileName = "contract_agreement.json";
		}
		
		try {
			bytes = Files.readAllBytes(dataLakeDirectory.resolve(fileName));
			contractAgreement = IOUtils.toString(bytes, "UTF8");
		} catch (IOException e) {
			logger.error("Error while reading contract agreement file from dataLakeDirectory {}", e);
		}
		return contractAgreement;
	}
	
	private Connector getSelfDescription() {
		URI eccURI = null;

		try {
			eccURI = new URI(eccProperties.getRESTprotocol(), null, eccProperties.getHost(), eccProperties.getRESTport(), null, null, null);
			logger.info("Fetching self description from ECC {}.", eccURI.toString());
			String selfDescription = restTemplate.getForObject(eccURI, String.class);
			logger.info("Deserializing self description.");
			logger.debug("Self description content: {}{}", System.lineSeparator(), selfDescription);
			return new Serializer().deserialize(selfDescription, Connector.class);
		} catch (URISyntaxException e) {
			logger.error("Could not create URI for Self Description request.", e);
			return null;
		} catch (RestClientException e) {
			logger.error("Could not fetch self description from ECC", e);
			return null;
		} catch (IOException e) {
			logger.error("Could not deserialize self description to Connector instance", e);
			return null;
		}
	}
	
	
	private String getRequestedElement(URI requestedElement, Connector connector) {
		for (ResourceCatalog catalog : connector.getResourceCatalog()) {
			for (Resource offeredResource : catalog.getOfferedResource()) {
				if (requestedElement.equals(offeredResource.getId())) {
					try {
						return MultipartMessageProcessor.serializeToJsonLD(offeredResource);
					} catch (IOException e) {
						logger.error("Could not serialize requested element.", e);
					}
				}
			}
		}
		logger.error("Requested element not found.");
		return null;
	}
	
	private String getSelfDescriptionAsString() {
		try {
			return MultipartMessageProcessor.serializeToJsonLD(getSelfDescription());
		} catch (IOException e) {
			logger.error("Could not serialize self description", e);
		}
		return null;
	}
	
	private String createData(String firstName, String lastName, String dob, String address, String role) {
		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", firstName);
		jsonObject.put("lastName", lastName);
		jsonObject.put("dateOfBirth", dob);
		jsonObject.put("address", address);
		jsonObject.put("role", role);
		return new GsonBuilder().create().toJson(jsonObject);
	}
	
	private String createDefaultPayload() {
		// Put check sum in the payload
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String formattedDate = dateFormat.format(date);

		Map<String, String> jsonObject = new HashMap<>();
		jsonObject.put("firstName", "John");
		jsonObject.put("lastName", "Doe");
		jsonObject.put("dateOfBirth", formattedDate);
		jsonObject.put("address", "591  Franklin Street, Pennsylvania");
		jsonObject.put("checksum", "ABC123 " + formattedDate);
		Gson gson = new GsonBuilder().create();
		return gson.toJson(jsonObject);
	}
	
}
