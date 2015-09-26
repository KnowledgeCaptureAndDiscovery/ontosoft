package org.ontosoft.client.generator;


public interface EntityFactory {
  public Object instantiate( String className );
  public boolean hasClass( String className );
}