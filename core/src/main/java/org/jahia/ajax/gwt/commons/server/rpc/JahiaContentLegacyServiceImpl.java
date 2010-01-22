/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.commons.server.rpc;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.ajax.gwt.client.data.GWTJahiaAjaxActionResult;
import org.jahia.ajax.gwt.client.data.GWTJahiaBasicDataBean;
import org.jahia.ajax.gwt.client.data.GWTJahiaUserProperty;
import org.jahia.ajax.gwt.client.service.JahiaContentLegacyService;
import org.jahia.ajax.gwt.commons.server.JahiaRemoteService;
import org.jahia.bin.Jahia;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.UserProperties;
import org.jahia.services.usermanager.UserProperty;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.i18n.JahiaResourceBundle;

import java.util.*;

/**
 * This is the content service, for getting pages, containers and so on...
 */
public class JahiaContentLegacyServiceImpl extends JahiaRemoteService implements JahiaContentLegacyService {

    private static final ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
    private static final Logger logger = Logger.getLogger(JahiaContentLegacyServiceImpl.class);

    public List<GWTJahiaUserProperty> getJahiaUserProperties(boolean onlyMySettings) {
        List<GWTJahiaUserProperty> jahiaUserProperties = new ArrayList<GWTJahiaUserProperty>();

        try {
            JahiaUser user = getRemoteJahiaUser();
            UserProperties userProperties = user.getUserProperties();
            UserProperties missingProperties = new UserProperties();
			for (String propName : UserProperties.DEFAULT_PROPERTIES_NAME) {
				if (!userProperties.getProperties().containsKey(propName)) {
					final UserProperty missingProp;
					if ("emailNotificationsDisabled".equals(propName)) {
						missingProp = new UserProperty(propName, "false", false, UserProperty.CHECKBOX);

					} else if ("preferredLanguage".equals(propName)) {
						missingProp = new UserProperty(propName, getPreferredLocale(user).toString(), false,
						        UserProperty.SELECT_BOX);

					} else {
						missingProp = new UserProperty(propName, "", false);
					}
					missingProperties.setUserProperty(propName, missingProp);
				}
			}

            // add password
            final UserProperties all = new UserProperties();
            all.putAll(userProperties);
            all.putAll(missingProperties);
            final Iterator<String> propertyNameIterator = all.propertyNameIterator();
            while (propertyNameIterator.hasNext()) {
                // property name
                final String name = propertyNameIterator.next();
                final UserProperty property = all.getUserProperty(name);
                // create the corresponding gwt bean
                final GWTJahiaUserProperty gwtJahiaUserProperty = new GWTJahiaUserProperty();

                // Todo serialize the display attribute in UserProperty to remove this code
                if ("emailNotificationsDisabled".equals(property.getName())) {
                    final GWTJahiaBasicDataBean data = new GWTJahiaBasicDataBean();
                    data.setValue(property.getValue());
                    data.setDisplayName(data.getValue());
                    gwtJahiaUserProperty.setDisplay(UserProperty.CHECKBOX);
                    gwtJahiaUserProperty.setValue(data);

                } else if ("preferredLanguage".equals(property.getName())) {
                    gwtJahiaUserProperty.setDisplay(UserProperty.SELECT_BOX);
                    gwtJahiaUserProperty.setValues(getAvailableBundleLanguageBeans());
                    final Locale loc = LanguageCodeConverters.languageCodeToLocale(property.getValue());
                    gwtJahiaUserProperty.setValue(new GWTJahiaBasicDataBean(loc.toString(), loc.getDisplayName(loc)));

                } else {
                    final GWTJahiaBasicDataBean data = new GWTJahiaBasicDataBean();
                    data.setValue(property.getValue());
                    data.setDisplayName(data.getValue());
                    gwtJahiaUserProperty.setValue(data);
                    gwtJahiaUserProperty.setDisplay(UserProperty.TEXT_FIELD);
                }

                gwtJahiaUserProperty.setRealKey(property.getName());
                if (gwtJahiaUserProperty.isJahiaMySettingsProperty()) {
                    // label from resource bundle
                    gwtJahiaUserProperty.setLabel(getLocaleTemplateRessource("mySettings." + gwtJahiaUserProperty.getKey()));
                } else {
                    gwtJahiaUserProperty.setLabel(gwtJahiaUserProperty.getKey());
                }

                gwtJahiaUserProperty.setReadOnly(property.isReadOnly());
                gwtJahiaUserProperty.setPassword(false);

                // filter by MySettings
                if (onlyMySettings) {
                    // check if it's a customs mySettings prop.
                    boolean isCustomMySettingsProp = name.startsWith(GWTJahiaUserProperty.CUSTOM_USER_PROPERTY_PREFIX);
                    int index = gwtJahiaUserProperty.getJahiaMySettingsPropertyIndex();
                    if (index > -1) {
                        // case of jahiaUserProperties. add it to the 'top' of the list
                        if (jahiaUserProperties.size() > index) {
                            jahiaUserProperties.add(index, gwtJahiaUserProperty);
                        } else {
                            jahiaUserProperties.add(gwtJahiaUserProperty);
                        }
                    } else {
                        // case of customUserProperties. Add it to the 'bottom' of the list
                        if (isCustomMySettingsProp) {
                            jahiaUserProperties.add(gwtJahiaUserProperty);
                        }
                    }

                }
                // get all user properties
                else {
                    jahiaUserProperties.add(gwtJahiaUserProperty);
                }
            }
        } catch (Exception e) {
            logger.error("Can't retrive user properties due to:", e);
        }
        return jahiaUserProperties;
    }


    public GWTJahiaAjaxActionResult updateJahiaUserProperties(List<GWTJahiaUserProperty> newJahiaUserProperties, List<GWTJahiaUserProperty> removeJahiaUserProperties) {
        GWTJahiaAjaxActionResult gwtAjaxActionResult = new GWTJahiaAjaxActionResult();
        JahiaUser user = getRemoteJahiaUser();

        // update user properties
        for (GWTJahiaUserProperty gwtJahiaUserProperty : newJahiaUserProperties) {
            // update password
            if (gwtJahiaUserProperty.isPassword()) {
                String password = gwtJahiaUserProperty.getValue().toString();
                ServicesRegistry registry = ServicesRegistry.getInstance();
                JahiaPasswordPolicyService pwdPolicyService = registry.getJahiaPasswordPolicyService();
                boolean pwdPolicyEnabled = pwdPolicyService.isPolicyEnabled(user);

                // check if password is to short
                if (password != null && password.length() > 0) {
                    if (!pwdPolicyEnabled && password.length() < 6) {
                        gwtAjaxActionResult.addError(getLocaleMessageResource("org.jahia.engines.mysettings.passwordTooShort"));
                    } else {
                        // no prb. with pwd length
                        if (pwdPolicyEnabled) {
                            PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnPasswordChange(user, password, true);
                            if (!evalResult.isSuccess()) {
                                // password not validated by the pwdPolicyService
                                EngineMessages policyMsgs = evalResult.getEngineMessages();
                                for (EngineMessage errorMessage : policyMsgs.getMessages()) {
                                    gwtAjaxActionResult.addError(getLocaleMessageResource(errorMessage));
                                }
                            } else {
                                // password validated by the pwdPolicyService
                                user.setPassword(password);
                            }
                        } else {
                            // pwdPolicy not activated
                            user.setPassword(password);
                        }
                    }
                } else {
                    logger.debug("newPassword not set--> keep old one.");
                }
            }
            // update other properties
            else {
                UserProperty currentProp = user.getUserProperty(gwtJahiaUserProperty.getRealKey());
                if (currentProp == null) {
                    // add new properties
                    user.setProperty(gwtJahiaUserProperty.getRealKey(), gwtJahiaUserProperty.getValue().toString());
                } else if (!currentProp.getValue().equals(gwtJahiaUserProperty.getValue().getValue())) {
                    // update old properties
                    if (!currentProp.isReadOnly()) {
                        user.setProperty(gwtJahiaUserProperty.getRealKey(), gwtJahiaUserProperty.getValue().toString());
                    } else {
                        logger.debug("property[" + gwtJahiaUserProperty.getRealKey() + "] not update due to 'isReadOnly' flag.");
                    }
                }
            }
        }

        // remove user properties
        for (GWTJahiaUserProperty gwtJahiaUserProperty : removeJahiaUserProperties) {
            UserProperty currentProp = user.getUserProperty(gwtJahiaUserProperty.getRealKey());
            if (currentProp != null) {
                user.removeProperty(gwtJahiaUserProperty.getRealKey());
            }
        }

        return gwtAjaxActionResult;
    }

    private static Locale getPreferredLocale(JahiaUser user) {
        String propValue = user != null ? user.getProperty("preferredLanguage") : null;
        Locale locale = propValue != null ? LanguageCodeConverters.languageCodeToLocale(propValue) : null;

        if (null == locale) {
            // property is not set --> get list of site languages
            List<Locale> siteLocales = Collections.emptyList();
            final JahiaSite site = Jahia.getThreadParamBean().getSite();
            siteLocales = site.getLanguagesAsLocales();

            List<Locale> availableBundleLocales = getAvailableBundleLocales();
            for (Locale siteLocale : siteLocales) {
                if (availableBundleLocales.contains(siteLocale)) {
                    // this one is available
                    locale = siteLocale;
                    break;
                } else if (StringUtils.isNotEmpty(siteLocale.getCountry())) {

                    Locale languageOnlyLocale = new Locale(siteLocale.getLanguage());
                    if (availableBundleLocales.contains(languageOnlyLocale)) {
                        // get lanugtage without the country
                        locale = new Locale(siteLocale.getLanguage());
                        break;
                    }
                }
            }
            if (null == locale) {
                locale = Jahia.getThreadParamBean().getLocale();
            }
        }
        return locale;
    }

    public static List<Locale> getAvailableBundleLocales() {
        return LanguageCodeConverters.getAvailableBundleLocales(
                JahiaResourceBundle.JAHIA_INTERNAL_RESOURCES, null);
    }

    public static List<GWTJahiaBasicDataBean> getAvailableBundleLanguageBeans() {
        final List<Locale> tmp = getAvailableBundleLocales();
        final List<GWTJahiaBasicDataBean> result = new ArrayList<GWTJahiaBasicDataBean>();
        for (Locale locale : tmp) {
            result.add(new GWTJahiaBasicDataBean(locale.toString(), locale.getDisplayName(locale)));
        }
        return result;
    }
}
