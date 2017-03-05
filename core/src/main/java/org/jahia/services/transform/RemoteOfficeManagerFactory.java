/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.transform;

import org.artofsolving.jodconverter.office.ExternalOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeConnectionProtocol;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.springframework.beans.factory.config.AbstractFactoryBean;

/**
 * Factory bean for instantiating and configuring instance of the
 * {@link OfficeManager} that is started as a service on a local or remote
 * machine.
 * 
 * @author Sergiy Shyrkov
 */
public class RemoteOfficeManagerFactory extends AbstractFactoryBean<OfficeManager> {

    private ExternalOfficeManagerConfiguration cfg;

    /**
     * Initializes an instance of this class.
     */
    public RemoteOfficeManagerFactory() {
        super();
        cfg = new ExternalOfficeManagerConfiguration();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.config.AbstractFactoryBean#createInstance
     * ()
     */
    @Override
    protected OfficeManager createInstance() throws Exception {
        return cfg.buildOfficeManager();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.springframework.beans.factory.config.AbstractFactoryBean#getObjectType
     * ()
     */
    @Override
    public Class<? extends OfficeManager> getObjectType() {
        return OfficeManager.class;
    }

    public void setConnectOnStart(boolean connectOnStart) {
        cfg.setConnectOnStart(connectOnStart);
    }

    public void setHost(String host) {
        cfg.setHost(host);
    }

    public void setPipeName(String pipeName) throws NullPointerException {
        cfg.setPipeName(pipeName);
        cfg.setConnectionProtocol(OfficeConnectionProtocol.PIPE);
    }

    public void setPortNumber(int portNumber) {
        cfg.setPortNumber(portNumber);
    }

}