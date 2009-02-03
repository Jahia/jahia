#!/usr/bin/perl -w
################################################################################
use strict;

# JAHIA_SRC is the path to the sources where jahia is checkout
my $JAHIA_SRC = '/home/nightly/JAHIA-ANDROMEDA-BRANCH/jahia';

# DOC_SRC is the path to the sources where doc_templates are checkout
my $DOC_SRC = '/home/nightly/JAHIA-ANDROMEDA-BRANCH/doc_templates';

# GENERIC_SRC is the path to the sources where generic_templates are checkout
my $GENERIC_SRC = '/home/nightly/JAHIA-ANDROMEDA-BRANCH/generic_templates';

# CATALINA_HOME is the directory where tomcat is.
#my $CATALINA_HOME = '/home/nightly/JAHIA-ANDROMEDA-BRANCH/apache-tomcat-5.5.26';
my $CATALINA_HOME = '/home/nightly/JAHIA-ANDROMEDA-BRANCH/apache-tomcat-6.0.16';

# MAVEN_HOME is the path to maven
my $MAVEN_HOME = '/home/nightly/maven-2.0.6';

# JAVA_HOME is the path to java
my $JAVA_HOME = '/home/nightly/jdk1.5.0_15';

# ANT_HOME is the path to ant
my $ANT_HOME = '/home/nightly/apache-ant-1.7.0';

# TAR is the path to tar application
my $TAR = '/bin/tar';

# SVN is the path to svn application
my $SVN = '/usr/bin/svn';

# ARCHIVES is the path to archives
my $ARCHIVES = '/home/nightly/archives/JAHIA-ANDROMEDA-BRANCH';

my $TMP = '/tmp';

################################################################################
my $BUILD_NUMBER = -1;
my $RELEASE_NUMBER = '5.1';
my $PATCH_NUMBER = '0';

# check if user is 'pol'
if ($ENV{'USER'} ne 'nightly'){
    print "Only user nightly can run this script. Exiting.\n";
    exit;
}

# set environement values
$ENV{'JAVA_HOME'} = $JAVA_HOME;
$ENV{'MAVEN_HOME'} = $MAVEN_HOME;
$ENV{'ANT_HOME'} = $ANT_HOME;
$ENV{'MAVEN_OPTS'} = '-Xms64m -Xmx1024m';

# svn update
chdir $JAHIA_SRC;
&cmd($SVN,'cleanup');
&cmd($SVN,'update');
# remove old template jars resulting from conflicts
&cmd('rm','-f',"$JAHIA_SRC/war/src/main/webapp/WEB-INF/var/shared_templates/*.r*");


# svn doc and build the jar
chdir $DOC_SRC;
&cmd($SVN,'cleanup');
&cmd($SVN,'update');
&cmd('/bin/rm','-rf',"$DOC_SRC/build","$DOC_SRC/dist");
&cmd("$ANT_HOME/bin/ant");
&cmd('cp','-a',"$DOC_SRC/dist/doc_templates.jar","$JAHIA_SRC/war/src/main/webapp/WEB-INF/var/shared_templates/.");

# svn generic update and build the jar
chdir $GENERIC_SRC;
&cmd($SVN,'cleanup');
&cmd($SVN,'update');
&cmd('/bin/rm','-rf',"$GENERIC_SRC/build","$GENERIC_SRC/dist");
&cmd("$ANT_HOME/bin/ant");
&cmd('cp','-a',"$GENERIC_SRC/dist/core_templates.jar","$JAHIA_SRC/war/src/main/webapp/WEB-INF/var/shared_templates/.");


chdir $JAHIA_SRC;
$BUILD_NUMBER = `svn info | grep '^Last Changed Rev' | sed -e 's/Last Changed Rev: *//'`;
chomp $BUILD_NUMBER;

# clean and deploy
#&cmd('/bin/rm','-rf',"$CATALINA_HOME/webapps/jahia");
&cmd('/bin/rm','-rf',"$CATALINA_HOME/webapps/ROOT");
&cmd("$MAVEN_HOME/bin/mvn",'clean','install','jahia:deploy','-DskipTests=true','-Dtest=0');
#&cmd('cp','-a',"$JAHIA_SRC/war/target/jahia","$CATALINA_HOME/webapps/.");
#&cmd('cp','-af',"$JAHIA_SRC/war/target/jahia/META-INF/context.xml","$CATALINA_HOME/conf/Catalina/localhost/jahia.xml");
# rename jahia snapshot-> TODO

# copy to new directory and zip and remove tmp dir
my $BUILD_NAME =  'jahia-ANDROMEDA_r' . $BUILD_NUMBER;
&cmd('mkdir','-p',$TMP . '/' . $BUILD_NAME);
&cmd('cp','-af',$CATALINA_HOME,$TMP . '/' . $BUILD_NAME . '/tomcat');
&cmd('cp','-af',$JAHIA_SRC . '/core/src/scripts',$TMP . '/' . $BUILD_NAME . '/bin');

# change snapshot name in scripts
#&searchreplace($TMP . '/' . $BUILD_NAME . '/bin/jahia.bat','jahia-5.0-SNAPSHOT.jar',$SNAPSHOT_NAME);
#&searchreplace($TMP . '/' . $BUILD_NAME . '/bin/jahia.sh','jahia-5.0-SNAPSHOT.jar',$SNAPSHOT_NAME);

&cmd('cp','-a','/home/nightly/jcddl.txt',$TMP . '/' . $BUILD_NAME . '/jcddl.txt');
&cmd('cp','-a','/home/nightly/jssl.txt',$TMP . '/' . $BUILD_NAME . '/jssl.txt');

chdir $TMP;
my $MONTH = `/bin/date +%m`;
chomp $MONTH;
my $YEAR = `/bin/date +%Y`;
chomp $YEAR;
&cmd($TAR,'cvfz',$ARCHIVES . '/' . $BUILD_NAME . '.tar.gz',$BUILD_NAME);
&cmd('/bin/rm','-rf',$TMP . '/' . $BUILD_NAME);
################################################################################
sub searchreplace {
    my ($file,$search,$replace) = @_;
    my $filecontent;
    open F,$file or warn ("could not open $file: $!");
    while (<F>) {
        $filecontent .= $_;
    }
    close F;
    $filecontent =~ s/$search/$replace/g;
    open F,">$file" or warn ("could not write $file: $!");
    print F $filecontent;
    close F;
}

sub cmd {
    my @cmd = @_;
    foreach my $cmd (@cmd){
        print $cmd . ' ';
    }
    print "\n";
    system(@cmd);
}

