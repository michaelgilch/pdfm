package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static LogHelper.*
import PDFManager.domain.PdfData

import org.grails.orm.hibernate.HibernateDatastore

class Pdfm {

    HibernateDatastore hibernateDatastore

    Pdfm() {
        Map databaseConfig = [
                'dataSource.dbCreate':'update', // implies 'create'
                'dataSource.url':'jdbc:h2:file:./db/pdfm',
        ]
        hibernateDatastore = initializeAppDatabase(databaseConfig)

        addDbEntry()
    }

    def initializeAppDatabase(databaseConfig) {
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
