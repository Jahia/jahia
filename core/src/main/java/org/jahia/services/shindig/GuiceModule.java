package org.jahia.services.shindig;

import org.apache.shindig.social.core.config.SocialApiGuiceModule;
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
      super.configure();

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
