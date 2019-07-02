/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.settings.readonlymode;

import java.util.List;

/**
 * A supplier providing a list of {@link ReadOnlyModeStatusInfo}
 *
 * @author cmoitrier
 * @since 7.3.2.1
 */
public interface ReadOnlyModeStatusSupplier {

    /**
     * Returns a list of {@link ReadOnlyModeStatusInfo}
     */
    List<ReadOnlyModeStatusInfo> getStatuses();

}
