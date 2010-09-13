def test = project.properties['test']

def p = ~/.*\.jmx/

new File(project.properties['testsPath']).eachDir {
  d ->
  if ((test != null && d.getName().equals(test)) || test == null) {
    new File(d.getAbsolutePath()).eachFileMatch(p) {
      f ->
      final def file = new File(d.getAbsolutePath() + "/test.properties");
      def jmeterExe;
      def jmeterPath;
      //if not windows
      jmeterPath = project.properties['path'];
      if (jmeterPath.toString().count("/") != 0) {
        jmeterPath = project.properties['path'] + "/bin/jmeter.sh -n -t "
      } else {
        // if windows
        jmeterPath = project.properties['path'] + "\\bin\\jmeter.bat -n -t "
      }
      if (file.exists()) jmeterExe = jmeterPath + f + " " + evaluate(file) else
        jmeterExe = jmeterPath + f
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
