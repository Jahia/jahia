def test = project.properties['test']
def gui = project.properties['gui']

def p = ~/.*\.jmx/

new File(project.properties['jahia.test.jmeter.path']+"/bin/testPlan").eachDir {
    moduleDir ->
    new File(moduleDir,"scripts/jmeter").eachDir {
        d ->
        if ((test != null && d.getName().equals(test)) || test == null) {
            new File(d.getAbsolutePath()).eachFileMatch(p) {
                f ->
                final def file = new File(d.getAbsolutePath() + "/test.properties");
                def jmeterExe;
                def jmeterPath;
                //if not windows
                jmeterPath = project.properties['jahia.test.jmeter.path'];

                def params = "";

                project.properties.each() { key, value -> params += " -J${key}=${value}"}

                def guiparam = "-n "
                if (gui != null) guiparam = ""

                if (jmeterPath.toString().count("/") != 0) {
                    jmeterPath = project.properties['jahia.test.jmeter.path'] + "/bin/jmeter.sh " + guiparam + " -t "
                } else {
                    // if windows
                    jmeterPath = project.properties['jahia.test.jmeter.path'] + "\\bin\\jmeter.bat " + guiparam  + " -t "
                }
                if (file.exists()) jmeterExe = jmeterPath + f + " " + evaluate(file) else
                    jmeterExe = jmeterPath + f + params
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