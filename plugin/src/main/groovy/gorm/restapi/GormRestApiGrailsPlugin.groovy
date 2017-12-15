package gorm.restapi

import gorm.restapi.appinfo.AppInfoBuilder

import grails.core.GrailsApplication
import grails.core.GrailsClass
import grails.plugins.Plugin
import groovy.transform.CompileStatic
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler

@SuppressWarnings(['NoDef', 'EmptyMethod', 'VariableName', 'EmptyCatchBlock'])
class GormRestApiGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    String grailsVersion = "3.2.11 > *"
    // resources that are excluded from plugin packaging
    List pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    String title = "Gorm Rest Api Tools" // Headline display name of the plugin
    String author = "Your name"
    String authorEmail = ""
    String description = '''\
Brief summary/description of the plugin.
'''
    //List profiles = ['web']
    List loadBefore = ['controllers']
    List observe = ['domainClass']

    // URL to the plugin's documentation
    String documentation = "http://grails.org/plugin/gorm-rest-tools"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    String license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    Map organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    List developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    Map issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    Map scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

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
                if (!app.getArtefact(ControllerArtefactHandler.TYPE, controllerClassName)) {

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

