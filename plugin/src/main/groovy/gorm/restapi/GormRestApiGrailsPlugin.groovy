package gorm.restapi

import gorm.restapi.appinfo.AppInfoBuilder
import gorm.restapi.json.GormRestApiJsonViewResolver
import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugin.json.view.JsonViewConfiguration
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.api.jsonapi.DefaultJsonApiIdRenderer
import grails.plugin.json.view.mvc.JsonViewResolver
import grails.plugins.Plugin
import grails.views.mvc.GenericGroovyTemplateViewResolver
import grails.views.resolve.PluginAwareTemplateResolver
import groovy.transform.CompileStatic
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.plugins.appsetupconfig.AppSetupService
import org.grails.web.servlet.view.CompositeViewResolver

@SuppressWarnings(['NoDef', 'EmptyMethod', 'VariableName', 'EmptyCatchBlock'])
class GormRestApiGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "3.2.11 > *"
    // resources that are excluded from plugin packaging
    List pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    String title = "Gorm Rest Api Tools"
    // Headline display name of the plugin
    String author = "Your name"
    String authorEmail = ""
    String description = '''\
Brief summary/description of the plugin.
'''
    //List profiles = ['web']
    List loadBefore = ['controllers']
    List loadAfter = [ 'views-json', 'app-setup-config']
    List observe = ['domainClass']

    // URL to the plugin's documentation
    String documentation = "http://grails.org/plugin/gorm-rest-tools"

    GrailsApplication grailsApplication

    Closure doWithSpring() {
        { ->
            jsonSchemaGenerator(JsonSchemaGenerator) { bean ->
                // Autowiring behaviour. The other option is 'byType'. <<autowire>>
                // bean.autowire = 'byName'
            }

            appInfoBuilder(AppInfoBuilder) { bean ->
                // Autowiring behaviour. The other option is 'byType'. <<autowire>>
                // bean.autowire = 'byName'
            }

            jsonSmartViewResolver(GormRestApiJsonViewResolver, jsonTemplateEngine) {
                templateResolver = bean(PluginAwareTemplateResolver, jsonViewConfiguration)
            }
            jsonViewResolver(GenericGroovyTemplateViewResolver, jsonSmartViewResolver )

            GrailsApplication application = grailsApplication
            GormRestApiGrailsPlugin.registryRestApiControllers(application)

        }
    }

    @Override
    void onChange(Map<String, Object> event) {
        GormRestApiGrailsPlugin.registryRestApiControllers(grailsApplication)
    }

    @CompileStatic
    static void registryRestApiControllers(GrailsApplication app) {
        for (GrailsClass grailsClass in app.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            final clazz = grailsClass.clazz
            if (clazz.getAnnotation(RestApi)) {
                //println "${clazz.name}"
                String controllerClassName = "${clazz.name}Controller"
                //Check if we already have such controller in app
                if (!app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName) && !(app.getArtefacts
                (ControllerArtefactHandler.TYPE)*.name.contains(clazz.simpleName))) {

                    try {
                        app.addArtefact(ControllerArtefactHandler.TYPE, app.classLoader.loadClass(controllerClassName))
                        //println "added $controllerClassName"
                    } catch (ClassNotFoundException cnfe) {

                    }
                }
            }
        }
    }

}
