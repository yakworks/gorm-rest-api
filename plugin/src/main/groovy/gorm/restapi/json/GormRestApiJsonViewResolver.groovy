/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.restapi.json

import javax.annotation.PostConstruct

import groovy.transform.CompileDynamic

import org.springframework.validation.Errors

import grails.plugin.json.renderer.ErrorsJsonViewRenderer
import grails.plugin.json.view.JsonViewConfiguration
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.JsonViewWritableScript
import grails.plugin.json.view.mvc.JsonViewResolver
import grails.web.mime.MimeType

@CompileDynamic
class GormRestApiJsonViewResolver extends JsonViewResolver {

    public static final String JSON_VIEW_SUFFIX = ".${JsonViewWritableScript.EXTENSION}"

    GormRestApiJsonViewResolver(JsonViewConfiguration configuration = new JsonViewConfiguration()) {
        this(new JsonViewTemplateEngine(configuration))
    }

    GormRestApiJsonViewResolver(JsonViewTemplateEngine templateEngine) {
        this(templateEngine, JSON_VIEW_SUFFIX, MimeType.JSON.name)
    }

    GormRestApiJsonViewResolver(JsonViewTemplateEngine templateEngine, String suffix, String contentType) {
        super(templateEngine, suffix, contentType)
    }

    @PostConstruct
    void initialize() {
        if(rendererRegistry != null) {
            def errorsRenderer = new ErrorsJsonViewRenderer((Class) Errors)
            errorsRenderer.setJsonViewResolver(this)
            rendererRegistry.addRenderer(errorsRenderer)
            def defaultJsonRenderer = rendererRegistry.findRenderer(MimeType.JSON, Object)
            viewConfiguration.mimeTypes.each { String mimeTypeString ->
                MimeType mimeType = new MimeType(mimeTypeString, "json")
                rendererRegistry.addDefaultRenderer(
                    new GormRestApiJsonViewJsonRenderer<Object>(Object, mimeType, this , proxyHandler, rendererRegistry, defaultJsonRenderer)
                )
            }
        }
    }
}
