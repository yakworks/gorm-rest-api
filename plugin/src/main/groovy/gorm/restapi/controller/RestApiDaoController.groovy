package gorm.restapi.controller

import gorm.tools.repository.api.RepositoryApi
import grails.artefact.Artefact

//import gorm.tools.Pager
import grails.converters.JSON
import grails.core.GrailsApplication
import gorm.tools.repository.errors.RepoExceptionSupport
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import groovy.transform.CompileStatic
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.context.MessageSource

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
class RestApiDaoController<D> {
    static allowedMethods = [list  : ["GET", "POST"], create: "POST",
                             update: ["PUT", "PATCH"], delete: "DELETE"]

    static responseFormats = ['json']
    static namespace = 'api'

    Class<D> resource
    String resourceName
    String resourceClassName
    boolean readOnly

    MessageSource messageSource
    RepoExceptionSupport repoExceptionSupport

    //AppSetupService appSetupService
    GrailsApplication grailsApplication

    RestApiDaoController(Class<D> resource) {
        this(resource, false)
    }

    RestApiDaoController(Class<D> resource, boolean readOnly) {
        this.resource = resource
        this.readOnly = readOnly
        resourceClassName = resource.simpleName
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    protected RepositoryApi<D> getDao() {
        return domainClass.repo
    }

    Class<D> getDomainClass() {
        resource
    }

//    @PostConstruct
//    protected void init(){
//        //println "init called and ga is ${grailsApplication?'initialized':'null'}"
//    }

    protected String getDomainInstanceName() {
        def suffix = grailsApplication.config?.grails?.scaffolding?.templates?.domainSuffix
        if (!suffix) {
            suffix = ''
        }
        def propName = GrailsNameUtils.getPropertyNameRepresentation(domainClass)
        "${propName}${suffix}"
    }

    /**
     * Lists all resources up to the given maximum
     *
     * @param max The maximum
     * @return A list of resources
     */
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        respond listAllResources(params), model: [("${resourceName}Count".toString()): countResources()]
    }

// ---------------------------------- ACTIONS ---------------------------------

    // GET /api/resource
    //
    def list() {

        log.trace "list invoked for ${params.pluralizedResourceName} - request_id=${request.request_id}"
        try {
            //cache headers
            def requestParams = params
            def logger = log

            def result

            if (request.method == "POST") {
                //request.body will possibly have the criteria
                result = listPost(request.body, requestParams)
            } else if (request.method == "GET") {
                result = listGet(requestParams)
            }

            respond result

        }
        catch (e) {
            logMessageError(e)
            renderErrorResponse(e)
        }
    }

    /**
     * returns the list of domain obects
     */
    protected def listPost(body, requestParams) {
        return getDaoService().list(body, requestParams)
    }

    /**
     * returns the list of domain obects
     */
    protected def listGet(requestParams) {
        return getDaoService().list(requestParams)
    }

    //TODO This should be handled in the DAO
    protected def pagedList(dlist) {
        def pageData = null//new Pager(params)
        def fieldList
        if (hasProperty('listFields')) {
            fieldList = listFields
        } else if (hasProperty('showFields')) {
            fieldList = showFields
        } else if (hasProperty('selectFields')) {
            fieldList = selectFields
        }
        pageData.setupData(dlist, fieldList)
        return pageData
    }

    /**
     * the rest way to get a domain.
     */
    def get() {
        def record = domainClass.get(params.id)
        if (record) {
            render BeanPathTools.buildMapFromPaths(record, selectFields) as JSON
        } else {
            response.status = 404
        }
    }

    /**
     * The core method to update or insert from json. once json is convertd it calls back out or
     */
    //XXX this should be called insertOrUpdate, is very confusing
    def create() {
        try {
            def p = BeanPathTools.flattenMap(request, request.JSON)
            log.debug "saveOrUpdateJson json p: ${p}"
            def result = createDomain(p)
            return render(BeanPathTools.buildMapFromPaths(result.entity, selectFields) as JSON)
        } catch (Exception e) {
            log.error("create with error: $e.message", e)
            def errResponse = errorMessageService.buildErrorResponse(e)
            response.status = errResponse.code
            render errResponse as JSON
        }
    }

    /**
     * The core method to update or insert from json. once json is convertd it calls back out or
     */
    //XXX this should be called insertOrUpdate, is very confusing
    def update() {
        try {
            def p = BeanPathTools.flattenMap(request, request.JSON)
            log.debug "saveOrUpdateJson json p: ${p}"
            def result = updateDomain(p)
            return render(BeanPathTools.buildMapFromPaths(result.entity, selectFields) as JSON)
        } catch (Exception e) {
            log.error("update with error: $e.message", e)
            def errResponse = errorMessageService.buildErrorResponse(e)
            response.status = errResponse.code
            render errResponse as JSON
        }
    }

    def delete() {
        log.debug("in deleteJson with ${params}")

        try {
            def p = request.JSON
            def result = deleteDomain(p)
            response.status = 204
            render result as JSON
        } catch (ValidationException e) {
            log.error("saveJson with error", e)
            response.status = 422
            def responseJson = [
                "code"       : 422,
                "status"     : "error",
                "message"    : errorMessageService.buildMsg(e.messageMap),
                "messageCode": e.messageMap.code
            ]
            render responseJson as JSON
        } catch (Exception e) {
            log.error("saveJson with error", e)
            response.status = 400
            def responseJson = [
                "code"   : 400,
                "status" : "error",
                "message": e.message,
                "error"  : e.message
            ]
            render responseJson as JSON
        }
    }

    /**
     * Called from the saves and saveOrUpdateJson,
     * providing a place to override functionality
     */
    //XXX this should be called insertDomain(), it is very confusing
    protected def insertDomain(p) {
        log.info("saveDomain(${p})")
        return dao.insert(p)
    }

    protected def updateDomain(p, opts = null) {
        params.remove('companyId') //TODO XXX - why is it here for every controllers ?
        log.debug "updateDomain with ${p}"
        def res = dao.update(p)
        if (opts?.flush) DaoUtil.flush()
        return res
    }

    //
    protected def deleteDomain(p) {
        return dao.remove(p)
    }

    protected def getDataService() {
        return dao
    }

    //Build human readable error message
    protected Map buildErrorResponse(e) {
        errorMessageService.buildErrorResponse(e)
    }

    //this should be able to be done easier than this
    protected errorBuilder(messageCode, args, defmsg, entity, meta) {
        def message = g.message('code': messageCode, 'args': args, 'default': defmsg)
        flash.message = null
        def errs = []

        if (entity) {
            errs = buildError(entity, errs)
        }
        if (meta) {
            meta.values()?.each { obj ->
                errs = buildError(obj, errs)
            }
        }
        //FIXME implement new way
        return [
            "code"       : 422,
            "status"     : "error",
            "message"    : message,
            "messageCode": messageCode,
            "id"         : entity ? "${entity.id}" : "",
            'errors'     : errs
        ]

    }

    def buildError(obj, errs) {
        eachError([bean: obj], {
            errs << [(it.field): [object          : it.objectName, field: it.field, message: g.message(error: it).toString(),
                                  'rejected-value': StringEscapeUtils.escapeXml(it.rejectedValue?.toString())]]
        })
        return errs
    }

    protected Map getExternalConfig() {
        ConfigObject controllerConfig = grailsApplication.setupConfig.screens."$controllerName"
        return controllerConfig
    }

    protected List<String> getDefaultShowFields() { return [] }

    protected def getSelectFields() {
        return externalConfig.show.fields ?: defaultShowFields
    }

    protected List<String> getDefaultListFields() { return [] }

    protected def getListFields() {
        return externalConfig.list.fields ?: defaultListFields
    }

    protected Map getDefaultGridOptions() {
        return [:]
    }

    protected Map getGridOptions() {
        def options = externalConfig.list.gridz
        if (!options) {
            return defaultGridOptions
        }
        return appSetupService.getValue(options)
    }

    /**
     * Renders js file with grid configuration.
     */
    def gridOptions() {
        String gridOptsJson = (gridOptions as JSON).toString()
        render(contentType: "text/javascript", text: "window.${controllerName}GridOptions = Object.freeze($gridOptsJson);")
    }
}
