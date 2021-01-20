package it.eng.idsa.dataapp.service.impl;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import de.fraunhofer.iais.eis.Catalog;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

public class CatalogServiceImplTest {

	
	@Test
	public void vojkan() throws IOException {
		Catalog c = new ResourceCatalogBuilder()
				.build();
		 Serializer serializer = new Serializer();
	        String serializePlainJson = serializer.serialize(c);
	        System.out.println(serializePlainJson);
	}
	
	
	

}
