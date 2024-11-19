/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport.validation;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.mail.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created at 17 Jan$
 *
 * @author chooliyip
 **/
public class ValidationNotificationHandler {

    private static Logger logger = LoggerFactory.getLogger(ValidationNotificationHandler.class);

    /**
     * Send notification email to system administrator if import validation failed
     *
     * @param siteInfo
     * @param errors
     */
    public static void notifyAdministratorWhenValidationFailed(Map<Object, Object> siteInfo, String errors){

        String siteKey = (String) siteInfo.get("sitekey");
        String importFile = (String) siteInfo.get("importFileName");

        String subject = "Site import failed";

        String text = String.format("The import file %s for the site %s has validation errors. " +
                        "you could try to redo import after the errors are fixed. " +
                        "\n For more details about errors please check the error logs." +
                        "\n\n" +
                        "\n VALIDATION ERRORS:" +
                        "\n =====================================" +
                        "\n %s",
                importFile, siteKey, prettifyErrors(errors));

        MailService mailService = ServicesRegistry.getInstance().getMailService();
        if(mailService!=null && mailService.isEnabled()){
            //use system admin email server settings for out-going email
            mailService.sendMessage(null, null, null, null, subject, text);
        }else{
            logger.warn("Mail service is disabled now, notification for validation failure is ignored");
        }

    }

    /**
     * Add line breaks and spaces to error message
     *
     * @param errors
     * @return String
     */
    private static String prettifyErrors(final String errors){
        String spaces = "    ";
        return errors
                .replaceAll("\\{", "{\n"+spaces)
                .replaceAll("\\[", "[\n"+spaces+spaces)
                .replaceAll("\\]", "\n"+spaces+"]")
                .replaceAll("\\}", "\n}\n")
                .replaceAll(",", ",\n"+spaces+spaces);
    }

}
