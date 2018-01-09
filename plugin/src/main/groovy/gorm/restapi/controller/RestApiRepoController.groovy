package gorm.restapi.controller

import gorm.tools.repository.GormRepoEntity
import grails.artefact.Artefact
import grails.core.GrailsApplication
import grails.util.GrailsNameUtils

/**
 * Credits: took rally.BaseDomainController with core concepts from grails RestfulConroller
 * Some of this is, especailly the cache part is lifted from the older grails2 restful-api plugin
 *
 * @author Joshua Burnett
 */
//see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
// we can get some good ideas from how that plugin does things
@SuppressWarnings(['CatchException', 'NoDef', 'ClosureAsLastMethodParameter', 'FactoryMethodName'])
@Artefact("Controller")
class RestApiRepoController<D extends GormRepoEntity> implements RestRepositoryApi<D> {
    static allowedMethods = [list  : ["GET", "POST"], create: "POST",
                             update: ["PUT", "PATCH"], delete: "DELETE"]

    static responseFormats = ['json']
    static namespace = 'api'

    Class<D> entityClass
    String entityName
    String entityClassName
    boolean readOnly


    //AppSetupService appSetupService
    GrailsApplication grailsApplication

    RestApiRepoController(Class<D> entityClass) {
        this(entityClass, false)
    }

    RestApiRepoController(Class<D> entityClass, boolean readOnly) {
        this.entityClass = entityClass
        this.readOnly = readOnly
        entityName = entityClass.simpleName
        entityClassName = GrailsNameUtils.getPropertyName(entityClass)
    }

    protected String getDomainInstanceName() {
        def suffix = grailsApplication.config?.grails?.scaffolding?.templates?.domainSuffix
        if (!suffix) {
            suffix = ''
        }
        def propName = GrailsNameUtils.getPropertyNameRepresentation(entityClass)
        "${propName}${suffix}"
    }


// ---------------------------------- ACTIONS ---------------------------------

    def index(){
        listGet()
    }

    /**
     * request type is handled in urlMapping
     *
     * returns the list of domain objects
     */
    def listPost() {
        respond query((request.JSON?:[:]) as Map, params)
    }

    /**
     * request type is handled in urlMapping
     *
     * returns the list of domain objects
     */
    def listGet() {
        respond query(params)
    }
}
