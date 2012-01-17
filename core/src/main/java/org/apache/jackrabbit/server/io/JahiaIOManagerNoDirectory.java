/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.server.io;

import org.slf4j.Logger;
import org.jahia.api.Constants;

/**
 * <code>JahiaIOManager</code>...
 */
public class JahiaIOManagerNoDirectory extends IOManagerImpl {

    private static Logger log = org.slf4j.LoggerFactory.getLogger(JahiaIOManagerNoDirectory.class);

    /**
     * Creates a new <code>DefaultIOManager</code> and populates the internal
     * list of <code>IOHandler</code>s by the defaults.
     *
     * @see #init()
     */
    public JahiaIOManagerNoDirectory() {
        init();
    }

    /**
     * Creates a new <code>DefaultIOManager</code>. The if the flag is set to
     * false no <code>IOHandler</code>s are added to the internal list.
     *
     * @deprecated Use {@link org.apache.jackrabbit.server.io.IOManagerImpl} instead.
     */
    protected JahiaIOManagerNoDirectory(boolean initDefaults) {
        if (initDefaults) {
           init();
        }
    }

    /**
     * Add the predefined <code>IOHandler</code>s to this manager. This includes
     * <ul>
     * <li>{@link org.apache.jackrabbit.server.io.ZipHandler}</li>
     * <li>{@link org.apache.jackrabbit.server.io.XmlHandler}</li>
     * <li>{@link org.apache.jackrabbit.server.io.DirListingExportHandler}</li>
     * <li>{@link org.apache.jackrabbit.server.io.DefaultHandler}.</li>
     * </ul>
     */
    protected void init() {
        addIOHandler(new ExtraContentHandler(this));
        addIOHandler(new SymLinkHandler(this));
        addIOHandler(new DefaultHandler(this, Constants.JAHIANT_FOLDER, Constants.JAHIANT_FILE, Constants.JAHIANT_RESOURCE));
    }
}