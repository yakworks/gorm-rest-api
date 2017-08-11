package gorm.restapi

import grails.converters.JSON
import grails.core.DefaultGrailsApplication
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.transaction.Transactional
import grails.util.GrailsNameUtils
import grails.validation.ConstrainedProperty
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping

import javax.annotation.Resource
//import javax.inject.Inject
//import org.springframework.bean.factory.Autowired

import static grails.util.GrailsClassUtils.getStaticPropertyValue

/**
 * Generates the domain part
 * should be merged with either Swaggydocs or Springfox as outlined
 * https://github.com/OAI/OpenAPI-Specification is the new openAPI that
 * Swagger moved to.
 * We are chasing this part https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaObject
 * Created by JBurnett on 6/19/17.
 */
//@CompileStatic
class JsonSchemaGenerator {

    @Resource
    HibernateMappingContext grailsDomainClassMappingContext
    @Resource
    DefaultGrailsApplication grailsApplication

    //good overview here
    //https://spacetelescope.github.io/understanding-json-schema/index.html
    //https://docs.spring.io/spring-data/rest/docs/current/reference/html/#metadata.json-schema
    //https://github.com/OAI/OpenAPI-Specification
    Map generate(String domainName) {
        //TODO figure out a more performant way to do these if possible
        DefaultGrailsDomainClass domClass = getDomainClass(domainName)
        Mapping mapping = getMapping(domainName)

        //Map cols = mapping.columns
        List<GrailsDomainClassProperty> props = resolvePersistentProperties(domClass)

        def map = ['$schema': "http://json-schema.org/schema#",
                   '$id': "http://localhost:8080/schema/${domainName}", //<-TODO come from application.yml?
                   title: domClass.name]
        if(mapping?.comment) map.description = mapping.comment
        if(domClass.clazz.isAnnotationPresent(RestApi.class)){
            map.description = domClass.clazz.getAnnotation(RestApi.class).description()
        }
        map.type = 'Object'
        map.required = []
        map.properties = [:]

        //ID
        def idProp = domClass.getIdentifier()
        map.properties[idProp.name] = [
            type : getJsonType(idProp.type).type,
            readOnly: true
        ]
        //version
        if(domClass.version) map.properties[domClass.version.name] = [type : 'integer', readOnly: true]

        for(def prop : props){
            ConstrainedProperty constraints = domClass.constrainedProperties.get(prop.name)
            //Map mappedBy = domClass.mappedBy
            if(!constraints.display) continue //skip if display is false
            def m = prop.getMetaPropertyValues()
            def jprop = [:]
            //jprop.title = prop.naturalName
            jprop.title = constraints.getMetaConstraintValue("title")?:prop.naturalName
            //title override
            //def metaConstraints = constraints.getMetaConstraintValue()metaConstraints
            //if(constraints.attributes?.title) jprop.title = constraints.attributes.title
            //if(constraints.getMetaConstraintValue("title"))
            String description = constraints.getMetaConstraintValue("description")
            if(description) jprop.description = description

            //Example
            String example = constraints.getMetaConstraintValue("example")
            if(example) jprop.example = example

            //type
            Map typeFormat = getJsonType(constraints.propertyType)
            jprop.type = typeFormat.type
            //format
            if(typeFormat.format) jprop.format = typeFormat.format
            //format override from constraints
            if(constraints.format) jprop.format = constraints.format
            if(constraints.email) jprop.format = 'email'
            //pattern TODO

            //defaults
            String defVal = getDefaultValue(mapping,prop.name)
            if(defVal != null) jprop.default = defVal //TODO convert to string?

            //required
            if(!constraints.isNullable() && constraints.editable) {
                //TODO update this so it can use config too
                if(prop.name in ['dateCreated','lastUpdated']){
                    jprop.readOnly = true
                }
                //if its nullable:false but has a default then its not required as it will get filled in.
                else if(jprop.default == null) {
                    jprop.required = true
                    (map.required as List).add(prop.name)
                }
            }
            //readOnly
            if(!constraints.editable) jprop.readOnly = true
            //default TODO
            //minLength
            if(constraints.getMaxSize()) jprop.maxLength = constraints.getMaxSize()
            //maxLength
            if(constraints.getMinSize()) jprop.minLength = constraints.getMinSize()

            if(constraints.getMin() != null) jprop.minimum = constraints.getMin()
            if(constraints.getMax() != null) jprop.maximum = constraints.getMax()
            if(constraints.getScale() != null) jprop.multipleOf = 1/Math.pow(10, constraints.getScale())

            //def typeFormat = getJsonType(constraints)
            //map.properties[prop.pathFromRoot] = typeFormat
            map.properties[prop.name] = jprop
        }

        return map
        //def fooMap = [foo:'bar']
        //render map as JSON
    }

    String getDefaultValue(Mapping mapping, String propName) {
        mapping.columns[propName]?.columns?.getAt(0)?.defaultValue
        //cols[prop.name]?.columns?.getAt(0)?.defaultValue
    }

    @CompileDynamic
    DefaultGrailsDomainClass getDomainClass(String domainName){
        grailsApplication.domainClasses.find { it.propertyName == domainName }
    }

    @CompileDynamic
    Mapping getMapping(String domainName){
        PersistentEntity pe = grailsDomainClassMappingContext.persistentEntities.find {
            GrailsNameUtils.getPropertyName(it.name) == domainName
        }
        return grailsDomainClassMappingContext.mappingFactory?.entityToMapping?.get(pe)
    }
    /* see http://epoberezkin.github.io/ajv/#formats */
    /* We are adding 'money' and 'date' as formats too
     * big decimal defaults to money
     */

    protected Map getJsonType(propertyType){
        Map typeFormat = [type: 'string']
        switch (propertyType){
            case [Boolean,Byte]:
                typeFormat.type = 'boolean'
                break
            case [Integer,Long,Short]:
                typeFormat.type = 'integer'
                break
            case [Double,Float,BigDecimal]:
                typeFormat.type = 'number'
                break
            case [BigDecimal]:
                typeFormat.type = 'number'
                typeFormat.format = 'money'
                break
            case [java.time.LocalDate]:
                typeFormat.type = 'string'
                //date. verified to be a date of the format YYYY-MM-DD
                typeFormat.format = 'date'
                break
            case [Date]:
                //date-time. verified to be a valid date and time in the format YYYY-MM-DDThh:mm:ssZ
                typeFormat.type = 'string'
                typeFormat.format = 'date-time'
                break
            case [String]:
                typeFormat.type = 'string'
                break
        }
        //TODO what about types like Byte etc..? or enums?
        return typeFormat
    }

    //copied from FormFieldsTagLib in the Fields plugin
    @CompileDynamic
    private List<GrailsDomainClassProperty> resolvePersistentProperties(GrailsDomainClass domainClass, Map attrs =[:]) {
        List<GrailsDomainClassProperty> properties

        if(attrs.order) {
            def orderBy = attrs.order?.tokenize(',')*.trim() ?: []
            properties = orderBy.collect { propertyName -> domainClass.getPersistentProperty(propertyName) }
        } else {
            properties = domainClass.persistentProperties as List
            def blacklist = attrs.except?.tokenize(',')*.trim() ?: []
            //blacklist << 'dateCreated' << 'lastUpdated'
            Map scaffoldProp = getStaticPropertyValue(domainClass.clazz, 'scaffold')
            if (scaffoldProp) {
                blacklist.addAll(scaffoldProp.exclude)
            }
            properties.removeAll { it.name in blacklist }
            properties.removeAll { !it.domainClass.constrainedProperties[it.name]?.display }
            properties.removeAll { it.derived }

            Collections.sort(properties, new org.grails.validation.DomainClassPropertyComparator(domainClass))
        }

        return properties
    }
}
