
This can be used as a template to start a good intro to a products API docs

## Reference

The API is organized around REST. The API has predictable, resource-oriented URLs, and uses HTTP response codes to indicate API errors. We use built-in HTTP features, like HTTP authentication and HTTP verbs, which are understood by off-the-shelf HTTP clients. We support cross-origin resource sharing, allowing you to interact securely with our API from a client-side web application (though you should never expose your secret API key in any public website's client-side code). JSON is returned by all API responses, including errors.
To make an API as explorable as possible, users can have test mode and live mode API keys. There is no "switch" for changing between modes, just use the appropriate key to perform a live or test transaction. Requests made with test mode credentials never hit production

An operation is a unit of a REST API that you can call. An operation comprises an HTTP verb and a URL path that is subordinate to the context root of the API. By configuring the operation, you define how the API is exposed to your developers.

## Authentication
TODO

## Schema

### HTTP Verbs

The URLs are expected to following normal REST conventions.

| HTTP Method | Ctrl Action |                                 Purpose                                 |
|:-----------:| ----------- |:----------------------------------------------------------------------- |
|     GET     | index       | Read a resource or list of resources                                    |
|    POST     | update      | Create a new resource (when the key is not known a-priori) See note. |
|     PUT     | save        | Update an existing resource or create one if the key is pre-defined     |
|   DELETE    | Remove      | Remove a resource                                                       |

### ENDPOINTS AND ACTIONS

|  Endpoint  |      Action       | HTTP Verbs |                           Returns                            | code |
| ---------- | ----------------- | ---------- | ------------------------------------------------------------ | ---- |
| /thing     | list(params)      | GET        | Array - a paginated array of things                       | 200  |
| /thing/123 | show(id)         | GET        | Object - <br>  one thing where id=123          | 200  |
| /thing     | insert(body)      | POST       | Object - <br> Inserts a new thing and returns it                | 201  |
| /thing/123 | update(id, body) | PUT        | Object - <br>  update and return the thing where id=123         | 200  |
| /thing/123 | delete(id)       | DELETE     | nothing <br>  Deletes the thing where id=123 | 204  |
|            |                   | PATCH      | TODO                                                         | 200  |


### Idempotent Requests

The API supports [idempotency] for safely retrying requests without accidentally performing the same operation twice. For example, if a request to create a doodad fails due to a network connection error, you can retry the request with the same idempotency key to guarantee that only a single charge is created.
GET and DELETE requests are idempotent by definition, meaning that the same backend work will occur no matter how many times the same request is issued. You shouldn't send an idempotency key with these verbs because it will have no effect.
To perform an idempotent request, provide an additional `Idempotency-Key: <key>` header to the request.

  [idempotency]: https://en.wikipedia.org/wiki/Idempotence

## responses

### Success Status Codes

   Status Code    |                        Description                         
 ---------------- | ----------------------------------------------------------
 200 - OK         | Everything worked as expected. default                     
 201 - CREATED    | Resource/instance was created. returned from `save` action
 204 - NO_CONTENT | response code on successful DELETE request                 
 404 - NOT_FOUND  | The requested resource doesn't exist.                      
 422              | Validation errors.
 405 - METHOD_NOT_ALLOWED | If method (GET,POST,etc..) is not setup in `static allowedMethods` for action or resource is read only
 406 - NOT_ACCEPTABLE  | Accept header requests a response in an unsupported format. not configed in mime-types. RestResponder uses this

### Response Body.

Any response body will be one of the following:

* A representation of a resource
* An array of representations of a resource (either a JSON array, or list representation in xml)
* an empty body

Any 'envelope' information is conveyed in headers.

### Pagination

Because large data sets are possible, paging is used for all GET index actions.
The default page size is set to 100. 9ci will return four response headers along with the result set.

|          Header           |                  Description                  |
| ------------------------- | --------------------------------------------- |
| X-Pagination-Limit        | The per page size limit. Defaults to 100           |
| X-Pagination-Current-Page | The current page. Defaults to 1.              |
| X-Pagination-Total-Pages  | The total number of pages in the result list.  |
| X-Pagination-Total-Count  | The total number of items across all pages. |

To retrieve data for a specific page, simply specify the page query parameter `/doodad?page=5`.

| Query parameters |                                         Description                                         |
| ---------------- | ------------------------------------------------------------------------------------------- |
| page             | The page number to show                                                                     |
| limit or max     | The number of items in the result list to show per page                                     |
| offset           | The item number to start from (zero based) in the result list. Don't use both this and page |

Pages start at 1 of course. Any value less than 1 will default to the first page while any value greater than Pagination-Total-Pages will simply return an empty result set.

### Errors

Errors come in the form of HTTP response codes to indicate the success or failure of an API request. as listed as well as validation errors

Not all errors map cleanly onto HTTP response codes, however. When a request is valid but does not complete successfully (e.g., a card is declined), we return a 422 error code.

Status Code                |                        Description                         
-------------------------- | ----------------------------------------------------------                 
422 - UNPROCESSABLE_ENTITY | Validation errors.
404 - NOT_FOUND            | The requested resource doesn't exist.                      
405 - METHOD_NOT_ALLOWED   | If method (GET,POST,etc..) is not setup in `static allowedMethods` for action or resource is read only
406 - NOT_ACCEPTABLE       | Accept header requests a response in an unsupported format. not configed in mime-types. RestResponder uses this

### Validation Errors 422

If you try to create or update a record with invalid data, you'll receive a 422 response code and the operation will fail.

You'll also receive an errors object in the response body with the resulting error messages. This object will have one key for each field with errors.

Each field will have an array of human readable error messages, as show below:
```
TODO
```
