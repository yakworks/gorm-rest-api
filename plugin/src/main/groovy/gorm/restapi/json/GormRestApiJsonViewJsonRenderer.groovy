package gorm.restapi.json

import gorm.tools.beans.BeanPathTools
import grails.core.support.proxy.ProxyHandler
import grails.rest.render.RenderContext
import grails.rest.render.Renderer
import grails.rest.render.RendererRegistry
import grails.views.mvc.SmartViewResolver
import grails.views.mvc.renderer.DefaultViewRenderer
import grails.web.mime.MimeType
import groovy.util.logging.Slf4j
import org.grails.plugins.web.rest.render.ServletRenderContext
import org.springframework.web.servlet.view.AbstractUrlBasedView

@Slf4j
class GormRestApiJsonViewJsonRenderer<T> extends DefaultViewRenderer<T> {
    GormRestApiJsonViewJsonRenderer(Class<T> targetType, SmartViewResolver viewResolver, ProxyHandler proxyHandler, RendererRegistry rendererRegistry, Renderer defaultRenderer) {
        super(targetType, viewResolver, proxyHandler, rendererRegistry, defaultRenderer)
    }

    GormRestApiJsonViewJsonRenderer(Class<T> targetType, MimeType mimeType, SmartViewResolver viewResolver, ProxyHandler proxyHandler, RendererRegistry rendererRegistry, Renderer defaultRenderer) {
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
            if (!mimeType.equals(MimeType.ALL)) {
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
                List includes = context.includes ?: ["*"]
                def val = [:]
                if (object instanceof List){
                    val = object.toArray().collect { obj ->
                        BeanPathTools.buildMapFromPaths(obj, includes, true)
                    }
                } else {
                    val = BeanPathTools.buildMapFromPaths(object, includes, true)
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
