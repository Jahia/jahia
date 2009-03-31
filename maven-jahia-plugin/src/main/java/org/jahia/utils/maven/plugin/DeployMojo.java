/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.resolver.ArtifactCollector;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilder;
import org.apache.maven.shared.dependency.tree.DependencyTreeBuilderException;
import org.apache.maven.shared.dependency.tree.DependencyTree;
import org.apache.maven.model.Dependency;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.utils.maven.plugin.deployers.ServerDeploymentFactory;

import java.util.*;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;
import java.io.*;
import java.net.ConnectException;

import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.Bootstrap;
import com.sun.jdi.ReferenceType;

/**
 * Created by IntelliJ IDEA.
 * User: Serge Huber
 * Date: 26 d�c. 2007
 * Time: 16:43:38
 * To change this template use File | Settings | File Templates.
 * @goal deploy
 * @requiresDependencyResolution runtime
 */
public class DeployMojo extends AbstractManagementMojo {

    /**
     * The dependency tree builder to use.
     *
     * @component
     * @required
     * @readonly
     */
    private DependencyTreeBuilder dependencyTreeBuilder;

    /**
     * The artifact metadata source to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactMetadataSource artifactMetadataSource;

    /**
     * The artifact collector to use.
     *
     * @component
     * @required
     * @readonly
     */
    private ArtifactCollector artifactCollector;

    /**
     * @parameter expression="${jahia.debug.address}" default-value="socket:hostname=localhost,port=8000"
     */
    private String address;


    public void doExecute() throws MojoExecutionException, MojoFailureException {
        try {
            if (targetServerDirectory != null) {
                deployProject();
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Error when deploying",e);
        }
    }

    private void deployProject() throws Exception {
        if (project.getPackaging().equals("ear")) {
            deployEarProject();
        } else if (project.getPackaging().equals("war")) {
            if (project.getGroupId().equals("org.jahia.server")) {
                deployWarProject();
            } else if (project.getGroupId().equals("org.jahia.templates")) {
                deployTemplateProject();
            }
        } else if (project.getPackaging().equals("sar") || project.getPackaging().equals("rar")) {
            deploySarRarProject();
        } else if (project.getPackaging().equals("jar")) {
            deployJarProject();
        } else if (project.getPackaging().equals("pom")) {
            deployPomProject();
        }
    }


    /**
     * Deploy all EAR dependencies ( WARs / SARs / RARs / shared resources ) to application server
     * @throws Exception
     */
    private void deployEarProject() throws Exception {
        getLog().info("Deploying application server specific files for " + targetServerType + " v" + targetServerVersion + " in directory " + targetServerDirectory);
        DependencyNode node = getRootDependencyNode();
        deployEarDependency(node);
    }

    /**
     * Copy output folder of WAR / SAR / RAR project to application server
     * @throws Exception
     */
    private void deployWarProject() throws Exception {
        File webappDir = getWarSarRarDeploymentDir(project.getArtifact());
        getLog().info("Update " + project.getPackaging() +
                " resources for " + targetServerType +
                " v" + targetServerVersion +
                " in directory " + webappDir);

        File origSource = new File(baseDir, "src/main/webapp");
        File source = new File(output, project.getBuild().getFinalName());
        try {
            int cnt = updateFiles(source, origSource, webappDir, new HashSet());
            getLog().info("Copied "+cnt+" files.");
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    /**
     * Copy output folder of WAR / SAR / RAR project to application server
     * @throws Exception
     */
    private void deploySarRarProject() throws Exception {
        File webappDir = getWarSarRarDeploymentDir(project.getArtifact());
        getLog().info("Update " + project.getPackaging() +
                " resources for " + targetServerType +
                " v" + targetServerVersion +
                " in directory " + webappDir);

        File source = new File(output, project.getBuild().getFinalName());
        try {
            int cnt = updateFiles(source, webappDir);
            getLog().info("Copied "+cnt+" files.");
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    /**
     * Copy template resources from output folder to the jsp/templates and WEB-INF/classes of jahia
     * @throws Exception
     */
    private void deployTemplateProject() throws Exception {
        File webappDir = getWebappDeploymentDir();
        File source = new File(output, project.getArtifactId()+"-"+project.getVersion());

        String prefix = "templates/";
        File target = new File(getWebappDeploymentDir(),prefix);
        if(!target.exists()) {
            prefix = "jsp/jahia/templates/";
        }

        File templateXml = new File(source, "WEB-INF/templates.xml");
        if (!templateXml.exists()) {
            getLog().info("No template.xml file, bypassing template deployement");
            return;
        }
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(templateXml)));
            String outputDir = null;
            while (outputDir == null ) {
                String line = reader.readLine();
                if (line.trim().startsWith("<root-folder>")) {
                    outputDir = line.substring(line.indexOf('>')+1, line.lastIndexOf('<'));
                }
            }

            target = new File(getWebappDeploymentDir(),prefix+outputDir);
            getLog().info("Updated template war resources for " + targetServerType + " v" + targetServerVersion + " in directory " + target);
            int cnt = updateFiles(source, target, Collections.singleton(new File(source,"WEB-INF")));
            cnt += updateFiles(new File(source, "WEB-INF/classes"), new File(webappDir,"WEB-INF/classes"));
            getLog().info("Copied "+cnt+" files.");
            FileUtils.copyFileToDirectory(templateXml, target);
            getLog().info("Updated template descriptor.");
        } catch (IOException e) {
            getLog().error(e);
        }
    }

    /**
     * Copy JAR file to jahia webapp, tries to hotswap classes
     * @throws Exception
     */
    private void deployJarProject() {
        Artifact artifact = project.getArtifact();
        try {
            artifactResolver.resolve(artifact, project.getRemoteArtifactRepositories(), localRepository);
            File libDir = new File(new File(getWebappDeploymentDir(), "WEB-INF"), "lib");
            getLog().info("Deploying jar file "+artifact.getFile().getName() + " to "+ libDir);
            File deployedJar = new File(libDir, artifact.getFile().getName());
            if (deployedJar.exists()) {
                hotSwap(deployedJar);
            }
            FileUtils.copyFileToDirectory(artifact.getFile(), libDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Look if the pom depends on a jahia-ear project and deploys it
     * @throws Exception
     */
    private void deployPomProject() {
        try {
            DependencyNode rootNode = getRootDependencyNode();
            List l = rootNode.getChildren();
            for (Iterator iterator = l.iterator(); iterator.hasNext();) {
                DependencyNode dependencyNode = (DependencyNode) iterator.next();
                Artifact artifact = dependencyNode.getArtifact();
                if (artifact.getGroupId().equals("org.jahia.server") && artifact.getArtifactId().equals("jahia-ear")) {
                    deployEarDependency(dependencyNode);
                }
                if (artifact.getGroupId().equals("org.jahia.server") && artifact.getArtifactId().equals("configwizard-ear")) {
                    deployEarDependency(dependencyNode);
                }
                if (artifact.getGroupId().equals("org.jahia.server") && artifact.getArtifactId().equals("configwizard-webapp")) {
                    deployWarRarSarDependency(dependencyNode);
                }
            }
        } catch (Exception e) {
            getLog().error(e);
        }
    }


    protected DependencyNode getRootDependencyNode() throws DependencyTreeBuilderException {
        DependencyTree dependencyTree =
                dependencyTreeBuilder.buildDependencyTree(project, localRepository, artifactFactory,
                        artifactMetadataSource, artifactCollector);
        DependencyNode rootNode = dependencyTree.getRootNode();
        return rootNode;
    }


    /**
     * Deploy all EAR dependencies ( WARs / SARs / RARs / shared resources ) to application server
     * @throws Exception
     */
    protected void deployEarDependency(DependencyNode dependencyNode) throws ArtifactResolutionException, ArtifactNotFoundException {
        List child = dependencyNode.getChildren();
        for (Iterator iterator1 = child.iterator(); iterator1.hasNext();) {
            DependencyNode node = (DependencyNode) iterator1.next();
            Artifact artifact = node.getArtifact();

            artifactResolver.resolve(artifact,  project.getRemoteArtifactRepositories(), localRepository);

            if ((artifact.getGroupId().equals("org.jahia.server") && (artifact.getArtifactId().equals("jahia-war") || artifact.getArtifactId().equals("config"))) ||
                    artifact.getType().equals("rar") ||
                    artifact.getType().equals("sar")) {
                deployWarRarSarDependency(node);
            } else if (Artifact.SCOPE_COMPILE.equals(artifact.getScope())) {
                getLog().info("Copy shared resource " + artifact.getFile().getName());
                try {
                    List<File> sharedLibs = new LinkedList();
                    sharedLibs.add(artifact.getFile());
                    ServerDeploymentFactory.getInstance()
                        .getImplementation(targetServerType + targetServerVersion)
                            .deploySharedLibraries(targetServerDirectory, targetServerVersion, sharedLibs);
                } catch (IOException e) {
                    getLog().error(e);
                }
            }
        }
    }

    /**
     * Deploy WAR / SAR / RAR artifact to application server
     * @param dependencyNode
     */
    protected void deployWarRarSarDependency(DependencyNode dependencyNode) {
        Artifact artifact = dependencyNode.getArtifact();
        File webappDir = getWarSarRarDeploymentDir(artifact);

        getLog().info("Update " + artifact.getType() +
                " resources for " + targetServerType +
                " v" + targetServerVersion +
                " in directory " + webappDir);
        try {
            ZipInputStream z = new ZipInputStream(
                    new FileInputStream(artifact.getFile()));
            ZipEntry entry;
            int cnt = 0;
            while ((entry = z.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File target = new File(webappDir, entry
                            .getName());
                    if (entry.getTime() > target.lastModified()) {

                        target.getParentFile().mkdirs();
                        FileOutputStream fileOutputStream = new FileOutputStream(
                                target);
                        IOUtils.copy(z, fileOutputStream);
                        fileOutputStream.close();
                        cnt++;
                    }
                }else{
                    //in the case of empty folders create anyway
                   (new File(webappDir, entry.getName())).mkdir();
                 }
            }
            z.close();
            getLog().info("Copied " + cnt + " files.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // *************** Hotswap

    private void hotSwap(File deployedJar) {
        int colonIndex = address.indexOf(':');
        String connectorName = address.substring(0,colonIndex);
        if (connectorName.equals("socket")) connectorName = "com.sun.jdi.SocketAttach";
        else if (connectorName.equals("shmem")) connectorName = "com.sun.jdi.SharedMemoryAttach";
        String argumentsString = address.substring(colonIndex+1);

        AttachingConnector connector = (AttachingConnector) findConnector(connectorName);
        Map arguments = connector.defaultArguments();

        StringTokenizer st = new StringTokenizer(argumentsString,",");
        while (st.hasMoreTokens()) {
            String pair = st.nextToken();
            int index = pair.indexOf('=');
            String name = pair.substring(0, index);
            String value = pair.substring(index +1);
            Connector.Argument argument = (Connector.Argument)arguments.get(name);
            if (argument != null) {
                argument.setValue(value);
            }
        }

        Map<String, Long> dates = new HashMap<String, Long>();
        try {
            ZipInputStream z = new ZipInputStream(new FileInputStream(deployedJar));
            ZipEntry entry;
            while ((entry = z.getNextEntry()) != null) {
                dates.put(entry.getName(), entry.getTime());
            }
            z.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        VirtualMachine vm = null;
        try {
            vm = connector.attach(arguments);
            getLog().info("Connected to " + vm.name()+" "+vm.version());

            Map<String, File> files = new HashMap<String,File>();

            parse(new File(output,"classes"), dates, "", files);
            getLog().debug("Classes : "+files.keySet());
            if (!files.isEmpty()) {
                reload(vm,files);
            }
        } catch (ConnectException e) {
            getLog().warn("Cannot hotswap classes : "+e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalConnectorArgumentsException e) {
            e.printStackTrace();
        } finally {
            if (vm != null) {
                vm.dispose();
            }
        }
    }

    private Connector findConnector(String name) {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }

    private void parse(File folder, Map<String,Long> dates, String base, Map<String,File> result) {
        File[] files = folder.listFiles();
        for (File file : files) {
            String filename = file.getName();
            if (file.isDirectory()) {
                parse(file, dates, base + filename + ".", result);
            } else if (filename.endsWith(".class")) {
                String name = base + filename.substring(0, filename.lastIndexOf("."));
                String classFileName = name.replace(".","/")+".class";

                if (dates.containsKey(classFileName)) {
                    long l = dates.get(classFileName);
                    if (file.lastModified() > l) {
                        result.put(name, file);
                        getLog().debug("Updated class : "+file);
                    }
                }
            }
        }
    }

    public void reload(VirtualMachine vm, Map<String,File> classFiles) {
        Map<ReferenceType,byte[]> map = new HashMap<ReferenceType,byte[]>();

        for (String className : classFiles.keySet()) {
            List classes = vm.classesByName(className);
            if (classes.size() != 1) {
                continue;
            }

            ReferenceType refType = (ReferenceType) classes.get(0);

            File f = classFiles.get(className);
            byte[] bytes = new byte[(int) f.length()];
            try {
                InputStream in = new FileInputStream(f);
                in.read(bytes);
                in.close();
            } catch (Exception e) {
                getLog().error("Error reading file " + f, e);
                continue;
            }
            map.put(refType, bytes);
        }


        try {
            vm.redefineClasses(map);
            getLog().info("Reloaded "+map.size() + " classes.");
        } catch (Exception e) {
            getLog().warn("Cannot reload classes : "+ e.getMessage());
        }

    }
}
