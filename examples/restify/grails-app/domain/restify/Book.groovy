package restify

import gorm.restapi.RestApi
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@RestApi(description = "Book test domain")
class Book {

    String title


    static constraints = {

    }

}
