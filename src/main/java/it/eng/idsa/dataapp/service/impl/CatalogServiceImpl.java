package it.eng.idsa.dataapp.service.impl;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.fraunhofer.iais.eis.Catalog;
import it.eng.idsa.dataapp.repository.impl.CatalogHashMapRepositoryImpl;
import it.eng.idsa.dataapp.service.CatalogService;

@Service
public class CatalogServiceImpl implements CatalogService {
	
	@Autowired
	private CatalogHashMapRepositoryImpl catalogHashMapRepositoryImpl;

	@Override
	public void saveNewCatalog(Catalog catalog) {
		catalogHashMapRepositoryImpl.saveNewCatalog(catalog);
	}

	@Override
	public boolean deleteById(URI id) {
		return catalogHashMapRepositoryImpl.deleteById(id);
	}

	@Override
	public Catalog findById(URI id) {
		return catalogHashMapRepositoryImpl.findById(id);
	}

}
