/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.apache.jackrabbit.server.io;

import org.apache.log4j.Logger;
import org.jahia.api.Constants;

/**
 * <code>JahiaIOManager</code>...
 */
public class JahiaIOManager extends IOManagerImpl {

    private static Logger log = Logger.getLogger(JahiaIOManager.class);

    /**
     * Creates a new <code>DefaultIOManager</code> and populates the internal
     * list of <code>IOHandler</code>s by the defaults.
     *
     * @see #init()
     */
    public JahiaIOManager() {
        init();
    }

    /**
     * Creates a new <code>DefaultIOManager</code>. The if the flag is set to
     * false no <code>IOHandler</code>s are added to the internal list.
     *
     * @deprecated Use {@link IOManagerImpl} instead.
     */
    protected JahiaIOManager(boolean initDefaults) {
        if (initDefaults) {
           init();
        }
    }

    /**
     * Add the predefined <code>IOHandler</code>s to this manager. This includes
     * <ul>
     * <li>{@link ZipHandler}</li>
     * <li>{@link XmlHandler}</li>
     * <li>{@link DirListingExportHandler}</li>
     * <li>{@link DefaultHandler}.</li>
     * </ul>
     */
    protected void init() {
        addIOHandler(new DirListingExportHandler(this));
        addIOHandler(new ExtraContentHandler(this));
        addIOHandler(new SymLinkHandler(this));
        addIOHandler(new DefaultHandler(this, Constants.JAHIANT_FOLDER, Constants.JAHIANT_FILE, Constants.JAHIANT_RESOURCE));
    }
}