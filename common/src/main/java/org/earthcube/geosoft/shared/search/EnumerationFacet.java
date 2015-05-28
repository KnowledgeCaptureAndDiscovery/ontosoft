package org.earthcube.geosoft.shared.search;

import java.util.List;

/**
 * A search facet can contain multiple properties to search in
 * -- The properties should have the same range/type
 */
public class EnumerationFacet {
  String facetId;
  List<String> propertyIds;
  List<String> enumerationIds;

  public String getFacetId() {
    return facetId;
  }

  public void setFacetId(String facetId) {
    this.facetId = facetId;
  }

  public List<String> getPropertyIds() {
    return propertyIds;
  }

  public void setPropertyIds(List<String> propertyIds) {
    this.propertyIds = propertyIds;
  }

  public List<String> getEnumerationIds() {
    return enumerationIds;
  }

  public void setEnumerationIds(List<String> enumerationIds) {
    this.enumerationIds = enumerationIds;
  }
}
