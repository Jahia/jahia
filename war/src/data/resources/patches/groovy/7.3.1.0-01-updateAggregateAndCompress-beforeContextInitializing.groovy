import org.apache.commons.io.FileUtils
import org.jahia.settings.JahiaPropertiesUtils
import org.jahia.settings.StringUtils

File jahiaPropertiesFile = JahiaPropertiesUtils.detectJahiaPropertiesFile()
def fixApplierMessage1 = "The first property is \"aggregateAssets\" and the second property is \"compressAssetsDuringAggregation\"."
def fixApplierMessage3 = "You can find more information on Jahia Academy"

def importantMessage1 = "The compression of assets is not possible and will be disabled if you run your instance with a version of OpenJDK/JDK different of 1.8"
def importantMessage2 = "Therefore the value of the following property \"compressAssetsDuringAggregation\" will be ignored in those versions."

//script from 7.3.1.0

// We need to find the entry aggregateAndCompressAssets and if it exists duplicate it
// then replace it by the new keys aggregateAssets and compressAssetsDuringAggregation
if (jahiaPropertiesFile != null) {
    List<String> lines = FileUtils.readLines(jahiaPropertiesFile, JahiaPropertiesUtils.CHARSET)

    List<String> toBeAdded = new ArrayList<String>()
    int insertPosition = lines.size()
    for (String line : lines) {
        if (line.contains("aggregateAndCompressAssets")) {

            log.warn("Found the property \"aggregateAndCompressAssets\" in jahia.properties, the property will be replaced by two new properties.")
            log.warn(fixApplierMessage1)
            log.warn(importantMessage1)
            log.warn(importantMessage2)
            log.warn(fixApplierMessage3)

            insertPosition = lines.indexOf(line) + 1
            toBeAdded.add(StringUtils.replaceAllTokens(line, "aggregateAndCompressAssets", "compressAssetsDuringAggregation"))
            toBeAdded.add("# " + importantMessage2)
            toBeAdded.add("# " + importantMessage1)
            toBeAdded.add(StringUtils.replaceAllTokens(line, "aggregateAndCompressAssets", "aggregateAssets"))
        }
    }

    if (!toBeAdded.isEmpty()) {
        for (String lineToAdd : toBeAdded) {
            lines.add(insertPosition, lineToAdd)
        }
        lines.remove(insertPosition - 1)

        FileUtils.writeLines(jahiaPropertiesFile, JahiaPropertiesUtils.CHARSET.name(), lines)

        log.info("Property \"aggregateAndCompressAssets\" successfully replaced by properties \"aggregateAssets\" and \"compressAssetsDuringAggregation\" in jahia.properties")
    }
}
