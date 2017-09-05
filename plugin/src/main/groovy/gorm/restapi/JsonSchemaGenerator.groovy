package gorm.restapi

import grails.core.DefaultGrailsApplication
import grails.core.GrailsDomainClass
import grails.core.GrailsDomainClassProperty
import grails.util.GrailsNameUtils
import grails.validation.ConstrainedProperty
import groovy.transform.CompileDynamic
import org.grails.core.DefaultGrailsDomainClass
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping

import javax.annotation.Resource
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
    Map generate(Class clazz) {
        return generate(GrailsNameUtils.getPropertyNameRepresentation(clazz.simpleName))
    }

    Map generate(String domainName) {
        DefaultGrailsDomainClass domClass = getDomainClass(domainName)
        Map schema = [:]
        schema['$schema'] = "http://json-schema.org/schema#"
        schema['$id'] = "http://localhost:8080/schema/${domClass.name}.json"
        schema["definitions"] = [:]
        schema.putAll generate(domClass, schema)
        return schema
    }

    Map generate(GrailsDomainClass domainClass, Map schema) {
        Map map = [:]
        //TODO figure out a more performant way to do these if
        Mapping mapping = getMapping(domainClass.name)

        //Map cols = mapping.columns
        map.title = domainClass.name //TODO Should come from application.yml !?

        if(mapping?.comment) map.description = mapping.comment
        if(domainClass.clazz.isAnnotationPresent(RestApi.class)){
            map.description = domainClass.clazz.getAnnotation(RestApi.class).description()
        }

        map.type = 'Object'
        map.required = []

        def (props, required) = getDomainProperties(domainClass, schema)

        map.properties = props
        map.required = required

        return map
    }

    private List getDomainProperties(DefaultGrailsDomainClass domClass, Map schema) {
        String domainName = GrailsNameUtils.getPropertyNameRepresentation(domClass.name)
        LinkedHashMap<String, String> map = [:]
        List required = []

        GrailsDomainClassProperty idProp = domClass.getIdentifier()

        //id
        map[idProp.name] = [type : getJsonType(idProp.type).type, readOnly: true]

        //version
        if(domClass.version) map[domClass.version.name] = [type : 'integer', readOnly: true]


        Mapping mapping = getMapping(domainName)
        List<GrailsDomainClassProperty> props = resolvePersistentProperties(domClass)

        for (GrailsDomainClassProperty prop : props) {
            ConstrainedProperty constraints = domClass.constrainedProperties.get(prop.name)
            //Map mappedBy = domClass.mappedBy
            if (!constraints.display) continue //skip if display is false


            if(prop.isAssociation()) {
                GrailsDomainClass referencedDomainClass = prop.referencedDomainClass
                if((prop.isManyToOne() || prop.isOneToOne() && !schema.definitions.containsKey(referencedDomainClass.name))) {
                    if(!referencedDomainClass.clazz.isAnnotationPresent(RestApi)) {
                        //treat as definition in same schema
                        schema.definitions[referencedDomainClass.name] = [:]
                        schema.definitions[referencedDomainClass.name] = generate(referencedDomainClass, schema)
                        map[prop.name] = ['$ref': "#/definitions/$prop.referencedDomainClass.name"]
                    } else {
                        //treat as a seperate file
                        map[prop.name] = ['$ref': "${prop.referencedDomainClass.name}.json"]
                    }

                    if (!constraints.isNullable() && constraints.editable) {
                        required.add(prop.name)
                    }
                }
            }
            else {

                Map jprop = [:]
                //jprop.title = prop.naturalName
                jprop.title = constraints.getMetaConstraintValue("title") ?: prop.naturalName
                //title override
                //def metaConstraints = constraints.getMetaConstraintValue()metaConstraints
                //if(constraints.attributes?.title) jprop.title = constraints.attributes.title
                //if(constraints.getMetaConstraintValue("title"))
                String description = constraints.getMetaConstraintValue("description")
                if (description) jprop.description = description

                //Example
                String example = constraints.getMetaConstraintValue("example")
                if (example) jprop.example = example

                //type
                Map typeFormat = getJsonType(constraints.propertyType)
                jprop.type = typeFormat.type
                //format
                if (typeFormat.format) jprop.format = typeFormat.format
                if (typeFormat.enum) jprop.enum = typeFormat.enum

                //format override from constraints
                if (constraints.format) jprop.format = constraints.format
                if (constraints.email) jprop.format = 'email'
                //pattern TODO

                //defaults
                String defVal = getDefaultValue(mapping, prop.name)
                if (defVal != null) jprop.default = defVal //TODO convert to string?

                //required
                if (!constraints.isNullable() && constraints.editable) {
                    //TODO update this so it can use config too
                    if (prop.name in ['dateCreated', 'lastUpdated']) {
                        jprop.readOnly = true
                    }
                    //if its nullable:false but has a default then its not required as it will get filled in.
                    else if (jprop.default == null) {
                        jprop.required = true
                        required.add(prop.name)
                    }
                }
                //readOnly
                if (!constraints.editable) jprop.readOnly = true
                //default TODO
                //minLength
                if (constraints.getMaxSize()) jprop.maxLength = constraints.getMaxSize()
                //maxLength
                if (constraints.getMinSize()) jprop.minLength = constraints.getMinSize()

                if (constraints.getMin() != null) jprop.minimum = constraints.getMin()
                if (constraints.getMax() != null) jprop.maximum = constraints.getMax()
                if (constraints.getScale() != null) jprop.multipleOf = 1 / Math.pow(10, constraints.getScale())

                map[prop.name] = jprop
            }

            //def typeFormat = getJsonType(constraints)
            //map.properties[prop.pathFromRoot] = typeFormat

        }

        return [map, required]
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

    protected Map getJsonType(Class propertyType){
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
            case {it.isEnum()}:
                typeFormat.type = 'string'
                typeFormat.enum = propertyType.values()*.name() as String[]

        }
        //TODO what about types like Byte etc..?
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
