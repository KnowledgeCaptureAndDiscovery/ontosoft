package org.earthcube.geosoft.client.components.form.formgroup.input;

import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;
import org.gwtbootstrap3.client.ui.constants.InputType;

public class LocationInput extends EntityInput {
  
  @Override
  public void createWidget(Entity e, MetadataProperty prop, Vocabulary vocab) {
    super.createWidget(e, prop, vocab);
    input.setType(InputType.URL);
    input.setPlaceholder(prop.getLabel() + " (URL)");
    input.addValidator(Validators.URL);
  }
}
