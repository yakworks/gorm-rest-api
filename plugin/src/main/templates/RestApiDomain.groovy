package $

import gorm.restapi.RestApi
import grails.compiler.GrailsCompileStatic

{ packageName }
//@EqualsAndHashCode(includes='fooName') //natural key
//@ToString(includes='fooName', includeNames=true, includePackage=false)
@GrailsCompileStatic
@RestApi(description = "The ${className} for fooinators TODO")
class $ {
    className
}

{

    String fooName

    Date dateCreated
    Date lastUpdated

    static constraints = {
        fooName description: 'TODO The foo name description',
                example: "foo name TODO",
                nullable: false, maxSize: 50
    }

    //static selectFields = ['id, name']

}
