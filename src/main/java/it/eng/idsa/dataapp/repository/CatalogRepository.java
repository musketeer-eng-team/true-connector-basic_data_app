package it.eng.idsa.dataapp.repository;

import java.net.URI;

import de.fraunhofer.iais.eis.Catalog;

public interface CatalogRepository {
	
	 void saveNewCatalog(Catalog catalog);
	 boolean deleteById(URI id);
	 Catalog findById(URI id);
	
}
