package it.eng.idsa.dataapp.service;

import java.net.URI;

import de.fraunhofer.iais.eis.Catalog;

public interface CatalogService {
	
	 void saveNewCatalog(Catalog catalog);
	 boolean deleteById(URI id);
	 Catalog findById(URI id);

}
