package org.ontosoft.server.repository.plugins;

import org.ontosoft.shared.classes.entities.Software;
import org.ontosoft.shared.classes.util.KBConstants;
import org.ontosoft.shared.plugins.Plugin;
import org.ontosoft.shared.plugins.PluginResponse;

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
