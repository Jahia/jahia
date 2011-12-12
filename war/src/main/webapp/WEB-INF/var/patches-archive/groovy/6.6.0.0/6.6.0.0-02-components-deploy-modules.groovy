import java.util.*

import javax.jcr.*
import javax.jcr.query.*

import org.jahia.services.content.*
import org.jahia.registries.ServicesRegistry
import org.jahia.services.templates.JahiaTemplateManagerService

def sysout = out;

sysout << "Start analyzing sites and deploying modules\n"

        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
            public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaTemplateManagerService templateService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
                for (NodeIterator siteIterator = session
                        .getWorkspace()
                        .getQueryManager()
                        .createQuery("select * from [jnt:virtualsite] where ischildnode('/sites')",
                                Query.JCR_SQL2).execute().getNodes(); siteIterator.hasNext();) {
                    JCRNodeWrapper site = (JCRNodeWrapper) siteIterator.nextNode();
                    if ("systemsite".equals(site.getName())) {
                        // skip system site
                        continue;
                    }
                    
                    sysout << "\nProcessing site " + site.getName() + "\n";
                    
                    // searching for the homepage
                    List<String> searchedLocations = new LinkedList<String>();
                    searchedLocations.add("contents");
                    for (NodeIterator homeIterator = session
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery("select * from [jnt:page] where ischildnode('/sites/" + site.getName() + "/') and [j:isHomePage] = true",
                                    Query.JCR_SQL2).execute().getNodes(); homeIterator.hasNext();) {
                        searchedLocations.add(homeIterator.nextNode().getName());
                    }
                    
                    Set<String> toDeploy = new LinkedHashSet<String>();
                    toDeploy.add("default");
                    
                    for (NodeIterator moduleIterator  = session
                            .getWorkspace()
                            .getQueryManager()
                            .createQuery("select * from [jnt:virtualsite] where ischildnode('/templateSets')"+" and [j:siteType] = 'module' order by localname()",
                                    Query.JCR_SQL2).execute().getNodes(); moduleIterator.hasNext();) {
                        JCRNodeWrapper module = (JCRNodeWrapper) moduleIterator.nextNode();
                        if (!module.hasNode("components")) {
                            sysout << "  Module " + module.getName() +" does not provide any components -> skipping\n";
                            continue;
                        }
                        sysout << "  Checking components of module " + module.getName() + "\n";
                        for (NodeIterator coponentIterator  = session
                                .getWorkspace()
                                .getQueryManager()
                                .createQuery("select * from [jnt:component] where isdescendantnode('/templateSets/" + module.getName() + "/components')",
                                        Query.JCR_SQL2).execute().getNodes(); coponentIterator.hasNext();) {
                            JCRNodeWrapper component = (JCRNodeWrapper) coponentIterator.nextNode();
                            if (component.isNodeType("jmix:studioOnly")) {
                                // skip studio only component
                                continue;
                            }
                            //sysout << "    Checking component " + component.getName() + " for site " + site.getName() + "\n";
                            boolean found = false;
                            try {
                                for (String home : searchedLocations) {
                                    found = session
                                        .getWorkspace()
                                        .getQueryManager()
                                        .createQuery("select * from ["+component.getName()+"] where isdescendantnode('/sites/"+site.getName()+"/" + home + "')",
                                        Query.JCR_SQL2).execute().getNodes().hasNext();
                                    if (found) {
                                        //sysout << "    Site " + site.getName() + " contains component " + component.getName() + ". Module " + module.getName() + " will be deployed\n";
                                        toDeploy.add(module.getName());
                                        break;
                                    }
                                }
                            } catch (RepositoryException e) {
                                sysout << "    Error checking component " + component.getName() + " for site " + site.getName() + ". Cause: " + e.getMessage() + "\n";
                            }
                            if (found) {
                                break;
                            }
                        }
                        if (toDeploy.contains(module.getName())) {
                            sysout << "    + Site " + site.getName() + " contains components of module " + module.getName() + ". It will be deployed to the site.\n";
                        } else {
                            sysout << "    - Site " + site.getName() + " does not contain components of module " + module.getName() + ". It won't be deployed to the site.\n";
                        }
                    }
                    
                    if (toDeploy.isEmpty()) {
                        sysout << "  No new modules will need to be deployed on the site " + site.getName() + "\n";
                    } else {
                        sysout << "  Following modules will deployed to the site " + site.getName() + ": " + toDeploy + "\n";
                        for (String deployedModule : toDeploy) {
                            sysout << "    Deploying module " + deployedModule + " to site " + site.getName() + "\n";
                            templateService.deployModule("/templateSets/" + deployedModule, site.getPath(), session); 
                        }
                        session.save();
                    }
                }
                
                return null;
            }

        });

sysout << "\n... done analyzing sites and deploying modules.\n"