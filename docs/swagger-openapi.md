# OpenAPI Specification (OAS)

> The [OpenAPI] Specification (OAS) defines a standard, language-agnostic interface to RESTful APIs which allows both humans and computers to discover and understand the capabilities of the service without access to source code, documentation, or through network traffic inspection.

[OAS] formerly known as [Swagger] and they donated it to the Linux Foundation to become OpenAPI (Swagger is a better name) . Swagger was version 2.0 and current version of [OAS] is 3.0. 

OAS is a super set of [json-schema]. In Grails or Spring, OAS describes both the controllers and actions as well the domains. Its uses [json-schema], particularly the validation spec to describe the domains. 

> :memo: See [here for an example](http://editor.swagger.io/#/) of the Swagger 2.0 spec in action.

  [OpenAPI]: https://github.com/OAI/OpenAPI-Specification
  [OAS]: https://github.com/OAI/OpenAPI-Specification
  [Swagger]: https://swagger.io/announcing-openapi-3-0/

## Goals

There are 2 primary goals of getting OAS and json-schema setup for our Grails Gorm domains

1. Enable automatic generation of forms with something like [Schema Form](http://schemaform.io/)
2. Autogenerate docs like some of the examples links below

### json-schema docs, specs and examples

> :bookmark: 
> 
> - [json-schema]( http://json-schema.org/ )
- [json-schema example]( http://json-schema.org/example1.html )
- [json-schema Spec]( http://json-schema.org/latest/json-schema-core.html )
- [json-schema Validation Spec]( http://json-schema.org/latest/json-schema-validation.html )
- [Examples]( http://json-schema.org/examples.html ) 
> - spacetelescope has a [Decent guide]( https://spacetelescope.github.io/understanding-json-schema/ )
> - Another [example]( http://developers.ros.gov.uk/schema/common/index.html) of what we are after

### Other Articles & Tutorials

https://brandur.org/elegant-apis
https://blog.cloudflare.com/cloudflares-json-powered-documentation-generator/

## Swagger

Swagger is a superset of json-schema. See http://editor.swagger.io/#/ for an example
And the Swagger site for plenty of examples

## Doc Engines

If we have our API defined in Swagger and/or json-schema or yml then we can use something like 
https://github.com/mermade/widdershins to generate a slate based doc site
See http://mikeralphson.github.io/openapi/2016/12/19/oa2s-comparison 
Example site https://mermade.github.io/shins/#swagger-petstore-v1-0-0 

As mentioned in widdershins if we are using a message queue then this is worth looking at for 

## Other Resources

### VueJs

https://github.com/koumoul-dev/vue-openapi
https://github.com/koumoul-dev/openapi-viewer

### Examples

http://swapi.co/
https://projects.spring.io/spring-restdocs/

Java version of schema validator
https://github.com/java-json-tools/json-schema-validator

## javadoc to markdown
https://delight-im.github.io/Javadoc-to-Markdown/

## Using javadocs or annotations
These are some possibilities to generate swagger or docs from javadocs ideally
https://wiki.onosproject.org/display/ONOS/Generating+Swagger+documentation+for+the+REST+API

This provides a pretty good overview of 4 options. Swagger(spring fox), Spring REST Docs, RAML, ApiDocJS
https://opencredo.com/rest-api-tooling-review/ and Spring Fox looks promising http://springfox.github.io/springfox/docs/current/ there seems to be a version for Grails too, https://github.com/springfox/springfox-grails-integration

[json-schema]: 
