package gorm.restapi

import grails.converters.JSON
import grails.core.GrailsApplication
import grails.plugin.dao.*
import grails.util.GrailsClassUtils
import grails.util.GrailsNameUtils
import grails.validation.ValidationException
import grinder.BeanPathTools
import grinder.Pager
import nine.rally.utils.CriteriaUtils
import org.apache.commons.lang.StringEscapeUtils
import org.springframework.context.MessageSource

import javax.annotation.PostConstruct

import grails.plugin.dao.DaoUtil

//took rally.BaseDomainController with core concepts from grails RestfulConroller and our RestApiDomainController
@Artefact("Controller")
abstract class RestApiDaoController<T>  {
    def ajaxGrid = true

    static allowedMethods = [save: "POST", update: "POST", saveOrUpdateJson: "POST", delete: ["POST", "DELETE"], deleteJson: ["POST", "DELETE"]]

    MessageSource messageSource
	ErrorMessageService errorMessageService

    //AppSetupService appSetupService
    GrailsApplication grailsApplication



    protected GormDaoSupport getDao(){
        return domainClass.dao
    }

	abstract def getDomainClass()

    @PostConstruct
    protected void init(){
        //println "init called and ga is ${grailsApplication?'initialized':'null'}"
    }

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


	def list() {
		if(request.format=='json' || response.format == 'json'){
			listJson()
		}else{
			if(ajaxGrid){
            	return listModel()
			} else {
				forward(action:"listhtml")
				return //stop the flow
			}
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

	/**
	 * calls the list criteria and renders the json
	 */
	protected def listJson(){
		def pageData = pagedList(listCriteria())
		render pageData.jsonData as JSON
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
	 * Returns a page with the expectation that it will make an ajax call back into here for json data
	 */
	protected def listModel(){
		return []
	}

    //the old standard way to list into an html page with out any ajax feeding the grid
	def listhtml(Integer max) {
	    params.max = Math.min(max ?: 10, 100)
		def pageData= pagedList(listCriteria())
		//[${propertyName}List: ${className}.list(params), ${propertyName}Total: ${className}.count()]
		def propName = GrailsClassUtils.getPropertyNameRepresentation(domainClass)
		return [("${propName}List".toString()): pageData.data, ("${propName}ListTotal".toString()): pageData.recordCount]
	}


	/**
	 * html way of rendering a create page with form. standard grails way
	 */
	def create() {
		def domainInstance = domainClass.newInstance()
		domainInstance.properties = params
		return [(domainInstanceName) : domainInstance]
    }

    /**
     * standard html render view to display a show page
     */
    def edit(Long id){
		def domainInstance = domainClass.get(id)
		if (!domainInstance) {
			flash.message = DaoMessage.notFound(GrailsNameUtils.getShortName(domainClass),params)
			redirect(action: "list")
			return
		}
		else {
			return [(domainInstanceName): domainInstance]
		}

	}

    def save(){
    	if(request.format=='json' || response.format=='json'){
    		saveOrUpdateJson()
    		return
    	}
    	//else render normal html view
		try {
			def result = saveDomain(params)
			flash.message = result.message
			redirect(action: 'show', id: result.entity.id)
			return
		} catch (ValidationException e) {
			log.warn("save with error ${request.format}")
			flash.message = e.messageMap
			render(view: 'create', model: [(domainInstanceName): e.entity])
        	return
		}
	}

	/**
	 * Called from the saves and saveOrUpdateJson,
	 * providing a place to override functionality
	 */
	 //XXX this should be called insertDomain(), it is very confusing
	protected def saveDomain(p){
		log.info("saveDomain(${p})")
		return dao.insert(p)
	}

	/**
	 * the standard show method for a sitemesh form
	 */
    def show(Long id){
		try{
			def domainInstance = showDomain(id)
			return [ (domainInstanceName) : domainInstance ]
		}catch(ValidationException e){
			flash.message =  e.messageMap
			redirect(action: "list")
		}
	}

	/**
	 * the rest way to get a domain. FIXME we need to look at more restful url mappings
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
	 * a simple method to override the show logic.
	 */
	protected def showDomain(Long id){
		def domainInstance = domainClass.get(id)
		if (!domainInstance) {
			throw new DomainException(DaoMessage.notFound(GrailsNameUtils.getShortName(domainClass),params), null)
		}
		return domainInstance
	}

    def update(){
    	if(request.format=='json' || response.format=='json'){
    		saveOrUpdateJson()
			DaoUtil.flush()
    		return
    	}
		try{
			def result = updateDomain(params)
			flash.message = result.message
			redirect(action: 'show', id: result.entity.id)
			return null
		} catch(ValidationException e) {
			log.error("Cannot update entity", e)
			flash.message = e.messageMap
			render(view: 'edit', model: [(domainInstanceName): e.entity])
			return null
		}
	}

	protected def updateDomain(p, opts=null){
		params.remove('companyId') //TODO XXX - why is it here for every controllers ?
		log.debug "updateDomain with ${p}"
		def res = dao.update(p)
		if(opts?.flush) DaoUtil.flush()
		return res
	}

	//FIXME not sure we need this anymore. Deprecate it?
	def saveOrUpdate(){
		try{
			def result = params.id ? dao.update(params) : dao.insert(params)
			//all was good render a success save message
			render message(code:'user.saved')
		}catch(ValidationException e){
			response.status = 409
			def emsg = (e.hasProperty("messageMap")) ? g.message(code:e.messageMap?.code,args:e.messageMap?.args,default:e.messageMap?.defaultMessage):null
			render(plugin:"rally", template:"userEdit",model:[user:e.meta?.user?:e.entity,errorMsg:emsg])
		}
	}

	/**
	 * The core method to update or insert from json. once json is convertd it calls back out or
	 */
	 //XXX this should be called insertOrUpdate, is very confusing
	def saveOrUpdateJson(){
		try {
			def p = BeanPathTools.flattenMap(request, request.JSON)
			log.debug "saveOrUpdateJson json p: ${p}"
			def result = p.id ? updateDomain(p) : saveDomain(p)
			return render(BeanPathTools.buildMapFromPaths(result.entity, selectFields) as JSON)
		} catch (Exception e) {
			log.error("saveJson with error: $e.message", e)
            def errResponse = errorMessageService.buildErrorResponse(e)
			response.status = errResponse.code
			render errResponse as JSON
		}
	}

	def delete() {
		if (request.format == 'json' || response.format == 'json') {
			return deleteJson()
		}
		try {
			log.info "process as html"
			def result = deleteDomain(params)
			flash.message = result.message
			redirect(action: "list")
		} catch (Exception e) {
			flash.message = e.messageMap
			redirect(action: "show", id: params.id)
		}
	}

	def deleteJson() {
		log.debug("in deleteJson with ${params}")

		try {
			def p = request.JSON
			println "calling deleteJson $p"
			def result = deleteDomain(p)
			println "delete successful  $p"
			//render result as JSON
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

	//
	protected def deleteDomain(p){
		return dao.remove(p)
	}

	/**
	 * a generic template that renders a template with the passed in id
	 * so a "customer/templateView/searchForm" will render that template
	 */
	//XXX this needs test coverage
	def templateView() {
		render(template: params.id)
	}

	//Stanadard template rendering that can be overriden in sub-classes
	def listTemplate() {
		render(template: "list")
	}
	def searchForm() {
		render(template: "searchForm")
	}
	def showTemplate() {
		render(template: 'show')
	}
	def formTemplate() {
		render(template: "form")
	}
	def massUpdateForm() {
		render(template: "massUpdateForm")
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
		// return ["response":[
		// 	"status": "fail",
		// 	"ok":false,
		// 	"error": true,
		// 	"message":message,
		// 	"id": entity ? "${entity.id}" : "",
		// 	'errors':errs
		// ]]
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

	//uses the stuff in flash to create an error response with the errorBuilder
	def errorModel(entity, meta){
		errorBuilder(flash.message?.code, flash.message?.args, flash.message?.defaultMessage, entity,meta)
	}

	// def successModel(id){
	// 	def message = g.message('code':flash.message?.code,'args':flash.message?.args,'default':flash.message?.defaultMessage)
	// 	flash.message = null
	//     return	["response":[
	// 		"status": "ok",
	// 		"ok":true,
	// 		"error": false,
	// 		"message":message ,
	// 		"id":id
	// 	]]
	// }

	//HTTP status codes
/*
The following table describes what various HTTP status codes mean in the context of the our Rest APIs.

Code 	Explanation
200 OK 	No error.
204 OK 	No error and no response. used for things like delete success
possible in fuure
** 201 CREATED 	Creation of the object was successful.

** 304 FUTURE

//these are standard error codes we will use
400 BAD REQUEST 	Invalid request header, or invalid JSON
403 FORBIDDEN 		authentication or authorization failed. //NOTE: Github uses 404 to prevent sending stuff that might compromise security
404 NOT FOUND 		Standard - Resource/domain not found. may also be used when a user is not authorized
422 Unprocessable   A validation error occured or a business rule was violated

possible in future
*** 409 CONFLICT Validation error or the version number doesn't match resource's latest version number.


500 INTERNAL SERVER ERROR 	Internal error. This is the default code that is used for all unrecognized internal server errors.
*/

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
     * Renders JSON file with jqGrid configuration.
     */
    def gridOptions() {
		String gridOptsJson = (gridOptions as JSON).toString()
        render(contentType: "text/javascript", text: "window.${controllerName}GridOptions = Object.freeze($gridOptsJson);")
    }
}
