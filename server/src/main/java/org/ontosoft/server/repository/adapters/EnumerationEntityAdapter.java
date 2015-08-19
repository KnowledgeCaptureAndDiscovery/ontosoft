package org.ontosoft.server.repository.adapters;

import org.ontosoft.server.repository.EntityUtilities;
import org.ontosoft.server.repository.SoftwareRepository;
import org.ontosoft.shared.classes.Entity;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.classes.vocabulary.MetadataEnumeration;

import edu.isi.wings.ontapi.KBAPI;
import edu.isi.wings.ontapi.KBObject;

public class EnumerationEntityAdapter extends EntityAdapter {
  
  public EnumerationEntityAdapter(KBAPI kb, KBAPI ontkb, KBAPI enumkb, String clsid) {
    super(kb, ontkb, enumkb, clsid, null);
  }
  
  @Override
  public Entity getEntity(String id) {
    String label = null;
    KBObject entityobj = this.ontkb.getIndividual(id);
    if(entityobj != null)
      label = this.ontkb.getLabel(entityobj);
    else {
      entityobj = this.enumkb.getIndividual(id);
      label = this.enumkb.getLabel(entityobj);
    }
    if(entityobj == null)
      return null;
    
    if(label == null) 
      label = entityobj.getName();

    return new Entity(id, label, entityClass.getID());
  }

  @Override
  public boolean saveEntity(Entity entity) {
    KBObject entityobj = this.ontkb.getIndividual(entity.getId());
    if(entityobj == null)
      entityobj = this.enumkb.getIndividual(entity.getId());

    if(entityobj == null) {
      // Check that there is no enumeration of the same type with the same label
      // If so, use that instead
      for(MetadataEnumeration menum : 
        SoftwareRepository.get().getEnumerationsForType(entity.getType())) {
        if(menum.getLabel().equalsIgnoreCase((String)entity.getValue())) {
          entity.setId(menum.getId());
          return true;
        }
      }
    }
    
    // If no entity found, then create a new one
    if(entityobj == null) {
      String etype = entity.getType().replaceAll("^.*/", "").replaceAll("^.*#", "");
      entity.setId(KBConstants.ENUMNS() + etype + "-" + EntityUtilities.shortUUID());
      entityobj = this.enumkb.createObjectOfClass(entity.getId(), entityClass);
      this.enumkb.setLabel(entityobj, (String)entity.getValue());
      
      // Add new enumeration to vocabulary
      MetadataEnumeration menum = new MetadataEnumeration();
      menum.setId(entityobj.getID());
      menum.setName(entityobj.getName());
      menum.setLabel((String)entity.getValue());
      menum.setType(entityClass.getID());
      SoftwareRepository.get().addEnumerationToVocabulary(menum);
    }
    return true;
  }
}
