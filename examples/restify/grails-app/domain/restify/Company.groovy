package restify

import gorm.restapi.RestApi
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@RestApi(description = "Company test domain, will be used for testing fields functionality")
class Company {

    String name
    String city
    int staffQuantity

    static List<String> getShowFields() { ["name"] }

    static constraints = {

    }

}
