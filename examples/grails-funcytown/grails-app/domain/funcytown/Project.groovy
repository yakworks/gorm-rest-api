package funcytown

import grails.rest.Resource
import groovy.transform.CompileStatic
import grails.compiler.GrailsCompileStatic
import groovy.transform.EqualsAndHashCode
import gorm.restapi.RestApiController

import java.time.LocalDate

//@EqualsAndHashCode(includes = 'code,name')
@GrailsCompileStatic
@Resource(superClass = RestApiController)
class Project {

    static constraints = {
        code        description: "The project code",
                    nullable: false, maxSize:10
        name        description: "The project name",
                    nullable: false, maxSize:50
        inactive    description: "is project inactivated", nullable: false
        billable    description: "is project billable", nullable: false
        startDate	description: "Start date of project."
        endDate	    description: "End date of project."
        activateDate description: "Date time project is activated"
    }

    static mapping = {
        inactive defaultValue: "0"
        billable defaultValue: "0"
    }

    String code
    String name
    Boolean inactive = false
    Boolean billable = true
    LocalDate startDate
    LocalDate endDate
    Date activateDate
}
