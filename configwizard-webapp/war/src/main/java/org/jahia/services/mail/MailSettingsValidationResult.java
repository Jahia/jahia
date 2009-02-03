/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL),
 * Version 1.0 (the "License"), or (at your option) any later version; you may
 * not use this file except in compliance with the License. You should have
 * received a copy of the License along with this program; if not, you may obtain
 * a copy of the License at
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jahia.services.mail;

/**
 * Mail settings validation result.
 *
 * @author Sergiy Shyrkov
 */
public class MailSettingsValidationResult {
    public static final MailSettingsValidationResult SUCCESSFULL = new MailSettingsValidationResult();

    private Object[] args;

    private String messageKey;

    private String property;

    private boolean success = true;


    private MailSettingsValidationResult() {
        super();
    }

    public MailSettingsValidationResult(String property, String messageKey) {
        this(property, messageKey, null);
    }

    public MailSettingsValidationResult(String property,
            String messageKey, Object[] args) {
        super();
        this.success = false;
        this.property = property;
        this.messageKey = messageKey;
        this.args = args;
    }

    /**
     * Returns the args.
     *
     * @return the args
     */
    public Object[] getArgs() {
        return args;
    }

    /**
     * Returns the messageKey.
     *
     * @return the messageKey
     */
    public String getMessageKey() {
        return messageKey;
    }

    /**
     * Returns the property.
     *
     * @return the property
     */
    public String getProperty() {
        return property;
    }

    /**
     * Returns the success.
     *
     * @return the success
     */
    public boolean isSuccess() {
        return success;
    }

}
