description("Creates a new rest domain and test") {
    usage "grails create-rest-domain [Domain NAME]"
    argument name:'Domain Name', description:"The name of the domain"
}

model = model(args[0])
render  template:"RestApiDomain.groovy",
        destination: file( "grails-app/domain/$model.packagePath/${model.simpleName}.groovy"),
        model: model

render  template:"RestApiDomainFunctionalSpec.groovy",
        destination: file( "src/integration-test/groovy/$model.packagePath/${model.simpleName}RestApiSpec.groovy"),
        model: model