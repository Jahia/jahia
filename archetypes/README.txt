====
    
    This file is part of Jahia: An integrated WCM, DMS and Portal Solution
    Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
    
    As a special exception to the terms and conditions of version 2.0 of
    the GPL (or any later version), you may redistribute this Program in connection
    with Free/Libre and Open Source Software ("FLOSS") applications as described
    in Jahia's FLOSS exception. You should have received a copy of the text
    describing the FLOSS exception, and it is also available here:
    http://www.jahia.com/license"
    
    Commercial and Supported Versions of the program
    Alternatively, commercial and supported versions of the program may be used
    in accordance with the terms contained in a separate written agreement
    between you and Jahia Limited. If you are unsure which license is appropriate
    for your use, please contact the sales department at sales@jahia.com.
====

to create a new set of templates :
mvn archetype:generate -DarchetypeArtifactId=jahia-templates-archetype -DarchetypeGroupId=org.jahia.archetype -DarchetypeVersion=<jahia-version>

You have to exchange the <jahia-version> with the Jahia version you are using (e.g. 6.0-SNAPSHOT).

You will then be asked to enter the following parameters:

packageDisplayName : This is the display name of your template package (e.g. My custom Jahia templates)
providerUrl : This is the URL of the organization providing your templates: (e.g. http://www.myorganization.org)
jahiaPackageVersion : This is the Jahia version you are using (e.g. 6.0-SNAPSHOT)
resourceBundleName : This is the name of the automatically created resource bundle holding all the labels
    in your templates (e.g. MyCustomTemplates).
    This term has to conform to Java naming conventions and must not use spaces, but simply the CamelCase convention.
artifactId : This is your template project’s artifact name (e.g. my-custom-templates)
    and will also be the name of the project’s root folder.
    Don’t use spaces, but either write all together or delimit with hyphens.
version : This is the version of your template project (e.g. 0.1)
groupId : This will only be asked if you do not confirm the settings.
    The group ID must be org.jahia.templates, so that the Jahia’s maven deployment plugin will work correctly.

for more information see : 01-Configugre jahia for template development.odt

in order to include your template set in the build, add in war project, pom.xml file, <dependencies> section :

    </dependency>
    <dependency>
      <groupId>org.jahia.templates</groupId>
      <artifactId>TEMPLATE_PROJECT_NAME</artifactId>
      <version>${jahia.package.version}</version>
      <type>war</type>
    </dependency>

where TEMPLATE_PROJECT_NAME is your module name
