package PDFManager.utils

// allow references to logInfo() rather than LogHelper.logInfo()
import static LogHelper.*

class PdfConfig {

    static File configFile = new File('conf/config.properties')

    static Map defaultConfig = [
            'databaseSource':'jdbc:h2:file:./db/pdfm',
            'storageFolder':'/home/michael/pdfStore/',
    ]

    Properties propertiesToUse = null

    PdfConfig() {
        Properties defaultProperties = loadDefaultConfigProperties()
        Properties userProperties = loadUserConfigProperties()
        propertiesToUse = new Properties()

        if (userProperties.isEmpty()) {
            propertiesToUse = defaultProperties
        } else {
            def userProperty = null
            defaultProperties.each { defaultProperty ->
                if (!userProperties.containsKey(defaultProperty.getKey())) {
                    logInfo("config file does not contain key: " + defaultProperty)
                    propertiesToUse << defaultProperty
                } else if (userProperties.containsKey(defaultProperty.getKey())) {
                    userProperty = userProperties.getProperty(defaultProperty.getKey())
                    if (userProperty.value != defaultProperty.getValue()) {
                        propertiesToUse << defaultProperty
                        propertiesToUse.setProperty(defaultProperty.getKey(), userProperty)
                    }
                } else {
                    propertiesToUse << defaultProperty
                }
            }
        }
        if (userProperties != propertiesToUse) {
            logInfo("Updating configuration with missing or invalid settings")
            saveConfigProperties(propertiesToUse)
        }
    }

    Properties getConfigProperties() {
        return propertiesToUse
    }

    private static loadDefaultConfigProperties() {
        Properties props = new Properties()
        defaultConfig.each {
            props.setProperty(it.key, it.value)
        }
        return props
    }

    private static loadUserConfigProperties() {
        Properties props = new Properties()
        if (configFile.exists()) {
            logInfo("Loading Configration from file: " + configFile.getAbsolutePath())
            props.load(configFile.newDataInputStream())
        }
        return props
    }

    private static saveConfigProperties(Properties props) {
        File directory = new File(configFile.getParent())
        if (!directory.exists()) {
            directory.mkdirs()
        }
        logInfo("Writing configuration to properties file: " + configFile.getAbsolutePath())
        props.store(configFile.newWriter(), null)
    }
}
