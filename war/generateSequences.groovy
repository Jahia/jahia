import org.hibernate.id.enhanced.SequenceStyleGenerator
import org.hibernate.dialect.Dialect
import org.hibernate.type.LongType

def daoFile = new File(project.basedir, 'src/main/webapp/WEB-INF/etc/spring/applicationcontext-dao.xml')
def beans = new XmlSlurper().parse(daoFile)


def p = ~/.*\.script/
new File(project.basedir,'src/main/webapp/WEB-INF/var/db/').eachFileMatch(p) {
    f ->
    try {
    println 'Generating Sequences for file ' + f
    def properties = new Properties()
    properties.load(new FileInputStream(f))
    println properties.getProperty("jahia.database.hibernate.dialect")
    def outputFile = new FileWriter(new File(project.basedir,'src/main/webapp/WEB-INF/var/db/'+properties.getProperty("jahia.database.schemascriptdir")+'/jahia-sequences-schema.sql'))
    SequenceStyleGenerator generator = new SequenceStyleGenerator();
    Properties hibProps = new Properties();
    def tablenames = beans.'**'.grep { it.@value != '' }.'@value'*.text()
    tablenames.each {it ->
        if (it.startsWith("jahia")) {
            int separatorPos = it.indexOf(".");
            String tableName = it.substring(0, separatorPos);
            final String dbSequenceName = tableName.replace("jahia", "seq");
            println dbSequenceName
            hibProps.setProperty(SequenceStyleGenerator.SEQUENCE_PARAM, dbSequenceName);
            hibProps.setProperty(SequenceStyleGenerator.INITIAL_PARAM, "1");
            hibProps.setProperty(SequenceStyleGenerator.OPT_PARAM, "pooled");
            hibProps.setProperty(SequenceStyleGenerator.INCREMENT_PARAM, "20");
            final Dialect dialect = Class.forName(properties.getProperty("jahia.database.hibernate.dialect")).newInstance();
            generator.configure(new LongType(), hibProps, dialect);
            final String[] sqls = generator.sqlCreateStrings(dialect);
            sqls.each {outputFile.write (it+";\n")}
        }
    }
    outputFile.close()
    } catch (e) {}
}
