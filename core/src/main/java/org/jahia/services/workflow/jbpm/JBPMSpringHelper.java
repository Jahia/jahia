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

package org.jahia.services.workflow.jbpm;

import org.hibernate.cfg.Configuration;
import org.jbpm.api.ProcessEngine;
import org.jbpm.internal.log.Log;
import org.jbpm.pvm.internal.cfg.ConfigurationImpl;
import org.jbpm.pvm.internal.env.EnvironmentImpl;
import org.jbpm.pvm.internal.env.PvmEnvironment;
import org.jbpm.pvm.internal.env.SpringContext;
import org.jbpm.pvm.internal.processengine.ProcessEngineImpl;
import org.jbpm.pvm.internal.processengine.SpringHelper;
import org.jbpm.pvm.internal.wire.descriptor.ProvidedObjectDescriptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.orm.hibernate3.LocalSessionFactoryBean;

/**
 * Jahia specific jBPM Spring initialization helper to overcome the problem of multiple
 * Hibernate session factories, reported in https://jira.jboss.org/browse/JBPM-2942.
 * 
 * @author Sergiy Shyrkov
 * 
 */
public class JBPMSpringHelper extends SpringHelper {
	
	private class JBPMConfigurationImpl extends ConfigurationImpl {
		@Override
		public ProcessEngine buildProcessEngine() {
			if (!isConfigured) {
				setResource(DEFAULT_CONFIG_RESOURCENAME);
			}
			return JBPMSpringProcessEngine.create(this, sessionFactoryName);
		}
	}

	/**
	 * this environment factory will see only the singleton beans.
	 *
	 * The created {@link SpringEnvironment}s will see the prototype beans and it
	 * will cache them.
	 *
	 * @author Andries Inze
	 */
	public static class JBPMSpringProcessEngine extends ProcessEngineImpl {

	  private static final Log log = Log.getLog(JBPMSpringProcessEngine.class.getName());

	  private static final long serialVersionUID = 1L;

	  public static ProcessEngine create(ConfigurationImpl configuration, String sessionFactoryName) {
		  JBPMSpringProcessEngine springProcessEngine = null;

	    ApplicationContext applicationContext = null;
	    if (configuration.isInstantiatedFromSpring()) {
	      applicationContext = (ApplicationContext) configuration.getApplicationContext();

	      springProcessEngine = new JBPMSpringProcessEngine();
	      springProcessEngine.applicationContext = applicationContext;
	      springProcessEngine.initializeProcessEngine(configuration);

	      // prefixing session factory bean name with "&" to get the factory
	      LocalSessionFactoryBean localSessionFactoryBean = sessionFactoryName != null ? (LocalSessionFactoryBean) springProcessEngine
				        .get("&" + sessionFactoryName) : springProcessEngine
				        .get(LocalSessionFactoryBean.class);
	      Configuration hibernateConfiguration = localSessionFactoryBean.getConfiguration();
	      springProcessEngine.processEngineWireContext
	          .getWireDefinition()
	          .addDescriptor(new ProvidedObjectDescriptor(hibernateConfiguration, true));

	      springProcessEngine.checkDb(configuration);

	    } else {
	      String springCfg = (String) configuration.getProcessEngineWireContext().get("spring.cfg");
	      if (springCfg==null) {
	        springCfg = "applicationContext.xml";
	      }
	      applicationContext = new ClassPathXmlApplicationContext(springCfg);
	      springProcessEngine = (JBPMSpringProcessEngine) applicationContext.getBean("processEngine");
	    }

	    return springProcessEngine;
	  }

	  private ApplicationContext applicationContext;

	  @SuppressWarnings("unchecked")
	  @Override
	  public <T> T get(Class<T> type) {
	    T candidateComponent = super.get(type);

	    if (candidateComponent != null) {
	      return candidateComponent;
	    }

	    String[] names = applicationContext.getBeanNamesForType(type);

	    if (names.length >= 1) {

	      if (names.length > 1 && log.isWarnEnabled()) {
	        log.warn("Multiple beans for type " + type + " found. Returning the first result.");
	      }

	      return (T) applicationContext.getBean(names[0]);
	    }

	    return null;
	  }

	  @Override
	  public Object get(String key) {
	    if (applicationContext.containsBean(key)) {
	      return applicationContext.getBean(key);
	    }

	    return super.get(key);
	  }

	  public EnvironmentImpl openEnvironment() {
	    PvmEnvironment environment = new PvmEnvironment(this);

	    if (log.isTraceEnabled())
	      log.trace("opening jbpm-spring" + environment);

	    environment.setContext(new SpringContext(applicationContext));

	    installAuthenticatedUserId(environment);
	    installProcessEngineContext(environment);
	    installTransactionContext(environment);

	    return environment;
	  }
	}

	private String sessionFactoryName;
	
	@Override
	public ProcessEngine createProcessEngine() {
		processEngine = new JBPMConfigurationImpl().springInitiated(applicationContext)
		        .setResource(jbpmCfg).buildProcessEngine();

		return processEngine;
	}

	public void setSessionFactoryName(String sessionFactoryName) {
	    this.sessionFactoryName = sessionFactoryName;
    }

}
