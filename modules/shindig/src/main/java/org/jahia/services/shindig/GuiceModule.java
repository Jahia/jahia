/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.shindig;

import com.google.common.collect.ImmutableSet;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
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
import org.apache.shindig.social.opensocial.service.ActivityHandler;
import org.apache.shindig.social.opensocial.service.AlbumHandler;
import org.apache.shindig.social.opensocial.service.AppDataHandler;
import org.apache.shindig.social.opensocial.service.MediaItemHandler;
import org.apache.shindig.social.opensocial.service.MessageHandler;
import org.apache.shindig.social.opensocial.service.PersonHandler;
import org.apache.shindig.social.opensocial.spi.ActivityService;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.PersonService;
import org.apache.shindig.social.opensocial.spi.MessageService;

import java.util.List;
import java.util.Set;

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

        Multibinder<Object> handlerBinder = Multibinder.newSetBinder(binder(), Object.class, Names.named("org.apache.shindig.handlers"));
        for (Class handler : getHandlers()) {
          handlerBinder.addBinding().toInstance(handler);
        }

      // Bind Mock Person Spi
      this.bind(PersonService.class).toInstance(jahiaShindigService);
      this.bind(ActivityService.class).toInstance(jahiaShindigService);
      this.bind(AppDataService.class).toInstance(jahiaShindigService);
      this.bind(MessageService.class).toInstance(jahiaShindigService);

      bind(OAuthDataStore.class).to(JahiaOAuthDataStore.class);

      // We do this so that jsecurity realms can get access to the jsondbservice singleton
      requestStaticInjection(JahiaSecurityRealm.class);
    }

    /**
     * @param jahiaShindigService the jahiaShindigService to set
     */
    public void setJahiaShindigService(JahiaShindigService jahiaShindigService) {
        this.jahiaShindigService = jahiaShindigService;
    }

    @Override
    protected Set<Class<?>> getHandlers() {
        return ImmutableSet.<Class<?>>of(ActivityHandler.class, AppDataHandler.class,
                PersonHandler.class, MessageHandler.class);
    }
}
