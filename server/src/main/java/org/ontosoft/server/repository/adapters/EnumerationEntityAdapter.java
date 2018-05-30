package org.ontosoft.server.repository.adapters;

import org.ontosoft.server.repository.SoftwareRepository;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.entities.EnumerationEntity;
import org.ontosoft.shared.classes.util.GUID;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class EnumerationEntityAdapter extends EntityAdapter {
  
  public EnumerationEntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String clsid) {
    super(kb, ontkb, enumkb, clsid, null, EnumerationEntity.class);
  }
  
  @Override
  public Entity getEntity(String id) {
    Entity entity = new EnumerationEntity();
    entity.setId(id);
    KBObject entityobj = this.ontkb.getIndividual(id);
    if(entityobj != null)
      entity = this.fetchEntityDetailsFromKB(entity, this.ontkb);
    else {
      entityobj = this.enumkb.getIndividual(id);
      entity = this.fetchEntityDetailsFromKB(entity, this.enumkb);
    }
    if(entityobj == null)
      return null;
    
    if(entity.getLabel() == null) 
      entity.setLabel(entityobj.getName());

    return entity;
  }

  @Override
  public boolean saveEntity(Entity entity) {
	if (entity.getLabel().equals("") 
			|| entity.getLabel() == null) {
		return false;
	}
    KBObject entityobj = this.ontkb.getIndividual(entity.getId());
    if(entityobj == null)
      entityobj = this.enumkb.getIndividual(entity.getId());

    SoftwareRepository repo = SoftwareRepository.get();
    if(entityobj == null) {
      // Check that there is no enumeration of the same type with the same label
      // If so, use that instead
      for(MetadataEnumeration menum : 
        repo.getEnumerationsForType(entity.getType())) {
        if(menum.getLabel().equalsIgnoreCase((String)entity.getValue())) {
          entity.setId(menum.getId());
          return true;
        }
      }
    }
    
    // If no entity found, then create a new one
    if(entityobj == null) {
      String etype = entity.getType().replaceAll("^.*/", "").replaceAll("^.*#", "");
      entity.setId(repo.ENUMNS() + etype + "-" + GUID.get());
      entityobj = this.enumkb.createObjectOfClass(entity.getId(), kbClass);
      this.enumkb.setLabel(entityobj, (String)entity.getValue());
      
      // Add new enumeration to vocabulary
      MetadataEnumeration menum = new MetadataEnumeration();
      menum.setId(entityobj.getID());
      menum.setName(entityobj.getName());
      menum.setLabel((String)entity.getValue());
      menum.setType(kbClass.getID());
      SoftwareRepository.get().addEnumerationToVocabulary(menum);
    }
    return true;
  }
}
