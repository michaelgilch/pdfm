package PDFManager.utils

// allow references to logInfo() rather than LogHelper.logInfo()
import static LogHelper.*

/**
 * Singleton Class for managing application configuration properties.
 */
class PdfConfig {

    static File configFile

    static Map defaultConfig = [
            'databaseSource':'jdbc:h2:file:./db/pdfm',
            'filesystemRefreshTimer':'0',
            'fontFace':'DejaVu Sans',
            'fontSize':'12',
            'storageFolder':'/home/michael/pdfStore/',

    ]

    Properties propertiesToUse = null

    private static final INSTANCE = new PdfConfig()

    static getInstance() {
        return INSTANCE
    }

    private PdfConfig() {
    }

    def setConfigFile(File config) {
        configFile = config
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
        logInfo(configFile.getAbsolutePath())
        File directory = new File(configFile.getParent())
        if (!directory.exists()) {
            directory.mkdirs()
        }
        logInfo("Writing configuration to properties file: " + configFile.getAbsolutePath())
        props.store(configFile.newWriter(), null)
    }
}
