def test = project.properties['test']

def p = ~/.*\.jmx/

new File(project.properties['testsPath']).eachDir {
    d ->
    if ((test != null && d.getName().equals(test)) || test == null) {
        new File(d.getAbsolutePath()).eachFileMatch(p) {
            f ->
            final def file = new File(d.getAbsolutePath() + "/test.properties");
            def jmeterExe;
            if (file.exists()) jmeterExe = project.properties['path'] + "/bin/jmeter -n -t " + f + " " + evaluate(file) else
                jmeterExe = project.properties['path'] + "/bin/jmeter -n -t " + f
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
