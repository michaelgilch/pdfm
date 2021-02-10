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
    String author
    String publisher
    String year
    String category
    String type
    String tags

    static constraints = {
        descriptiveName nullable: true, blank: true
        author nullable: true, blank: true
        publisher nullable: true, blank: true
        year nullable: true, blank: true
        category nullable: true, blank: true
        type nullable: true, blank: true
        tags nullable: true, blank: true
    }
}
