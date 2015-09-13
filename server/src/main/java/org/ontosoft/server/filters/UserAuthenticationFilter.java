package org.ontosoft.server.filters;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.Context;

import org.ontosoft.server.users.UserAuthenticator;
import org.ontosoft.server.util.Config;
import org.ontosoft.shared.classes.users.UserSession;

@PreMatching
public class UserAuthenticationFilter implements ContainerRequestFilter {
  @Context 
  HttpServletRequest request;
  
  @Override
  public void filter(ContainerRequestContext context) throws IOException {
    Config.load(request);
    String token = context.getHeaderString(UserSession.SESSION_HEADER);
    if(token != null) {
      UserSession session = UserSession.getSession(token);
      context.setSecurityContext(new UserAuthenticator(session));
    }
  }

}
