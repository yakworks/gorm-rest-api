package funcytown

import grails.util.GrailsClassUtils

class UrlMappings {

    //def grailsApplication

    static mappings = {

        // for(controller in getGrailsApplication().controllerClasses) {
        //     println "controler $controller.fullName"
        //     def cName = controller.logicalPropertyName
        //     def namespace = GrailsClassUtils.getStaticPropertyValue(controller.clazz, 'namespace')
        //     println "controler $cName with namespace $namespace"

        //     if(namespace == 'api') {
        //         group("/api") {
        //             delete "/${cName}/$id(.$format)?"(controller: cName, action: "delete")
        //             get "/${cName}(.$format)?"(controller: cName, action: "index")
        //             get "/${cName}/$id(.$format)"(controller: cName, action: "show")
        //             post "/${cName}(.$format)?"(controller: cName, action: "save")
        //             put "/${cName}/$id(.$format)?"(controller: cName,action: "update")
        //             patch "/${cName}/$id(.$format)?"(controller: cName, action: "patch")
        //         }
        //     }
        // }

        // group("/api") {
        //     delete "/$controller/$id(.$format)?"(action: "delete")
        //     get "/$controller(.$format)?"(action: "index")
        //     get "/$controller/$id(.$format)?"(action: "show")
        //     post "/$controller(.$format)?"(action: "save")
        //     put "/$controller/$id(.$format)?"(action: "update")
        //     patch "/$controller/$id(.$format)?"(action: "patch")
        // }

        //normal controllers
        "/$controller/$action?/$id?(.$format)?"{
            constraints {
                // apply constraints here
            }
        }

        "/"(controller: 'application', action:'index')
        //"schema"(controller: 'schema')
        "500"(view: '/error')
        "404"(view: '/notFound')
    }

}
