import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperExportManager
import groovy.sql.Sql
import net.sf.jasperreports.engine.query.JRJdbcQueryExecuterFactory

def sql = Sql.newInstance("jdbc:mysql://" + project.properties['jahia.test.jmeter.dbip'] + ":3306/" + project.properties['jahia.test.jmeter.dbname'],
                          project.properties['jahia.test.jmeter.dbusername'], project.properties['jahia.test.jmeter.dbpassword'] == null ? "" : project.properties['jahia.test.jmeter.dbpassword'], "com.mysql.jdbc.Driver")
def path = project.build.directory
final def dayVar = new Date().format('dd')
println "generating report for day = "+dayVar
def props = [sql:new JRJdbcQueryExecuterFactory(),day:new Integer(dayVar),SUBREPORT_DIR:path+"/jasper/"]
def print = JasperFillManager.fillReport(path+"/jasper/"+project.properties['jasperreport']+".jasper",props ,sql.getConnection())
JasperExportManager.exportReportToPdfFile(print,path+"/jasperreports/report"+dayVar+".pdf")
