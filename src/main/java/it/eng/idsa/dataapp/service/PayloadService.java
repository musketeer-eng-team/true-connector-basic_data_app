package it.eng.idsa.dataapp.service;

import de.fraunhofer.iais.eis.Message;

public interface PayloadService {

	String createPayload(Message headerMessage, String payload);
}
