package gorm.restapi.controller

import groovy.transform.CompileDynamic

@CompileDynamic
class ResponseHolder {
    Object data
    Map headers = [:]
    String message

    void addHeader(String name, Object value) {
        if (!headers[name]) {
            headers[name] = []
        }
        headers[name].add value?.toString()
    }
}
