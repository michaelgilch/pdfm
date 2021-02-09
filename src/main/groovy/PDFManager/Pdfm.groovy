package PDFManager

import java.security.MessageDigest

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
        checkFilesystemForChanges()
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

    static def checkFilesystemForChanges() {
        File pdfDirectory = new File(pdfConfig.getProperty('storageFolder'))
        pdfDirectory.eachFile { File absoluteFilename ->
            if (absoluteFilename.isFile()) {
                def md5 = generateMD5(absoluteFilename)
                PdfData.withNewSession {
                    PdfData.withTransaction {
                        def pdfDomainObj = PdfData.findAllByMd5(md5)
                        if (pdfDomainObj.isEmpty()) {
                            def newPdf = new PdfData(
                                    md5: md5,
                                    fileName: absoluteFilename.getName(),
                                    descriptiveName: "test",
                            )
                            try {
                                logInfo("Adding new PDF file: " + absoluteFilename)
                                newPdf.save(failOnError: true, flush: true)
                            } catch (Exception e) {
                                logError("Unable to add new PDF File: " + absoluteFilename, e)
                            }
                        } else if (pdfDomainObj[0].fileName != absoluteFilename.getName()) {
                            logInfo("Detected filename change from ${pdfDomainObj[0].fileName} to ${absoluteFilename.getName()}")
                            pdfDomainObj[0].fileName = absoluteFilename.getName()
                            try {
                                pdfDomainObj[0].save(failOnError: true, flush: true)
                            } catch (Exception e) {
                                logError("Unable to rename existing PDF data for " + absoluteFilename, e)
                            }
                        }
                    }
                }
            } else if (absoluteFilename.isDirectory()) {
                logError("Sub-Directories are not checked: " + absoluteFilename)
            }
        }
    }

    static def generateMD5(File file) {
        def digest = MessageDigest.getInstance("MD5")
        file.eachByte(4096) {buffer, length ->
            digest.update(buffer, 0, length)
        }
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

}
