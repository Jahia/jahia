To install Jahia, please do the following

1. You first need to install a Java 2 Software Development Kit (JDK or Java2SE SDK) 1.5 or better on your system. As Jahia needs to compile some jsp files, the Java Runtime Environment (or JRE) will not work, you will need to install the complete JDK (or Java2SE SDK).

You can find both versions for Linux and Windows on the SUN web site:
http://java.sun.com/j2se/

To check if Java is already installed on your system, type the following command line at the prompt of your system:
java -version

You should get a message indicating which Java version is installed on your system. Please note that the same message will be displayed if you only have a JRE installed. If an error is returned, you probably don't have a complete JDK installed.
If you have installed other versions of the Java Development Kit, Java Runtime Environment or other Java servers on your system, we recommend that you run a few checks before starting the installation in order to be sure that Jahia will run without problems.
Check that you have no TOMCAT_HOME and no CATALINA_HOME environment variable set.
To install a Java virtual machine on a Windows system (WindowsNT, Windows 2000 or Windows XP), you need to have administrator rights on your computer. Please contact your system administrator if you don't have sufficient permissions.
After the installation, you have to set the JAVA_HOME environment variable to the directory where your have installed the Java virtual machine. The default installation path of the SUN 1.5.0 Java virtual machine on Windows is "c:\j2sdk1.5.0_xx", where xx is the release number. Note that Jahia will check at run time that this variable is correctly set, and will stop if it is not the case.

Find more information on How to install and configure Jahia Enterprise Edition v6 on
http://extranet.jahia.com/jahia/Jahia/op/edit/documentation/pid/634


2. Launch Tomcat using the following command line from the Jahia folder:

- on Windows: /startJahia.bat

- on MacOS or Linux: ./startJahia.sh

3. Access from your browser http://localhost:8080/config

4. Configure your Jahia

5. When restarting you Jahia server after the installation, please point your browser to the following address  http://localhost:8080/


Troubleshooting and useful links in Jahia

- If the installation is not successful, try restarting you Jahia server and installing it another time
- Please avoid having spaces in the installation path of your Jahia server.
- The administration menu can be accessed from http://localhost:8080/administration
- The home page can also be accessed through http://localhost:8080/ or equivalently http://localhost:8080/cms


You can find many useful information on our community site http://www.jahia.org or contact us: sales@jahia.com