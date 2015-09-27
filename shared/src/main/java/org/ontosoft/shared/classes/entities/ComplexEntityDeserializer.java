package org.ontosoft.shared.classes.entities;

//import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;

public class ComplexEntityDeserializer 
  extends StdConverter<Map<String, List<Object>>, Map<String, List<Entity>>> {
  
  ObjectMapper mapper = new ObjectMapper();
  
  @SuppressWarnings("unchecked")
  public Map<String, List<Entity>> convert(Map<String, List<Object>> in) {
    try {
      String json = mapper.writeValueAsString(in);
      return (Map<String, List<Entity>>) mapper.readValue(json, 
          new TypeReference<Map<String, List<Entity>>>() { });
      /*Map<String, List<Entity>> out = new LinkedHashMap<String, List<Entity>>();
      for(String key : in.keySet()) {
        String json = mapper.writeValueAsString(in.get(key));
        out.put(key, (List<Entity>) mapper.readValue(json, new TypeReference<List<Entity>>() { }));
      }
      return out;*/
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }

}
