package gorm.restapi.controller

import gorm.tools.Pager
import grails.artefact.Artefact
import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.dao.DomainException
import grails.plugin.dao.ErrorMessageService
import grails.plugin.dao.GormDaoSupport
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import gorm.tools.beans.BeanPathTools
import gorm.tools.criteria.CriteriaUtils
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.context.MessageSource
import grails.plugin.dao.DaoUtil

/**
* Credits: took rally.BaseDomainController with core concepts from grails RestfulConroller
* Some of this is, especailly the cache part is lifted from the older grails2 restful-api plugin
*
* @author Joshua Burnett
*/
//see grails-core/grails-plugin-rest/src/main/groovy/grails/artefact/controller/RestResponder.groovy
// we can get some good ideas from how that plugin does things
@Artefact("Controller")
class RestApiDaoController<T>  {
    static allowedMethods = [list: ["GET","POST"], create: "POST",
                             update: ["PUT", "PATCH"], delete: "DELETE"]

    static responseFormats = ['json']
    static namespace = 'api'

    Class<T> resource
    String resourceName
    String resourceClassName
    boolean readOnly


    MessageSource messageSource
    ErrorMessageService errorMessageService

    //AppSetupService appSetupService
    GrailsApplication grailsApplication

    RestApiDaoController(Class<T> resource) {
        this(resource, false)
    }

    RestApiDaoController(Class<T> resource, boolean readOnly) {
        this.resource = resource
        this.readOnly = readOnly
        resourceClassName = resource.simpleName
        resourceName = GrailsNameUtils.getPropertyName(resource)
    }

    protected GormDaoSupport getDao(){
        return domainClass.dao
    }

    Class<T> getDomainClass(){
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
                result = getService().list(request.body, requestParams)
            }
            else {
                result = getService().list(requestParams)
            }

            respond result

            String etagValue = etagGenerator.shaFor( result, count, responseRepresentation.mediaType )

            String  tch = totalCountHeader,
                    poh = pageOffsetHeader,
                    pmh = pageMaxHeader

//            withCacheHeaders {
//                etag {
//                    etagValue
//                }
//                delegate.lastModified {
//                    lastModifiedFor( result )
//                }
//                generate {
//                    ResponseHolder holder = new ResponseHolder()
//                    holder.data = result
//                    holder.addHeader(totalCountHeader, count)
//                    holder.addHeader(pageOffsetHeader, requestParams.offset ? requestParams?.offset : 0)
//                    holder.addHeader(pageMaxHeader, requestParams.max ? requestParams?.max : result.size())
//                    renderSuccessResponse( holder, 'default.rest.list.message' )
//                }
//            }
        }
        catch (e) {
            logMessageError(e)
            renderErrorResponse(e)
        }
    }


    /**
     * returns the list of domain obects
     */
    protected def listCriteria(){
        def crit = domainClass.createCriteria()
        def pager = new Pager(params)
        def datalist = crit.list(max: pager.max, offset: pager.offset) {
            if (params.sort)
                CriteriaUtils.applyOrder(params, delegate)
        }
        return datalist
    }

    protected def pagedList(dlist) {
        def pageData = new Pager(params)
        def fieldList
        if(hasProperty('listFields')){
            fieldList = listFields
        }
        else if(hasProperty('showFields')){
            fieldList = showFields
        }
        else if(hasProperty('selectFields')){
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
    def create(){
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
    def update(){
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
                "code": 422,
                "status": "error",
                "message": errorMessageService.buildMsg(e.messageMap),
                "messageCode": e.messageMap.code
            ]
            render responseJson as JSON
        } catch (Exception e) {
            log.error("saveJson with error", e)
            response.status = 400
            def responseJson = [
                "code": 400,
                "status": "error",
                "message": e.message,
                "error": e.message
            ]
            render responseJson as JSON
        }
    }

    /**
     * Called from the saves and saveOrUpdateJson,
     * providing a place to override functionality
     */
    //XXX this should be called insertDomain(), it is very confusing
    protected def insertDomain(p){
        log.info("saveDomain(${p})")
        return dao.insert(p)
    }


    protected def updateDomain(p, opts=null){
        params.remove('companyId') //TODO XXX - why is it here for every controllers ?
        log.debug "updateDomain with ${p}"
        def res = dao.update(p)
        if(opts?.flush) DaoUtil.flush()
        return res
    }

    //
    protected def deleteDomain(p){
        return dao.remove(p)
    }

    //Build human readable error message
    protected Map buildErrorResponse(e){
        errorMessageService.buildErrorResponse(e)
    }

    //this should be able to be done easier than this
    protected errorBuilder(messageCode,args,defmsg,entity,meta) {
        def message = g.message('code':messageCode,'args':args,'default':defmsg)
        flash.message = null
        def errs = []

        if(entity){
            errs = buildError(entity, errs)
        }
        if(meta){
            meta.values()?.each {obj->
                errs = buildError(obj, errs)
            }
        }
        //FIXME implement new way
        return [
            "code": 422,
            "status": "error",
            "message":message,
            "messageCode":messageCode,
            "id": entity ? "${entity.id}" : "",
            'errors':errs
        ]

    }

    def buildError (obj, errs){
        eachError([bean:obj], {
            errs << [(it.field):[object:it.objectName,field:it.field,message:g.message(error:it).toString(),
                'rejected-value':StringEscapeUtils.escapeXml(it.rejectedValue?.toString())]]
        })
        return errs
    }

    protected Map getExternalConfig() {
        ConfigObject controllerConfig =  grailsApplication.setupConfig.screens."$controllerName"
        return controllerConfig
    }

    protected List<String> getDefaultShowFields() {return []}

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
