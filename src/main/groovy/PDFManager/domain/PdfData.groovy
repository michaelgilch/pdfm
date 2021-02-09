package PDFManager.domain

import grails.gorm.annotation.Entity
import groovy.transform.EqualsAndHashCode
import javax.persistence.Id
import org.grails.datastore.gorm.GormEntity

@Entity
@EqualsAndHashCode
class PdfData implements GormEntity<PdfData>, Serializable {
    @Id
    int id

    String md5
    String fileName
    String descriptiveName
}
