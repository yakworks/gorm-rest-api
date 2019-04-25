package gorm.restapi.json

import grails.rest.render.Renderer
import grails.web.mime.MimeType
import groovy.transform.Canonical
import org.grails.plugins.web.rest.render.DefaultRendererRegistry
import org.springframework.validation.Errors

import javax.annotation.PostConstruct
import java.util.concurrent.ConcurrentHashMap

class GormRestApiRendererRegistry extends DefaultRendererRegistry {
    Map<ContainerRendererCacheKey, Renderer> containerRenderers = new ConcurrentHashMap<>()

    @Override
    @PostConstruct
    void initialize() {
        super.initialize()
        addDefaultRenderer(new GormRestApiJsonRenderer<Object>(Object, groovyPageLocator, this))
        containerRenderers.put(new ContainerRendererCacheKey(Errors, Object, MimeType.JSON), new GormRestApiJsonRenderer(Errors))
        containerRenderers.put(new ContainerRendererCacheKey(Errors, Object, MimeType.TEXT_JSON), new GormRestApiJsonRenderer(Errors))
    }

    @Canonical
    class RendererCacheKey {
        Class clazz
        MimeType mimeType
    }

    @Canonical
    class ContainerRendererCacheKey {
        Class containerType
        Class clazz
        MimeType mimeType
    }
}
