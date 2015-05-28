package org.earthcube.geosoft.client.components.form.formgroup.input;

import org.earthcube.geosoft.client.components.form.formgroup.input.events.HasEntityHandlers;
import org.earthcube.geosoft.shared.classes.Entity;
import org.earthcube.geosoft.shared.classes.vocabulary.MetadataProperty;
import org.earthcube.geosoft.shared.classes.vocabulary.Vocabulary;

import com.google.gwt.user.client.ui.IsWidget;

public interface IEntityInput extends IsWidget, HasEntityHandlers {
  
  public void createWidget(Entity entity, MetadataProperty prop, Vocabulary vocabulary);
  
  public Entity getValue();

  public void setValue(Entity entity);
  
  public void clearValue();
  
  public boolean validate(boolean show);
  
  public void layout();
  
}
