package it.eng.idsa.dataapp.web.rest;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.fraunhofer.iais.eis.Catalog;
import it.eng.idsa.dataapp.service.impl.CatalogServiceImpl;

@Controller
public class CatalogDataController {

	private static final Logger logger = LogManager.getLogger(CatalogDataController.class);

	@Autowired
	CatalogServiceImpl catalogServiceImpl;

	@RequestMapping(value = "/**", method = RequestMethod.POST)
	public ResponseEntity<?> saveCatalog(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) Catalog payload, HttpServletRequest request) {

		logger.info("Catalog request");
		logger.info("headers=" + httpHeaders);
		logger.info("payload" + payload.getId());

		String catalogUri = request.getRequestURI().split(request.getContextPath() + "/")[1];
		String decodedCatalogUri = URLDecoder.decode(catalogUri, StandardCharsets.UTF_8);
		catalogServiceImpl.saveNewCatalog(payload);
		return ResponseEntity.created(payload.getId()).build();
	}

	@RequestMapping(value = "/**", method = RequestMethod.GET)
	public ResponseEntity<Catalog> getCatalog(HttpServletRequest request) {

		String catalogUri = request.getRequestURI().split(request.getContextPath() + "/")[1];
		String decodedCatalogUri = URLDecoder.decode(catalogUri, StandardCharsets.UTF_8);
		Catalog catalog = catalogServiceImpl.findById(URI.create(decodedCatalogUri));

		logger.info("catalog id=" + decodedCatalogUri);
		return ResponseEntity.ok(catalog);

	}

	@RequestMapping(value = "/**", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCatalog(HttpServletRequest request) {

		String catalogUri = request.getRequestURI().split(request.getContextPath() + "/")[1];
		String decodedCatalogUri = URLDecoder.decode(catalogUri, StandardCharsets.UTF_8);
		boolean isRemoved = catalogServiceImpl.deleteById(URI.create(decodedCatalogUri));

		if (!isRemoved) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.noContent().build();
	}

}
