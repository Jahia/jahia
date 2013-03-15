/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.modules.serversettings.flow;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.viewhelper.principal.PrincipalViewHelper;
import org.jahia.modules.serversettings.users.management.CsvFile;
import org.jahia.modules.serversettings.users.management.SearchCriteria;
import org.jahia.modules.serversettings.users.management.UserProperties;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.pwdpolicy.JahiaPasswordPolicyService;
import org.jahia.services.pwdpolicy.PolicyEnforcementResult;
import org.jahia.services.usermanager.*;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.security.Principal;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 13/03/13
 */
public class UsersFlowHandler implements Serializable {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(UsersFlowHandler.class);

    public Set<Principal> init() {
        return PrincipalViewHelper.getSearchResult(null, null, null, null, null);
    }

    public SearchCriteria initCriteria() {
        return new SearchCriteria();
    }

    public Set<Principal> search(SearchCriteria searchCriteria) {
        Set<Principal> searchResult = PrincipalViewHelper.getSearchResult(searchCriteria.getSearchIn(),
                searchCriteria.getSearchString(), searchCriteria.getProperties(), searchCriteria.getStoredOn(),
                searchCriteria.getProviders());
        searchResult = PrincipalViewHelper.removeJahiaAdministrators(searchResult);
        return searchResult;
    }

    public void addUser(UserProperties userProperties) {
        logger.info("Adding user");
        ServicesRegistry.getInstance().getJahiaUserManagerService().createUser(userProperties.getUsername(),
                userProperties.getPassword(), transformUserProperties(userProperties));
    }

    public void updateUser(UserProperties userProperties) {
        logger.info("Updating user");
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                userProperties.getUserKey());
        if (jahiaUser != null) {
            jahiaUser.setProperty("j:firstName", userProperties.getFirstName());
            jahiaUser.setProperty("j:lastName", userProperties.getLastName());
            jahiaUser.setProperty("j:email", userProperties.getEmail());
            jahiaUser.setProperty("j:organization", userProperties.getOrganization());
            jahiaUser.setProperty("emailNotificationsDisabled", userProperties.getEmailNotifications().toString());
            jahiaUser.setProperty("j:accountLocked", userProperties.getAccountLocked().toString());
            jahiaUser.setProperty("preferredLanguage", userProperties.getPreferredLanguage().toString());
            if (StringUtils.isNotBlank(userProperties.getPassword())) {
                jahiaUser.setPassword(userProperties.getPassword());
            }
        }
    }

    public UserProperties populateUser(String[] selectedUsers) {
        assert selectedUsers.length == 1;
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                selectedUsers[0]);
        UserProperties userProperties = new UserProperties();
        userProperties.setFirstName(jahiaUser.getProperty("j:firstName"));
        userProperties.setLastName(jahiaUser.getProperty("j:lastName"));
        userProperties.setUsername(jahiaUser.getUsername());
        userProperties.setUserKey(jahiaUser.getUserKey());
        userProperties.setEmail(jahiaUser.getProperty("j:email"));
        userProperties.setOrganization(jahiaUser.getProperty("j:organization"));
        userProperties.setEmailNotifications(Boolean.valueOf(jahiaUser.getProperty("emailNotificationsDisabled")));
        userProperties.setPreferredLanguage(UserPreferencesHelper.getPreferredLocale(jahiaUser));
        userProperties.setAccountLocked(Boolean.valueOf(jahiaUser.getProperty("j:accountLocked")));
        userProperties.setDisplayName(PrincipalViewHelper.getDisplayName(jahiaUser, LocaleContextHolder.getLocale()));
        userProperties.setLocalPath(jahiaUser.getLocalPath());
        final JahiaGroupManagerService managerService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        final List<String> userMembership = managerService.getUserMembership(jahiaUser);
        final List<JahiaGroup> groups = new ArrayList<JahiaGroup>(userMembership.size());
        for (String groupName : userMembership) {
            final JahiaGroup group = managerService.lookupGroup(groupName);
            groups.add(group);
        }
        userProperties.setGroups(groups);
        return userProperties;
    }

    public List<? extends JahiaUserManagerProvider> getProvidersList() {
        return ServicesRegistry.getInstance().getJahiaUserManagerService().getProviderList();
    }

    public UserProperties initUser() {
        return new UserProperties();
    }

    public void removeUser(UserProperties userProperties) {
        JahiaUser jahiaUser = ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(
                userProperties.getUserKey());
        ServicesRegistry.getInstance().getJahiaUserManagerService().deleteUser(jahiaUser);
    }

    public CsvFile initCSVFile() {
        CsvFile csvFile = new CsvFile();
        csvFile.setCsvSeparator(",");
        return csvFile;
    }

    public void bulkAddUser(CsvFile csvFile) {
        logger.info("Bulk adding users");
        long timer = 0;
        try {
            timer = System.currentTimeMillis();
            CSVReader csvReader = new CSVReader(new InputStreamReader(csvFile.getCsvFile().getInputStream(), "UTF-8"),
                    csvFile.getCsvSeparator().charAt(0));
            // the first line contains the column names;
            String[] headerElements = csvReader.readNext();
            List<String> headerElementList = Arrays.asList(headerElements);
            int userNamePos = headerElementList.indexOf("j:nodename");
            int passwordPos = headerElementList.indexOf("j:password");
            if ((userNamePos < 0) || (passwordPos < 0)) {
                logger.error("Couldn't find user name or password column in CSV file, aborting batch creation !");
                return;
            }
            String[] lineElements = null;
            JahiaPasswordPolicyService pwdPolicyService = ServicesRegistry.getInstance().getJahiaPasswordPolicyService();
            JahiaUserManagerService userService = ServicesRegistry.getInstance().getJahiaUserManagerService();

            while ((lineElements = csvReader.readNext()) != null) {
                List<String> lineElementList = Arrays.asList(lineElements);
                Properties properties = buildProperties(headerElementList, lineElementList);
                String userName = lineElementList.get(userNamePos);
                String password = lineElementList.get(passwordPos);
                if (userService.isUsernameSyntaxCorrect(userName)) {
                    PolicyEnforcementResult evalResult = pwdPolicyService.enforcePolicyOnUserCreate(userName, password);
                    if (evalResult.isSuccess()) {
                        JahiaUser jahiaUser = userService.createUser(userName, password, properties);
                        if (jahiaUser != null) {
                            logger.info("Successfully created user {}", userName);
                        } else {
                            logger.warn("Error creating user {}", userName);
                        }
                    } else {
                        StringBuilder result = new StringBuilder();
                        for (String msg : evalResult.getTextMessages()) {
                            result.append(msg).append("\n");
                        }
                        logger.warn("Skipping user {}. Following password policy rules are violated\n{}", userName,
                                result.toString());
                    }
                } else {
                    logger.warn("Username {} is not valid. Skipping user.", userName);
                }
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }


        logger.info("Batch user create took " + (System.currentTimeMillis() - timer) + " ms");
    }

    private Properties transformUserProperties(UserProperties userProperties) {
        Properties properties = new Properties();
        properties.put("j:firstName", userProperties.getFirstName());
        properties.put("j:lastName", userProperties.getLastName());
        properties.put("j:email", userProperties.getEmail());
        properties.put("j:organization", userProperties.getOrganization());
        properties.put("preferredLanguage", userProperties.getPreferredLanguage().toString());
        properties.put("j:accountLocked", userProperties.getAccountLocked().toString());
        properties.put("emailNotificationsDisabled", userProperties.getEmailNotifications().toString());
        return properties;
    }

    private Properties buildProperties(List<String> headerElementList, List<String> lineElementList) {
        Properties result = new Properties();
        for (int i = 0; i < headerElementList.size(); i++) {
            String currentHeader = headerElementList.get(i);
            String currentValue = lineElementList.get(i);
            if (!"j:nodename".equals(currentHeader) && !"j:password".equals(currentHeader)) {
                result.setProperty(currentHeader.trim(), currentValue);
            }
        }
        return result;
    }
}
