package gorm.restapi

import groovy.transform.CompileDynamic

@CompileDynamic
class BootStrap {

    Closure init = { servletContext ->
    }
    Closure destroy = {
    }
}
