package PDFManager

// allow references to logInfo() rather than LogHelper.logInfo()
import static PDFManager.utils.LogHelper.*
import PDFManager.domain.PdfData
import PDFManager.utils.PdfConfig

import groovy.sql.Sql

import java.awt.Desktop

import java.security.MessageDigest

import org.grails.orm.hibernate.HibernateDatastore

class Pdfm {

    static def DATABASE_SCHEMA_VERSION_V100 = 1000
    static def DATABASE_SCHEMA_VERSION_V101 = 1001
    static def DATABASE_SCHEMA_VERSION_V102 = 1002
    static def CURRENT_DATABASE_SCHEMA_VERSION = DATABASE_SCHEMA_VERSION_V102

    Properties pdfConfig
    HibernateDatastore hibernateDatastore
    int fsRefreshInterval = 0

    static File debugPauseDirectoryScanningFile = new File('debugPauseDirectoryScanning')

    Pdfm() {
        logInfo("Using Default Config")
        runPdfmController("conf/config.properties")
    }

    Pdfm(String pathToConfig) {
        logInfo("Using Config: ${pathToConfig}")
        runPdfmController(pathToConfig)
    }

    def runPdfmController(String pathToConfig) {
        PdfConfig pc = PdfConfig.getInstance()
        pc.setConfigFile(new File(pathToConfig))
        pdfConfig = pc.getConfigProperties()

        hibernateDatastore = initializeAppDatabase()

        createFileStorageDirIfNeeded()

        logInfo("pdfm Controller Started.")

        fsRefreshInterval = pdfConfig.getProperty('filesystemRefreshTimer').toInteger()
        if (fsRefreshInterval == 0) {
            logInfo("Filesystem Refresh set to Manual.")
            checkFilesystemForChanges()
        } else {
            logInfo("Filesystem Refresh set to every ${fsRefreshInterval} minute(s).")
            Thread.start("directoryScanningThread") {
                startDirectoryScanningThread()
            }
        }
    }

    def getTagList() {
        def allTags = []
        PdfData.withNewSession {
            PdfData.withTransaction {
                PdfData.list().each { pdf ->
                    if (pdf.tags != "" && pdf.tags != null) {
                        def pdfTags = pdf.tags.split(',')
                        pdfTags.each {
                            if (!(it.trim() in allTags)) {
                                allTags += it.trim()
                            }
                        }
                    }
                }
            }
        }
        return allTags.sort()
    }

    def createFileStorageDirIfNeeded() {
        // check for pdf storage folder and create if it does not already exist
        File storageDir = new File(pdfConfig.getProperty('storageFolder'))
        if (!storageDir.exists()) {
            logInfo("Creating storage directory")
            storageDir.mkdirs()
        }
    }

    def initializeAppDatabase() {
        upgradeDatabaseIfNecessary()
        logInfo("Initializing the database...")
        Map databaseConfig = [
                'dataSource.dbCreate':'update', // implies 'create'
                'dataSource.url':pdfConfig.getProperty('databaseSource'),
        ]

        Package domainClassPackage = PdfData.getPackage()
        return new HibernateDatastore(databaseConfig, domainClassPackage)
    }

    def upgradeDatabaseIfNecessary() {
        logInfo('Start: Upgrade database if required.')
        def sql
        def databaseUrl = pdfConfig.getProperty('databaseSource')
        try {
            sql = Sql.newInstance(databaseUrl, "org.h2.Driver")
            def databaseSchemaVersion
            sql.execute("create table if not exists SCHEMA_VERSION(VALUE varchar(10))")
            def schemaVersionQuery = "select * from SCHEMA_VERSION"
            def rows = sql.rows(schemaVersionQuery)
            if (rows.size() == 1) {
                logInfo("Existing database version: ${rows[0].value}")
                databaseSchemaVersion = Integer.parseInt(rows[0].value)
            } else {
                // If this table does not have any rows, then it must be a new database that has not yet been setup by hibernate.
                // When hibernate sets it up, it will then be at the latest version
                databaseSchemaVersion = CURRENT_DATABASE_SCHEMA_VERSION
                logInfo("Initializing database schema version to ${databaseSchemaVersion}")
                sql.execute("insert into SCHEMA_VERSION values(?)", [databaseSchemaVersion])
            }

            def initialDatabaseSchemaVersion = databaseSchemaVersion

            if (databaseSchemaVersion == DATABASE_SCHEMA_VERSION_V100) {
                initialDatabaseSchemaVersion = DATABASE_SCHEMA_VERSION_V100
                logInfo("Upgrading database from ${databaseSchemaVersion}")
                sql.execute("alter table PDF_DATA add column AUTHOR varchar(64) default ''")
                sql.execute("alter table PDF_DATA add column PUBLISHER varchar(64) default ''")
                sql.execute("alter table PDF_DATA add column TAGS varchar(512) default ''")
                sql.execute("alter table PDF_DATA add column YEAR varchar(4) default ''")
                sql.execute("alter table PDF_DATA add column CATEGORY varchar(32) default ''")
                sql.execute("alter table PDF_DATA add column TYPE varchar(32) default ''")
                databaseSchemaVersion = DATABASE_SCHEMA_VERSION_V101
            }

            if (databaseSchemaVersion == DATABASE_SCHEMA_VERSION_V101) {
                initialDatabaseSchemaVersion = DATABASE_SCHEMA_VERSION_V101
                logInfo("Upgrading database from ${databaseSchemaVersion}")
                sql.execute("alter table PDF_DATA add column ISBN varchar(24) default ''")
                databaseSchemaVersion = DATABASE_SCHEMA_VERSION_V102
            }

            if (databaseSchemaVersion == DATABASE_SCHEMA_VERSION_V102) {
                // TODO upgrade to next version
            }

            if (initialDatabaseSchemaVersion != databaseSchemaVersion) {
                logInfo("Updating database schema version to ${databaseSchemaVersion}")
                sql.execute("update SCHEMA_VERSION set value=?", [databaseSchemaVersion])
            }

        } finally {
            if (sql) {
                sql.close()
            }
        }
    }

    def checkFilesystemForChanges() {
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
                                    descriptiveName: "",
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
        getTagList()
    }

    def generateMD5(File file) {
        def digest = MessageDigest.getInstance("MD5")
        file.eachByte(4096) {buffer, length ->
            digest.update(buffer, 0, length)
        }
        new BigInteger(1, digest.digest()).toString(16).padLeft(32, '0')
    }

    def startDirectoryScanningThread() {
        def keepRunning =  true
        logInfo("Start")
        while (keepRunning) {
            while (!debugPauseDirectoryScanningFile.exists()) {
                logInfo("Checking filesystem for Changes")
                checkFilesystemForChanges()
                Thread.sleep(1000 * 60 * fsRefreshInterval)
            }
        }
    }

    def getFilteredListOfPdfs(Map filter) {
        def pdfList = []
            PdfData.withNewSession {
                PdfData.list().each  { pdf ->
                    def potentialMatch = true
                    filter.tags.each {
                        if (pdf.tags == null || !(potentialMatch && pdf.tags.contains(it))) {
                            potentialMatch = false
                        }
                    }
                    if (potentialMatch) { pdfList << pdf }
                }
            }
        return pdfList
    }

    def getListOfPdfs() {
        def pdfList = []
        PdfData.withNewSession {
            pdfList = PdfData.list()
        }
        return pdfList
    }

    def openPdfById(int pdfId) {
        PdfData.withNewSession {
            def pdf = PdfData.findById(pdfId)
            logInfo("Opening: " + pdf.fileName)
            File pdfFile = new File(pdfConfig.getProperty('storageFolder') + pdf.fileName)
            Desktop dt = Desktop.getDesktop()
            dt.open(pdfFile)
        }
    }

    def savePdfAttributeChanges(pdf, attributes) {
        PdfData.withNewSession {
            PdfData.withTransaction {
                pdf.descriptiveName = attributes.descriptiveName
                pdf.type = attributes.type
                pdf.isbn = attributes.isbn
                pdf.category = attributes.category
                pdf.author = attributes.author
                pdf.publisher = attributes.publisher
                pdf.year = attributes.year
                pdf.tags = attributes.tags.toLowerCase()
                try {
                    pdf.save(failOnError: true, flush: true)
                } catch (Exception e) {
                    logError("Failed to save attribute changes for $pdf.fileName", e)
                }
            }
        }
    }

    PdfData getPdfObject(int pdfId) {
        PdfData.withNewSession {
            def pdf = PdfData.findById(pdfId)
            return pdf
        }
    }
}
