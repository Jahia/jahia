import groovy.sql.Sql
import javax.xml.parsers.SAXParserFactory
import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler

/**
 * Created by IntelliJ IDEA.
 * User: rincevent
 * Date: 25 mars 2009
 * Time: 17:53:27
 * To change this template use File | Settings | File Templates.
 */

def sql = Sql.newInstance("jdbc:mysql://" + project.properties['jahia.test.jmeter.dbip'] + ":3306/" + project.properties['jahia.test.jmeter.dbname'],
                          project.properties['jahia.test.jmeter.dbusername'], project.properties['jahia.test.jmeter.dbpassword'] == null ? "" : project.properties['jahia.test.jmeter.dbpassword'], "com.mysql.jdbc.Driver")

// delete table if previously created
try {
    if(project.properties['db-clean'] == 'true') {
	sql.execute("drop table samples") 
	sql.execute("drop table filenames")
}
    sql.execute(" SELECT * FROM samples LIMIT 0 , 30 ")

} catch (Exception e) {
// create table
    sql.execute('''create table samples (
    id integer not null primary key auto_increment,
    label varchar(255),
    ts datetime,
    time integer,
    latency integer,
    success boolean,
    result integer,
    nb_bytes bigint,
    number_active_threads_total integer,
    number_active_threads_group integer,
    thread_name varchar(255),
    data_type varchar(10)
)''')
    sql.execute('''create table filenames (
    filename varchar(255)
)''')
}

class RecordsHandler extends DefaultHandler {
    def sql

    RecordsHandler(Sql sql) {
        this.sql = sql
    }

    void startElement(String ns, String localName, String qName, Attributes atts) {
        switch (qName) {
            case 'httpSample':
                String labelName = atts.getValue("lb")
                if (!labelName.startsWith("http://")) {
                    def dataSets = sql.dataSet("samples")
                    final String rcValue = atts.getValue("rc")
                    dataSets.add(label: labelName, ts: new Date(atts.getValue("ts").toLong()),
                                 time: atts.getValue("t").toInteger(), latency: atts.getValue("lt").toInteger(),
                                 success: atts.getValue("s").toBoolean(), result: rcValue.isInteger()? rcValue.toInteger():0,
                                 nb_bytes: atts.getValue("by").toLong(),
                                 number_active_threads_total: atts.getValue("na").toInteger(),
                                 number_active_threads_group: atts.getValue("ng").toInteger(),
                                 thread_name: atts.getValue("tn"), data_type: atts.getValue("dt"))
                }; break

        }
    }
}


def handler = new RecordsHandler(sql)
def reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader()
reader.setContentHandler(handler)


def p = ~/.*\.jtl/
new File("target/jmeterResults").eachFileMatch(p) {
    f ->
    if (sql.rows("select * from filenames where filename like '" + f + "'").size() == 0) {
        println 'Parsing file ' + f
        try {
            reader.parse(new InputSource(new FileInputStream(f)))
        } catch (org.xml.sax.SAXParseException e) {
            println "error during parsing of file " + f + " maybe due to a bad ending of the jmeter test"
        }
        def filenames = sql.dataSet("filenames")
        filenames.add(filename: '' + f)
    } else {
        println 'file ' + f + ' already parsed'
    }
}
