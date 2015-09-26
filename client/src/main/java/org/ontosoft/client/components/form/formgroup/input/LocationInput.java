package org.ontosoft.client.components.form.formgroup.input;

import org.gwtbootstrap3.client.ui.constants.InputType;
import org.ontosoft.shared.classes.entities.Entity;
import org.ontosoft.shared.classes.vocabulary.MetadataProperty;
import org.ontosoft.shared.classes.vocabulary.Vocabulary;

public class LocationInput extends EntityInput {
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocab) {
    super.createWidget(e, prop, vocab);
    input.setType(InputType.URL);
    input.setPlaceholder(prop.getLabel() + " (URL)");
    input.addValidator(Validators.URL);
  }
}
