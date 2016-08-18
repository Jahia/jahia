package org.jahia.services.render.scripting.bundle;

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 * Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 * THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 * 1/GPL OR 2/JSEL
 *
 * 1/ GPL
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * "This program is free software; you can redistribute it and/or
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
 * describing the FLOSS exception, also available here:
 * http://www.jahia.com/license"
 *
 * 2/ JSEL - Commercial and Supported Versions of the program
 * ======================================================================================
 *
 * IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 * Alternatively, commercial and supported versions of the program - also known as
 * Enterprise Distributions - must be used in accordance with the terms and conditions
 * contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 * Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 * streamlining Enterprise digital projects across channels to truly control
 * time-to-market and TCO, project after project.
 * Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 * marketing teams to collaboratively and iteratively build cutting-edge
 * online business solutions.
 * These, in turn, are securely and easily deployed as modules and apps,
 * reusable across any digital projects, thanks to the Jahia Private App Store Software.
 * Each solution provided by Jahia stems from this overarching vision:
 * Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 * Founded in 2002 and headquartered in Geneva, Switzerland,
 * Jahia Solutions Group has its North American headquarters in Washington DC,
 * with offices in Chicago, Toronto and throughout Europe.
 * Jahia counts hundreds of global brands and governmental organizations
 * among its loyal customers, in more than 20 countries across the globe.
 *
 * For more information, please visit http://www.jahia.com
 */

import org.apache.commons.lang.StringUtils;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A replacement for the JDK's {@link ScriptEngineManager} that properly deals with {@link ScriptEngineFactory} implementations provided in OSGi modules. In particular,
 * bundles that provide a {@code javax.script.ScriptEngineFactory} file in their {@code META-INF/services} directory (Java Service Provider infrastructure) will be handled by
 * this ScriptEngineManager, wrapping the provided ScriptEngineFactory implementation in a {@link BundleScriptEngineFactory} so that the appropriate class loader will be used
 * to resolve views and associated resources. Additionally, provided script extensions will now be listened to and installed bundles will be scanned so that any installed
 * module that provides views that can be handled by the newly installed ScriptEngineFactory will be now able to answer to view requests.
 * <p>
 * Additionally, for each ScriptEngineFactory, this BundleScriptEngineManager will attempt to load an associated {@link BundleScriptEngineFactoryConfigurator} which should be
 * named {@code <fully qualified ScriptEngineFactory name>Configurator} so that additional set up / clean up can be performed when the bundle is started or stopped.
 */
public class BundleScriptEngineManager extends ScriptEngineManager {

    private static final String SCRIPT_ENGINE_FACTORY_CLASS_NAME = ScriptEngineFactory.class.getName();
    private static final String META_INF_SERVICES = "META-INF/services";
    private static Logger logger = LoggerFactory.getLogger(BundleScriptEngineManager.class);
    private Bindings globalScopeBindings;

    private final Map<String, ScriptEngineFactory> extensionsToScriptFactories = new ConcurrentHashMap<>(17);
    private final Map<String, ScriptEngineFactory> namesToScriptFactories = new ConcurrentHashMap<>(17);
    private final Map<String, ScriptEngineFactory> mimeTypesToScriptFactories = new ConcurrentHashMap<>(17);
    private final Map<Long, List<BundleScriptEngineFactory>> bundleIdsToScriptFactories = new ConcurrentHashMap<>(17);
    private final Map<String, BundleScriptEngineFactoryConfigurator> configurators = new ConcurrentHashMap<>(17);
    private final Map<ClassLoader, Map<String, ScriptEngine>> engineCache = new ConcurrentHashMap<>(17);

    private enum KeyType {extension, mimeType, name}

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final BundleScriptEngineManager INSTANCE = new BundleScriptEngineManager();

        private Holder() {
        }
    }

    public static BundleScriptEngineManager getInstance() {
        return Holder.INSTANCE;
    }

    private BundleScriptEngineManager() {
        this.globalScopeBindings = new SimpleBindings();
    }

    public Object get(String key) { // ramzy: getBindingByKey
        return globalScopeBindings.get(key);
    }

    public Bindings getBindings() {
        return globalScopeBindings;
    }

    public void put(String key, Object value) {
        globalScopeBindings.put(key, value);
    }

    public void setBindings(Bindings bindings) {
        this.globalScopeBindings = bindings;
    }

    private ScriptEngine getEngine(String key, Map<String, ScriptEngineFactory> factoriesForKeyType, KeyType keyType) {
        final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        Map<String, ScriptEngine> stringScriptEngineMap = engineCache.get(contextClassLoader);

        if (stringScriptEngineMap == null) {
            stringScriptEngineMap = new ConcurrentHashMap<>();
            engineCache.put(contextClassLoader, stringScriptEngineMap);
        }

        ScriptEngine scriptEngine = stringScriptEngineMap.get(key);
        if (scriptEngine == null) {

            final ScriptEngineFactory scriptEngineFactory = factoriesForKeyType.get(key);
            if (scriptEngineFactory != null) {
                // perform configuration of the factory if needed
                if (scriptEngineFactory instanceof BundleScriptEngineFactory) {
                    BundleScriptEngineFactory factory = (BundleScriptEngineFactory) scriptEngineFactory;
                    final BundleScriptEngineFactoryConfigurator configurator = getConfiguratorFor(factory.getWrappedFactoryClassName());
                    if (configurator != null) {
                        configurator.configurePreScriptEngineCreation(factory.getWrappedFactory());
                    }
                }

                scriptEngine = scriptEngineFactory.getScriptEngine();
                scriptEngine.setBindings(getBindings(), ScriptContext.GLOBAL_SCOPE);
            } else {
                switch (keyType) {
                    case extension:
                        scriptEngine = super.getEngineByExtension(key);
                        break;
                    case mimeType:
                        scriptEngine = super.getEngineByMimeType(key);
                        break;
                    case name:
                        scriptEngine = super.getEngineByName(key);
                        break;
                    default:
                        scriptEngine = null;
                }
            }

            if (scriptEngine != null) {
                stringScriptEngineMap.put(key, scriptEngine);
            }
        }
        return scriptEngine;
    }

    public ScriptEngine getEngineByExtension(String extension) {
        return getEngine(extension, extensionsToScriptFactories, KeyType.extension);
    }

    public ScriptEngine getEngineByMimeType(String mimeType) {
        return getEngine(mimeType, mimeTypesToScriptFactories, KeyType.mimeType);
    }

    public ScriptEngine getEngineByName(String shortName) {
        return getEngine(shortName, namesToScriptFactories, KeyType.name);
    }

    public List<ScriptEngineFactory> getEngineFactories() {
        List<ScriptEngineFactory> bundleScriptEngineFactories = new ArrayList<>();
        bundleScriptEngineFactories.addAll(extensionsToScriptFactories.values());
        bundleScriptEngineFactories.addAll(namesToScriptFactories.values());
        bundleScriptEngineFactories.addAll(mimeTypesToScriptFactories.values());
        return bundleScriptEngineFactories;
    }

    public void registerEngineExtension(String extension, ScriptEngineFactory factory) {
        extensionsToScriptFactories.put(extension, factory);
    }

    public void registerEngineMimeType(String type, ScriptEngineFactory factory) {
        mimeTypesToScriptFactories.put(type, factory);
    }

    public void registerEngineName(String name, ScriptEngineFactory factory) {
        namesToScriptFactories.put(name, factory);
    }

    private BundleScriptingContext getScriptingContext(Bundle bundle) throws IOException {
        List<String> factoryCandidates = findFactoryCandidates(bundle);
        if (factoryCandidates == null) {
            return null;
        }

        ClassLoader factoryLoader = null;
        final List<ScriptEngineFactory> factories = new ArrayList<>(factoryCandidates.size());
        for (String factoryCandidate : factoryCandidates) {
            final Class<? extends ScriptEngineFactory> factoryClass;
            try {
                factoryClass = bundle.loadClass(factoryCandidate).asSubclass(ScriptEngineFactory.class);
                factories.add(factoryClass.cast(factoryClass.newInstance()));
            } catch (ClassNotFoundException e) {
                logger.warn("ScriptEngineFactory {} was registered to be loaded but no associated class was found in bundle {}. Ignoring.", factoryCandidate, bundle);
                continue;
            } catch (InstantiationException | IllegalAccessException e) {
                logger.warn("Couldn't instantiate ScriptEngineFactory {}. Cause: {}. Ignoring.", factoryCandidate, e.getLocalizedMessage());
                continue;
            }

            if (factoryLoader == null) {
                // retrieve the class loader associated with the bundle
                try {
                    factoryLoader = factoryClass.getClassLoader();
                } catch (ClassCastException e) {
                    logger.warn("Registered ScriptEngineFactory {} doesn't implement ScriptEngineFactory in bundle {}. Ignoring.", factoryCandidate, bundle);
                    continue;
                }
            }

            // attempt to load a configurator for the current script engine factory
            final String configuratorName = factoryCandidate + "Configurator";
            try {
                final Class<?> configuratorClass = bundle.loadClass(configuratorName);
                final BundleScriptEngineFactoryConfigurator configurator = configuratorClass.asSubclass(BundleScriptEngineFactoryConfigurator.class).newInstance();
                configurators.put(factoryCandidate, configurator);
            } catch (ClassNotFoundException e) {
                // no configurator found for this script engine factory
            } catch (ClassCastException e) {
                logger.warn("Found class {} that doesn't implement BundleScriptEngineFactoryConfigurator in bundle {}. Ignoring.", configuratorName, bundle);
            } catch (InstantiationException | IllegalAccessException e) {
                logger.error("Couldn't instantiate configurator class {} in bundle {}. Cause: {}", new String[]{configuratorName, bundle.toString(), e.getLocalizedMessage()});
            }
        }

        // check if the bundle defined any view extension priorities
        // todo: add more validation and better specify how this functionality should work
        final Dictionary<String, String> headers = bundle.getHeaders();
        final String extensionsPriorities = headers.get("Jahia-Scripting-Extensions-Priorities");
        final Map<String, Integer> extensionsPrioritiesMap;
        if (extensionsPriorities != null) {
            final String[] extensionPriorityPairs = StringUtils.split(extensionsPriorities);
            extensionsPrioritiesMap = new HashMap<>(extensionPriorityPairs.length);
            for (String extensionPriorityPair : extensionPriorityPairs) {
                final String[] extensionPrioritySplit = StringUtils.split(extensionPriorityPair, '=');
                boolean valid = false;
                if (extensionPrioritySplit != null && extensionPrioritySplit.length == 2) {
                    try {
                        extensionsPrioritiesMap.put(extensionPrioritySplit[0], Integer.parseInt(extensionPrioritySplit[1]));
                        valid = true;
                    } catch (NumberFormatException e) {
                        valid = false;
                    }
                }

                if (!valid) {
                    logger.warn("Invalid extension - priority pair: {}. Format is extension=priority, priority should be an integer. Extension will be ignored.",
                            extensionPriorityPair);
                }
            }
        } else {
            extensionsPrioritiesMap = null;
        }

        return new BundleScriptingContext(factories, factoryLoader, extensionsPrioritiesMap);
    }

    private BundleScriptEngineFactoryConfigurator getConfiguratorFor(String factory) {
        return configurators.get(factory);
    }

    private List<String> findFactoryCandidates(Bundle bundle) throws IOException {
        List<String> factoryCandidates = new ArrayList<>();
        if ("system.bundle".equals(bundle.getSymbolicName())) {
            return null;
        }

        if (bundle.getState() == Bundle.ACTIVE) {
            Enumeration<URL> urls = bundle.findEntries(META_INF_SERVICES, SCRIPT_ENGINE_FACTORY_CLASS_NAME, false);
            if (urls == null) {
                return null;
            }
            while (urls.hasMoreElements()) {
                URL u = urls.nextElement();
                BufferedReader reader = new BufferedReader(new InputStreamReader(u.openStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    factoryCandidates.add(line.trim());
                }
            }
        }
        return factoryCandidates;
    }

    public void removeScriptEngineFactoriesIfNeeded(Bundle bundle) {
        final List<BundleScriptEngineFactory> factories = bundleIdsToScriptFactories.remove(bundle.getBundleId());
        if (factories != null) {
            for (BundleScriptEngineFactory bundleScriptEngineFactory : factories) {
                final List<String> extensions = bundleScriptEngineFactory.getExtensions();
                for (String extension : extensions) {
                    extensionsToScriptFactories.remove(extension);
                }

                final List<String> mimeTypes = bundleScriptEngineFactory.getMimeTypes();
                for (String mimeType : mimeTypes) {
                    mimeTypesToScriptFactories.remove(mimeType);
                }

                final List<String> names = bundleScriptEngineFactory.getNames();
                for (String name : names) {
                    namesToScriptFactories.remove(name);
                }

                BundleScriptResolver.getInstance().remove(bundleScriptEngineFactory, bundle);

                // check if we have a configurator to call
                final String factoryClassName = bundleScriptEngineFactory.getWrappedFactoryClassName();
                final BundleScriptEngineFactoryConfigurator configurator = getConfiguratorFor(factoryClassName);
                if (configurator != null) {
                    configurator.destroy(bundleScriptEngineFactory.getWrappedFactory());
                }
                configurators.remove(factoryClassName);
            }
        }
    }

    public void addScriptEngineFactoriesIfNeeded(Bundle bundle) {
        try {
            final BundleScriptingContext scriptingContext = getScriptingContext(bundle);
            if (scriptingContext != null) {
                final List<ScriptEngineFactory> engineFactories = scriptingContext.getEngineFactories();

                addFactories(bundle, scriptingContext, engineFactories);
            }
        } catch (IOException e) {
            logger.error("Error trying to get bundle " + bundle + " script engine factory information", e);
        }
    }

    private void addFactories(Bundle bundle, BundleScriptingContext scriptingContext, List<ScriptEngineFactory> engineFactories) {
        final List<BundleScriptEngineFactory> existingFactories = new ArrayList<>(engineFactories.size());

        for (ScriptEngineFactory factory : engineFactories) {
            final BundleScriptEngineFactory bundleScriptEngineFactory = new BundleScriptEngineFactory(factory, scriptingContext);

            final List<String> extensions = factory.getExtensions();
            for (String extension : extensions) {
                extensionsToScriptFactories.put(extension, bundleScriptEngineFactory);
            }

            final List<String> mimeTypes = factory.getMimeTypes();
            for (String mimeType : mimeTypes) {
                mimeTypesToScriptFactories.put(mimeType, bundleScriptEngineFactory);
            }

            final List<String> names = factory.getNames();
            for (String name : names) {
                namesToScriptFactories.put(name, bundleScriptEngineFactory);
            }

            existingFactories.add(bundleScriptEngineFactory);

            // check if we need to further configure the factory
            final BundleScriptEngineFactoryConfigurator configurator = getConfiguratorFor(bundleScriptEngineFactory.getWrappedFactoryClassName());
            if (configurator != null) {
                configurator.configure(factory, bundle, scriptingContext.getClassLoader());
            }

            BundleScriptResolver.getInstance().register(bundleScriptEngineFactory, bundle);
        }

        bundleIdsToScriptFactories.put(bundle.getBundleId(), existingFactories);
    }

    ScriptEngineFactory getFactoryForExtension(String extension) {
        return extensionsToScriptFactories.get(extension);
    }
}
