package it.eng.idsa.dataapp.repository.impl;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Repository;

import de.fraunhofer.iais.eis.Catalog;
import it.eng.idsa.dataapp.repository.CatalogRepository;

@Repository
public class CatalogHashMapRepositoryImpl implements CatalogRepository {

	Map<URI, Catalog> catalogRepositoryMap = new HashMap<>();

	@Override
	public void saveNewCatalog(Catalog catalog) {
		catalogRepositoryMap.put(catalog.getId(), catalog);
	}

	@Override
	public boolean deleteById(URI id) {
		if (catalogRepositoryMap.remove(id) == null) {
			return false;
		}
		return true;
	}

	@Override
	public Catalog findById(URI id) {
		return catalogRepositoryMap.get(id);
	}

}
