package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*
import PDFManager.domain.PdfData
import PDFManager.utils.PdfConfig

import org.grails.orm.hibernate.HibernateDatastore

class Pdfm {

    static PdfConfig pdfConfig
    static Properties pdfConfigProperties
    static HibernateDatastore hibernateDatastore

    static {
        pdfConfig = new PdfConfig()
        logInfo("Fetching configuration...")
        pdfConfigProperties = pdfConfig.getConfigProperties()

        logInfo("Setting up database...")
        Map databaseConfig = [
                'dataSource.dbCreate':'update', // implies 'create'
                'dataSource.url':pdfConfigProperties.getProperty('databaseSource'),
        ]
        hibernateDatastore = initializeAppDatabase(databaseConfig)

    }

    Pdfm() {


        addDbEntry()

        //PdfConfig newConfig = new PdfConfig()
    }

    static def initializeAppDatabase(databaseConfig) {
        Package domainClassPackage = PdfData.getPackage()
        return new HibernateDatastore(databaseConfig, domainClassPackage)
    }

    def addDbEntry() {
        PdfData.withNewSession {
            PdfData.withTransaction {
                def newPDF = new PdfData(
                        fileName: 'test.pdf',
                        descriptiveName: 'Test PDF'
                )
                try {
                    newPDF.save(failOnError: true, flush: true)
                } catch (Exception e) {
                    println "error"
                }
            }
        }
    }

    String getGreeting() {
        logInfo('This is a test log')
        return 'Hello World from the Controller!'
    }
}
