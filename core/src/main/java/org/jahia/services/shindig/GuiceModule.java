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
import org.apache.shindig.social.core.oauth.AuthenticationHandlerProvider;
import org.apache.shindig.social.core.util.BeanXStreamAtomConverter;
import org.apache.shindig.social.core.util.xstream.XStream081Configuration;
import org.apache.shindig.social.opensocial.oauth.OAuthDataStore;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.sample.oauth.SampleOAuthDataStore;
import org.apache.shindig.social.sample.oauth.SampleRealm;
import org.apache.shindig.social.sample.service.SampleContainerHandler;
import org.springframework.context.ApplicationContext;
import org.jahia.hibernate.manager.SpringContextSingleton;

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

    private static final String JAHIA_SHINDIG_SERVICE_BEAN_NAME = "jahiaShindigService";

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

      // Get spring application context
      ApplicationContext applicationContext = SpringContextSingleton.getInstance().getContext();
      if (applicationContext == null) {
          return;
      }

      // Bind Mock Person Spi
      this.bind(PersonService.class).toInstance((PersonService) applicationContext.getBean(JAHIA_SHINDIG_SERVICE_BEAN_NAME));
      this.bind(ActivityService.class).toInstance((ActivityService) applicationContext.getBean(JAHIA_SHINDIG_SERVICE_BEAN_NAME));
      this.bind(AppDataService.class).toInstance((AppDataService) applicationContext.getBean(JAHIA_SHINDIG_SERVICE_BEAN_NAME));
      this.bind(MessageService.class).toInstance((MessageService) applicationContext.getBean(JAHIA_SHINDIG_SERVICE_BEAN_NAME));

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

}
