/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 * <p>
 * http://www.jahia.com
 * <p>
 * Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 * <p>
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 * <p>
 * 1/ GPL
 * ==================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * <p>
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ===================================================================================
 * <p>
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 * <p>
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 * <p>
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.osgi;

import org.ops4j.pax.swissbox.extender.BundleObserver;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
public class ExtensionObserverRegistry {
    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final ExtensionObserverRegistry INSTANCE = new ExtensionObserverRegistry();

        private Holder() {
        }
    }

    public static ExtensionObserverRegistry getInstance() {
        return Holder.INSTANCE;
    }

    private final Map<BundleURLScanner, BundleObserver<URL>> extensionObservers = new LinkedHashMap<>();

    public void put(BundleURLScanner scanner, BundleObserver<URL> observer) {
        extensionObservers.put(scanner, observer);
    }

    public void remove(BundleURLScanner scanner) {
        extensionObservers.remove(scanner);
    }

    public Iterable<? extends Map.Entry<BundleURLScanner, BundleObserver<URL>>> entrySet() {
        return extensionObservers.entrySet();
    }
}
