/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.restapi.json

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.web.servlet.view.AbstractUrlBasedView

import gorm.tools.json.Jsonify
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.views.mvc.SmartViewResolver
import grails.views.mvc.renderer.DefaultViewRenderer
import grails.web.mime.MimeType

@Slf4j
@CompileDynamic
class GormRestApiJsonViewJsonRenderer<T> extends DefaultViewRenderer<T> {
    GormRestApiJsonViewJsonRenderer(Class<T> targetType, SmartViewResolver viewResolver, ProxyHandler proxyHandler, RendererRegistry rendererRegistry,
                                    Renderer defaultRenderer) {
        super(targetType, viewResolver, proxyHandler, rendererRegistry, defaultRenderer)
    }

    GormRestApiJsonViewJsonRenderer(Class<T> targetType, MimeType mimeType, SmartViewResolver viewResolver, ProxyHandler proxyHandler, RendererRegistry rendererRegistry,
                                    Renderer defaultRenderer) {
        super(targetType, mimeType, viewResolver, proxyHandler, rendererRegistry, defaultRenderer)
    }

    @Override
    void render(T object, RenderContext context) {
        def arguments = context.arguments
        def ct = arguments?.contentType

        if(ct) {
            context.setContentType(ct.toString())
        }
        else {
            final mimeType = context.acceptMimeType ?: mimeTypes[0]
            if (!mimeType == MimeType.ALL) {
                context.setContentType(mimeType.name)
            }
        }

        String viewName
        if (arguments?.view) {
            viewName = arguments.view.toString()
        }
        else {
            viewName = context.actionName
        }

        String viewUri
        if (viewName?.startsWith('/')) {
            viewUri = viewName
        } else {
            viewUri = "/${context.controllerName}/${viewName}"
        }

        def webRequest = ((ServletRenderContext) context).getWebRequest()
        if (webRequest.controllerNamespace) {
            viewUri = "/${webRequest.controllerNamespace}" + viewUri
        }

        def request = webRequest.currentRequest
        def response = webRequest.currentResponse

        AbstractUrlBasedView view = (AbstractUrlBasedView)viewResolver.resolveView(viewUri, request, response)
        if(view == null) {
            if(proxyHandler != null) {
                object = (T)proxyHandler.unwrapIfProxy(object)
            }

            def cls = object.getClass()
            // Try resolve template. Example /book/_book
            view = (AbstractUrlBasedView)viewResolver.resolveView(cls, request, response)
        }

        if(view != null) {
            Map<String, Object> model
            if (view == viewResolver.objectView){
                def val = [:]
                if (object instanceof List){
                    List list = object.toArray().collect { obj ->
                        Jsonify.render(obj, [includes: arguments.includes?:["*"]]).json as Map
                    }
                    val = [data: list, metaData:[totalCount: object.getTotalCount()]] // TODO: dirty way, need to think how to make it smarter
                } else {
                    val = Jsonify.render(object, [includes: arguments.includes?:["*"]]).json as Map
                }
                defaultRenderer.render(val, context)

            } else {
                if(object instanceof Map) {
                    model = (Map) object
                }
                else {
                    model = [(resolveModelVariableName(object)): object]
                }
                view.render(model, request, response)
            }

        }
        else {
            defaultRenderer.render(object, context)
        }
    }
}
