package gorm.restapi.json

import grails.converters.JSON
import grails.rest.render.RenderContext
import grails.rest.render.RendererRegistry
import grails.web.mime.MimeType
import org.grails.plugins.web.rest.render.json.DefaultJsonRenderer
import org.grails.web.gsp.io.GrailsConventionGroovyPageLocator

class GormRestApiJsonRenderer<T> extends DefaultJsonRenderer<T>{

    GormRestApiJsonRenderer(Class<T> targetType) {
        super(targetType)
    }

    GormRestApiJsonRenderer(Class<T> targetType, MimeType... mimeTypes) {
        super(targetType, mimeTypes)
    }

    GormRestApiJsonRenderer(Class<T> targetType, GrailsConventionGroovyPageLocator groovyPageLocator) {
        super(targetType, groovyPageLocator)
    }

    GormRestApiJsonRenderer(Class<T> targetType, GrailsConventionGroovyPageLocator groovyPageLocator, RendererRegistry rendererRegistry) {
        super(targetType, groovyPageLocator, rendererRegistry)
    }

    //TODO: replace with something like we do in Jsonify
    protected void renderJson(T object, RenderContext context) {
        JSON converter
        if (namedConfiguration) {
            JSON.use(namedConfiguration) {
                converter = object as JSON
            }
        } else {
            converter = object as JSON
        }
        renderJson(converter, context)
    }

    //TODO: replace with something like we do in Jsonify
    protected void renderJson(JSON converter, RenderContext context) {
        converter.setExcludes(context.excludes)
        converter.setIncludes(context.includes)
        converter.render(context.getWriter())
    }
}
