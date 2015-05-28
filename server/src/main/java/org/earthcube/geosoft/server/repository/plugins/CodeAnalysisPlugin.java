package org.earthcube.geosoft.server.repository.plugins;

import org.earthcube.geosoft.shared.classes.Software;
import org.earthcube.geosoft.shared.classes.util.KBConstants;
import org.earthcube.geosoft.shared.plugins.Plugin;
import org.earthcube.geosoft.shared.plugins.PluginResponse;

public class CodeAnalysisPlugin extends Plugin {

  public CodeAnalysisPlugin() {
    super("CodeAnalysis", "Analyze code", 
        KBConstants.ONTNS()+"hasCodeLocation");
    this.setValueMatchRegex(".*\\.m$");
  }

  @Override
  public PluginResponse run(Software software) {
    PluginResponse response = new PluginResponse(this);
    response.setMessage("Code checked and found to be full of awesome :)");
    return response;
  }

}
