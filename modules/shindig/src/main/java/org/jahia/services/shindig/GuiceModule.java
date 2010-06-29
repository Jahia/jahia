package org.jahia.services.shindig;

import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import org.apache.shindig.auth.AnonymousAuthenticationHandler;
import org.apache.shindig.auth.AuthenticationHandler;
import org.apache.shindig.common.servlet.ParameterFetcher;
import org.apache.shindig.protocol.DataServiceServletFetcher;
import org.apache.shindig.protocol.conversion.BeanConverter;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.protocol.conversion.BeanXStreamConverter;
import org.apache.shindig.protocol.conversion.xstream.XStreamConfiguration;
import org.apache.shindig.social.core.config.SocialApiGuiceModule;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.xstream.XStream081Configuration;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.MessageService;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

/**
 * Guice module to initialize Shindig SPI implementation
 *
 * @author loom
 *         Date: Aug 18, 2009
 *         Time: 7:55:54 AM
 */
public class GuiceModule extends SocialApiGuiceModule {

    private JahiaShindigService jahiaShindigService;

    @Override
    protected void configure() {
        bind(ParameterFetcher.class).annotatedWith(Names.named("DataServiceServlet"))
            .to(DataServiceServletFetcher.class);

        bind(Boolean.class)
            .annotatedWith(Names.named(AnonymousAuthenticationHandler.ALLOW_UNAUTHENTICATED))
            .toInstance(Boolean.TRUE);
        bind(XStreamConfiguration.class).to(XStream081Configuration.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.xml")).to(
            BeanXStreamConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.json")).to(
            BeanJsonConverter.class);
        bind(BeanConverter.class).annotatedWith(Names.named("shindig.bean.converter.atom")).to(
            BeanXStreamAtomConverter.class);

        bind(new TypeLiteral<List<AuthenticationHandler>>(){}).toProvider(
            JahiaAuthenticationHandlerProvider.class);

        bind(new TypeLiteral<Set<Object>>(){}).annotatedWith(Names.named("org.apache.shindig.social.handlers"))
            .toInstance(getHandlers());

        bind(Long.class).annotatedWith(Names.named("org.apache.shindig.serviceExpirationDurationMinutes")).toInstance(60L);

      // Bind Mock Person Spi
      this.bind(PersonService.class).toInstance(jahiaShindigService);
      this.bind(ActivityService.class).toInstance(jahiaShindigService);
      this.bind(AppDataService.class).toInstance(jahiaShindigService);
      this.bind(MessageService.class).toInstance(jahiaShindigService);

      bind(OAuthDataStore.class).to(JahiaOAuthDataStore.class);

      // We do this so that jsecurity realms can get access to the jsondbservice singleton
      requestStaticInjection(JahiaSecurityRealm.class);
    }

    @Override
    protected Set<Object> getHandlers() {
      ImmutableSet.Builder<Object> handlers = ImmutableSet.builder();
      handlers.addAll(super.getHandlers());
      // handlers.add(SampleContainerHandler.class);
      return handlers.build();
    }

    /**
     * @param jahiaShindigService the jahiaShindigService to set
     */
    public void setJahiaShindigService(JahiaShindigService jahiaShindigService) {
        this.jahiaShindigService = jahiaShindigService;
    }

}
