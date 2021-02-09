package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*
import PDFManager.domain.PdfData
import PDFManager.utils.PdfConfig

import org.grails.orm.hibernate.HibernateDatastore

class Pdfm {

    static Properties pdfConfig
    static HibernateDatastore hibernateDatastore

    static {
        logInfo("Fetching configuration...")
        PdfConfig pc = new PdfConfig()
        pdfConfig = pc.getConfigProperties()

        hibernateDatastore = initializeAppDatabase()

        createFileStorageDirIfNeeded()

        runPdfmController()
    }

    static def runPdfmController() {
        logInfo("pdfm Controller Started.")
    }

    static def createFileStorageDirIfNeeded() {
        // check for pdf storage folder and create if it does not already exist
        File storageDir = new File(pdfConfig.getProperty('storageFolder'))
        if (!storageDir.exists()) {
            logInfo("Creating storage directory")
            storageDir.mkdirs()
        }
    }

    static def initializeAppDatabase() {

        logInfo("Initializing the database...")
        Map databaseConfig = [
                'dataSource.dbCreate':'update', // implies 'create'
                'dataSource.url':pdfConfig.getProperty('databaseSource'),
        ]

        Package domainClassPackage = PdfData.getPackage()
        return new HibernateDatastore(databaseConfig, domainClassPackage)
    }
}
