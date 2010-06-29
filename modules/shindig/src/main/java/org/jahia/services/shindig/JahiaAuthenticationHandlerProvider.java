package org.jahia.services.shindig;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Provider;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.auth.UrlParameterAuthenticationHandler;
import org.apache.shindig.social.core.oauth.OAuthAuthenticationHandler;

import java.util.List;

/**
 * Based on AuthenticationHandlerProvider to add Jahia's authentication handler.
 *
 * @author loom
 *         Date: Jun 24, 2010
 *         Time: 11:47:09 AM
 */
public class JahiaAuthenticationHandlerProvider implements Provider<List<AuthenticationHandler>> {
  protected List<AuthenticationHandler> handlers;

  @Inject
  public JahiaAuthenticationHandlerProvider(UrlParameterAuthenticationHandler urlParam,
      OAuthAuthenticationHandler threeLeggedOAuth,
      JahiaAuthentificationHandler jahia,
      AnonymousAuthenticationHandler anonymous) {
    handlers = Lists.newArrayList(urlParam, threeLeggedOAuth, jahia, anonymous);
  }

  public List<AuthenticationHandler> get() {
    return handlers;
  }
}