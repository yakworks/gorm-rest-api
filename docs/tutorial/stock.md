## Getting started

For this tutorial you will need

* JDK (I advise 8, but you can take 7 as well).

* Git.

* Grails 3.2.11 (you can install it with http://sdkman.io on most Unix based systems.)

After all is installed clone the repo:

```
git clone https://github.com/9ci/angle-grinder
```

and switch to branch `rest_tutorial` branch, and go to `angle-grinder/grails/restTutorial`, the final result is in
the `snapshot` folder for each step

So first let's create new grails app:

```
$ grails create-app -profile rest-api -features hibernate4 resttutorail
Application created at angle-grinder/grails/restTutorial
```

Grails 3 provides several different profiles you can read about them in the [grails docs]

## Creating an API with Grails Web Services

As described in the [grails ws docs]
we will use the default out of the box functionality as a starting point.

### Creating a GORM domain

```
grails create-domain-class Contact
```

Then set it up like so:

**Contact.groovy**
```groovy
package resttutorial

class Contact {
  String firstName
  String lastName
  String email
  Boolean inactive

  static constraints = {
    firstName nullable: false
    inactive bindable: false
  }
}
```

To avoid writing `nullable: true` we will set the default to allow nulls for fields
Add the following to `grails-app/conf/application.groovy`

**application.groovy**
```groovy
grails.gorm.default.constraints = {
  '*' (nullable: true, blank: true)
}
```

We will load 100 rows of mock test data from a file `Contacts.json` in resources.
The mock data was generated from a great tool https://www.mockaroo.com

Add the following code to `grails-app/init/BootStrap.groovy`

**BootStrap.groovy**
```groovy
package resttutorial

import groovy.json.JsonSlurper

class BootStrap {
    def grailsApplication
    def init = { servletContext ->
        def data = new JsonSlurper().parse(new File("../resources/Contacts.json"))
        data.each{
          Contact contact = new Contact(it)
          contact.save(failOnError:true,flush: true)
        }
    }
    def destroy = {
    }
}
```

#### Adding the `@Resource` annotation to our domain
:url-dr: {docs-grails}#domainResources

So now we can start working on creating REST Api for our app.
The easiest way is to use {url-dr}[domain resources].
So as we see from {url-dr}[docs] we just need to update our domain a bit (just add {docs-grails-api}/grails/rest/Resource.html[@Resource] anotation) in such a way:

**Contact.groovy**
```groovy
import grails.rest.Resource

@Resource(uri = '/contact', formats = ["json"])
class Contact {
  ...
}
```

> :memo **On plural resource names**
As you will notice we did not pluralize it to contacts above as many will do.
We are aware of the debate on this in the rest world. We feel this will cause confusion down the line to do it.
>
1. English plural rules like "cherry/cherries" or "goose/geese/moose/meese" are not the nicest thing to think of while developing API, particularly when english is not your mother tongue.
2. Many times, as in Grails, we want to generate endpoint from the model, which is usually singular. It does not play nicely with the above pluralization exceptions and creates more work maintaining UrlMappings.
3. When the model is singular, which is normally is for us, keeping the rest endpoint singular will have the rest developers and the grails developers speaking the same language
4. The argument "usually you start querying by a Get to display a list" does not refer to any real use case. And we will end up querying single items as much as and even more than a list of items.

##### The `RestfullController`

`@Resource` creates a RestfullController for the domain

> :bulb: **The `@Resource` annotation**  
> is used in an ASTTransformation that creates a controller that extends RestfullController. See [ResourceTransform](https://github.com/grails/grails-core/blob/master/grails-plugin-rest/src/main/groovy/org/grails/plugins/web/rest/transform/ResourceTransform.groovy) for details on how it does this. Later we will show how to specify the controller to user with superClass property.

### Default Endpoints and Status Codes

#### Url Mappings

The [Extending Restful Controllers](http://docs.grails.org/3.2.11/guide/webServices.html#extendingRestfulController) section of the [grails docs] outlines the action names and the URIs they map to:

.URI, Controller Action and Response Defaults
[cols="2,1,1,3", format="csv", options="header", width="80",grid=rows]
|===
URI, Method, Action, Response Data
/contact , GET , index , Paged List
/contact/create, GET , create , Contact.newInstance() unsaved
/contact, POST , save , The successfully saved contact (same as show's get)
/contact/${id}, GET , show , The contact for the id
/contact/${id}/edit, GET , edit , The contact for the id. same as show
/contact/${id}, PUT , update , The successfully updated contact
/contact/${id}, DELETE , delete , Empty response with HTTP status code 204
|===

==== Status Code Defaults

Piecing together the {docs-HttpStatus}[HttpStatus codes] and results from RestfullController, RestResponder and _errors.gson,
these are what looks like the out of the box status codes as of Grails 3.2.2

.Status Codes Out Of Box
[options="header", cols="1,2", grid=rows]
|===
| Status Code               | Description
| 200 - OK                  | Everything worked as expected. default
| 201 - CREATED             | Resource/instance was created. returned from `save` action
| 204 - NO_CONTENT          | response code on successful DELETE request
| 404 - NOT_FOUND           | The requested resource doesn't exist.
| 405 - METHOD_NOT_ALLOWED  | If method (GET,POST,etc..) is not setup in `static allowedMethods` for action or resource is read only
| 406 - NOT_ACCEPTABLE      | Accept header requests a response in an unsupported format. not configed in mime-types. RestResponder uses this
| 422 - UNPROCESSABLE_ENTITY | Validation errors.
|===


=== API Namespace

A Namespace is a mechanism to partition resources into a logically named group.

So the controllers that response for the REST endpoints we will move to separate namespace to avoid cases when we need to
have Controllers for GSP rendering or some other not related to REST stuff.

As a our preferred namespace design we will use the "api" namespace prefix for the rest of the tutorial.
So we will add `namespace = 'api'` on the contact @Resource. @Resource has also property `uri` but it will override namespace property,
for example if @Resource(namespace = 'api', uri='contacts', formats = ["json"]) url for resource will be `localhost:8080/contacts`, not

.Contact.groovy
```groovy
@Resource(namespace = 'api', formats = ["json"])
class Contact
```

Also we need to update UrlMappings.groovy, there are two ways:

1. Add `/api` prefix to each mapping for example  `get "/api/$controller(.$format)?"(action:"index")`
2. Use `group` property

We will use the second case:

.UrlMappings.groovy
```groovy
package resttutorial

class UrlMappings {

    static mappings = {
      group("/api") {
        delete "/$controller/$id(.$format)?"(action:"delete")
        get "/$controller(.$format)?"(action:"index")
        get "/$controller/$id(.$format)?"(action:"show")
        post "/$controller(.$format)?"(action:"save")
        put "/$controller/$id(.$format)?"(action:"update")
        patch "/$controller/$id(.$format)?"(action:"patch")
      }
        ...
    }
}
```

You can see all available endpoints that Grails create for us with url-mappings-report:

----
$ grails url-mappings-report
[options="header", cols="1,2", grid=rows]
Dynamic Mappings
 |    *     | ERROR: 500                                | View:   /error           |
 |    *     | ERROR: 404                                | View:   /notFound        |
 |   GET    | /api/${controller}(.${format)?            | Action: index            |
 |   POST   | /api/${controller}(.${format)?            | Action: save             |
 |  DELETE  | /api/${controller}/${id}(.${format)?      | Action: delete           |
 |   GET    | /api/${controller}/${id}(.${format)?      | Action: show             |
 |   PUT    | /api/${controller}/${id}(.${format)?      | Action: update           |
 |  PATCH   | /api/${controller}/${id}(.${format)?      | Action: patch            |

Controller: application
 |    *     | /                                                  | Action: index            |

Controller: contact
 |   GET    | /api/contact/create                                | Action: create           |
 |   GET    | /api/contact/${id}/edit                            | Action: edit             |
 |   POST   | /api/contact                                       | Action: save             |
 |   GET    | /api/contact                                       | Action: index            |
 |  DELETE  | /api/contact/${id}                                 | Action: delete           |
 |  PATCH   | /api/contact/${id}                                 | Action: patch            |
 |   PUT    | /api/contact/${id}                                 | Action: update           |
 |   GET    | /api/contact/${id}                                 | Action: show             |
----


=== Using CURL to test CRUD and List

Fire up the app with `run-app`

===== GET (list):
----
curl -i -X GET -H "Content-Type: application/json"  localhost:8080/api/contact
HTTP/1.1 200
X-Application-Context: application:development
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:30:31 GMT

[{"id":1,"email":"mscott0@ameblo.jp","firstName":"Marie","lastName":"Scott"},{"id":2,"email":"jrodriguez1@scribd.com","firstName":"Joseph","lastName":"Rodriguez"}, ...
----

===== POST:
----
curl -i -X POST -H "Content-Type: application/json" -d '{"firstName":"Joe", "lastName": "Cool"}' localhost:8080/api/contact
HTTP/1.1 201
X-Application-Context: application:development
Location: http://localhost:8080/api/contact/101
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:30:44 GMT

{"id":101,"firstName":"Joe","lastName":"Cool"}
----
===== GET (by id):
----
curl -i -X GET -H "Content-Type: application/json"  localhost:8080/api/contact/101
HTTP/1.1 200
X-Application-Context: application:development
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:31:00 GMT

{"id":101,"firstName":"Joe","lastName":"Cool"}
----

===== PUT:
----
curl -i -X PUT -H "Content-Type: application/json" -d '{"firstName": "New Name", "lastName": "New Last name"}' localhost:8080/api/contact/101
HTTP/1.1 200
X-Application-Context: application:development
Location: http://localhost:8080/api/contact/101
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:32:01 GMT

{"id":101,"firstName":"New Name","lastName":"New Last name"}
----

===== DELETE:
----
curl -i -X DELETE -H "Content-Type: application/json"  localhost:8080/api/contact/50
HTTP/1.1 204
X-Application-Context: application:development
Date: Mon, 31 Jul 2017 12:32:24 GMT
----

===== 422 - Post Validation Error:
----
curl -i -X POST -H "Content-Type: application/json" -d '{"lastName": "Cool"}' localhost:8080/api/contact
HTTP/1.1 422
X-Application-Context: application:development
Content-Type: application/json;charset=UTF-8
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:32:41 GMT

{"message":"Property [firstName] of class [class resttutorial.Contact] cannot be null","path":"/contact/index","_links":{"self":{"href":"http://localhost:8080/contact/index"}}}
----

===== 404 - Get Error:
----
curl -i -X GET -H "Content-Type: application/json"  localhost:8080/api/contact/105
HTTP/1.1 404
X-Application-Context: application:development
Content-Type: application/json;charset=UTF-8
Content-Language: en-US
Transfer-Encoding: chunked
Date: Mon, 31 Jul 2017 12:32:55 GMT

{"message":"Not Found","error":404}
----

===== 406 - NOT_ACCEPTABLE:

We did not setup XML support so we will get a 406. You may try adding XML to formats to see if this.
----
curl -i -X GET -H "Accept: application/xml"  http://localhost:8080/api/contact/8
HTTP/1.1 406
X-Application-Context: application:development
Content-Length: 0
Date: Mon, 31 Jul 2017 12:33:13 GMT
----

=== Functional Tests for the API

The next step is to add functional tests for our app. One option is to use Grails functional tests and RestBuilder.
We will cover another javscript option later the angle-grinder section
The line in the buidl.gradle that allows us to use RestBuilder is
----
testCompile "org.grails:grails-datastore-rest-client"
----

it is added by default when you create a grails app with `-profile rest-api`

==== POST testing example

Here is an example of `POST` request (creating of a new contact).
RestBuilder we use to emulate request from external source. Note, in Grails3 integration tests run on the random port,
so you cant call `http://localhost:8080/api/contact` , but we can use `serverPort` variable instead. And to make it more
intelligent lets use baseUrl. See example:

.ContactSpec.groovy
```groovy
package resttutorial

import grails.plugins.rest.client.RestBuilder
import grails.plugins.rest.client.RestResponse
import grails.test.mixin.integration.Integration
import org.grails.web.json.JSONElement
import spock.lang.Shared
import spock.lang.Specification

@Integration
class ContactSpec extends Specification {

    @Shared
    RestBuilder rest = new RestBuilder()

    def getBaseUrl(){"http://localhost:${serverPort}/api"}

    void "check POST request"() {
        when:
        RestResponse response = rest.post("${baseUrl}/contact"){
          json([
            firstName: "Test contact",
            email:"foo@bar.com",
            inactive:true //is bindable: false - see domain, so it wont be set to contact
          ])
        }

        then:
        response.status == 201
        JSONElement json = response.json
        json.id == 101
        json.firstName == "Test contact"
        json.lastName == null
        json.email == "foo@bar.com"
        json.inactive == null
    }
}
```

More tests examples are in the snapshot1 project's
{url-snapshot1}/src/integration-test/groovy/resttutorial/ContactSpec.groovy[ContactSpec.groovy]

=== GSON and Grails Views Defaults

As you can see by inspecting the views directory, by default Grails creates a number of gson files. Support for them is
provided with http://views.grails.org/latest/#_introduction[Grails Views Plugin]

The obvious question how does it work. If you look at sources of the RestfullController it doesn't "call" this templates
explicitly. So under the hood plugin just looks on request, if url ends on `.json`(localhost:8080/api/contact/1.json) or if
`Accept` header containing `application/json` the .gson view will be rendered.

If you delete default generated templates, then it will show default Grails page. Go ahead and try to delete `notFound.gson`
and try

----
curl -i -X GET -H "Content-Type: application/json"  localhost:8080/api/contact/105
HTTP/1.1 404
X-Application-Context: application:development
Content-Type: text/html;charset=utf-8
Content-Language: en-US
Content-Length: 990
Date: Mon, 31 Jul 2017 12:34:06 GMT

<!DOCTYPE html><html><head><title>Apache Tomcat/8.5.5 - Error report</title><style type="text/css">H1 ...
----

===== error.gson
{url-snapshot1}/grails-app/views/error.gson[See source]

This is for internal server errors. As you can see this is where the 500 status code gets set, and error message is specified.

It is called when we get `500` error, the same as for `gsp` look at UrlMapping: `"500"(view: '/error')`

===== notFound.gson
{url-snapshot1}/grails-app/views/notFound.gson[See source]
This is for case when resource isn't found. As you can see this is where the 404 status code gets set, and error message is specified.

It is called when we get `404` error, the same as for `gsp` look at UrlMapping: `"404"(view: '/notFound')`

===== errors/_errors.gson
{url-snapshot1}/grails-app/views/errors/_errors.gson[See source]
This is for validation errors. As you can see this is where the `UNPROCESSABLE_ENTITY`(422) status code gets set, and
error messages for entity specified.

It is rendered on {src-grails-rest}/src/main/groovy/grails/rest/RestfulController.groovy#L99[see src]
so if entity has errors it will look for `views/contact/_errors.gson` and if it doesn't exist then `views/errors/_errors.gson`

You can read more about defaults http://views.grails.org/latest/#_content_negotiation[here]

===== object/_object.gson
{url-snapshot1}/grails-app/views/object/_object.gson[See source]
This is for transforming entity to JSON object.

The rendering of this template is called for example here: {src-grails-rest}/src/main/groovy/grails/rest/RestfulController.groovy#L114[Save method]
So by convention if you have  `views/contact/_contact.gson` it will render it, in other case `views/object/_object.gson`,
which just render object as Json, so if we delete it it will still work in the same way because `respond instance` make
the same.


So all this files are default tempaltes for rendering in JSON all types of the responses and before delete them we need
to implement our own gson templates.

=== Snapshot 1 of this tutorial is at this point

[grails docs]: http://docs.grails.org/3.2.11/guide
[grails-api]: http://docs.grails.org/3.2.11/api
[grails ws docs]: http://docs.grails.org/3.2.11/guide/webServices.html
[src-grails-rest]: https://github.com/grails/grails-core/blob/master/grails-plugin-rest
