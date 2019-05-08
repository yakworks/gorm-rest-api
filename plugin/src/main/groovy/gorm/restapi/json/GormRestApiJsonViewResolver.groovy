package gorm.restapi.json

import grails.plugin.json.renderer.ErrorsJsonViewRenderer
import grails.plugin.json.view.JsonViewConfiguration
import grails.plugin.json.view.JsonViewTemplateEngine
import grails.plugin.json.view.JsonViewWritableScript
import grails.plugin.json.view.mvc.JsonViewResolver
import grails.web.mime.MimeType
import org.springframework.validation.Errors

import javax.annotation.PostConstruct

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
            def defaultJsonRenderer = rendererRegistry.findRenderer(MimeType.JSON, Object.class)
            viewConfiguration.mimeTypes.each { String mimeTypeString ->
                MimeType mimeType = new MimeType(mimeTypeString, "json")
                rendererRegistry.addDefaultRenderer(
                    new GormRestApiJsonViewJsonRenderer<Object>(Object.class, mimeType, this , proxyHandler, rendererRegistry, defaultJsonRenderer)
                )
            }
        }
    }
}
