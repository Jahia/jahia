def test = project.properties['test']

def p = ~/.*\.jmx/

def tests;
tests = test.tokenize(",");

new File(project.properties['jahia.test.jmeter.path']+"/bin/testPlan").eachDir {
    moduleDir ->
    new File(moduleDir,"scripts/jmeter").eachDir {
        d ->
        if ((test != null && tests.contains(d.getName())) || test == null) {
            new File(d.getAbsolutePath()).eachFileMatch(p) {
                f ->
                final def file = new File(d.getAbsolutePath() + "/test.properties");
                def jmeterExe;
                def jmeterPath;

                jmeterPath = project.properties['jahia.test.jmeter.path'];

                def params = "";

                project.properties.each() { key, value -> params += " -J${key}=${value}"}

                if (System.properties['os.name'].toLowerCase().contains('windows')) {
                    jmeterPath = project.properties['jahia.test.jmeter.path'] + "\\bin\\jmeter.bat -n -t "				
                } else {
                    jmeterPath = project.properties['jahia.test.jmeter.path'] + "/bin/jmeter.sh -n -t "
                }
                if (file.exists()) 
				    jmeterExe = jmeterPath + f + " " + evaluate(file) 
			    else
                    jmeterExe = jmeterPath + f + params
					
                if (System.properties['os.name'].toLowerCase().contains('windows')) {
				    // add this to deactivate the pause in jmeter.bat as otherwise process would never end on error
				    jmeterExe = ["cmd", "/c", "\"cd /d " + project.properties['jahia.test.jmeter.path'] + "\\bin && " + jmeterExe + "\"", "< nul"]
				}
				
                println "Executing test : " + jmeterExe
                def proc = jmeterExe.execute()
                proc.waitFor()
                // Obtain status and output
                println "return code: ${ proc.exitValue()}"
                println "stderr: ${proc.err.text}"
                println "stdout: ${proc.in.text}"
                println "End of test : " + jmeterExe
            }
        }
    }
}