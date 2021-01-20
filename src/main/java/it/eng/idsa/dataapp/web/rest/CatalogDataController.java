package it.eng.idsa.dataapp.web.rest;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.fraunhofer.iais.eis.Catalog;
import it.eng.idsa.dataapp.service.impl.CatalogServiceImpl;

@Controller
//@RequestMapping(value="/{$catalogId}")
public class CatalogDataController {

	private static final Logger logger = LogManager.getLogger(CatalogDataController.class);

	@Autowired
	CatalogServiceImpl catalogServiceImpl;

	@RequestMapping(value = "/{catalogId}", method = RequestMethod.POST)
	public ResponseEntity<?> saveCatalog(@RequestHeader HttpHeaders httpHeaders,
			@RequestBody(required = false) Catalog payload, @PathVariable String catalogId) {

		logger.info("Catalog request");
		logger.info("headers=" + httpHeaders);
		logger.info("payload" + payload);

		catalogServiceImpl.saveNewCatalog(payload);
		return ResponseEntity.created(payload.getId()).build();

	}

	@RequestMapping(value = "/{catalogId}", method = RequestMethod.GET)
	public ResponseEntity<Catalog> getCatalog(@PathVariable("catalogId") String id) {

		Catalog catalog = catalogServiceImpl.findById(URI.create(id));

		return ResponseEntity.ok(catalog);

	}

	@RequestMapping(value = "/{catalogId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteCatalog(@PathVariable("catalogId") URI id) {

		boolean isRemoved = catalogServiceImpl.deleteById(id);

		if (!isRemoved) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.noContent().build();
	}

}
