
see:
https://zalando.github.io/restful-api-guidelines/tooling/Tooling.html

## API as a Product

We are transforming to a platform comprising a rich set of products following a Software as a Platform (SaaS) model for our business partners. We want to deliver products to our customers which can be consumed like a service.

Platform products provide their functionality via (public) APIs; hence, the design of our APIs should be based on the API as a Product principle:

- Treat your API as product and understand the needs of its customers
- Take ownership and advocate for the customer and continuous improvement
- Emphasize easy understanding, discovery and usage of APIs; design APIs irresistible for client engineers
- Actively improve and maintain API consistency over the long term
- Make use of customer feedback and provide service level support
- RESTful API as a Product makes the difference between enterprise integration business and agile, innovative product service business built on a platform of APIs.
- are easy to understand and learn
- are general and abstracted from specific implementation and use cases
- have a common look and feel
- follow a consistent RESTful style and syntax

Based on your concrete customer use cases, you should carefully check the trade-offs of API design variants and avoid short-term server side implementation optimizations at the expense of unnecessary client side obligations and have a high attention on API quality and client developer experience.

API as a Product is closely related to our API First principle (see next chapter) which is more focussed on how we engineer high quality APIs.

## API Design Principles

REST is centered around business (data) entities exposed as resources that are identified via URIs and can be manipulated via standardized CRUD-like methods using different representations, self-descriptive messages and hypermedia. RESTful APIs tend to be less use-case specific and comes with less rigid client / server coupling and are more suitable as a platform interface being open for diverse client applications.

- We prefer REST-based APIs with JSON payloads
- We prefer systems to be truly RESTful
- We apply the RESTful web service principles to all kind of application components, whether they provide functionality via the Internet or via the intranet as larger application elements.
- We strive to build interoperating distributed systems that different teams can evolve in parallel.

An important principle for (RESTful) API design and usage is Postel's Law, aka the Robustness Principle

- Be liberal in what you accept, be conservative in what you send

Readings: Read the following to gain additional insight on the RESTful service architecture paradigm and general RESTful API design style:

## Security

TODO

## JSON Guidelines

## Property Names

- be consistent
- property names must be camelCase
- Property names must be an ASCII subset. The first character must be a letter, an underscore or a dollar sign, and subsequent characters can be a letter, an underscore, hyphen or a number.
- Array and collection names should be pluralized
- Use Consistent Property Values
- Boolean property values must not be null and have a default, which means they are always shown but never required
- Null values should have their fields removed
  Swagger/OpenAPI, which is in common use, doesn't support null field values (it does allow omitting that field completely if it is not marked as required). However that doesn't prevent clients and servers sending and receiving those fields with null values. Also, in some cases null may be a meaningful value - for example, JSON Merge Patch RFC 7382) using null to indicate property deletion.

- Empty array values should not be null, they can be represented as the the empty list, [].
- Enumerations should be represented as Strings
- Date and date-time property values should conform to RFC 3399
- for "date" use strings matching date-fullyear "-" date-month "-" date-mday, for example: 2015-05-28
- for "date-time" use strings matching full-date "T" full-time, for example 2015-05-28T14:07:17Z
- A zone offset may be used (both, in request and responses) -- this is simply defined by the standards. However, we encourage restricting dates to UTC and without offsets. For example 2015-05-28T14:07:17Z rather than 2015-05-28T14:07:17+00:00. From experience we have learned that zone offsets are not easy to understand and often not correctly handled. Note also that zone offsets are different from local times that might be including daylight saving time. Localization of dates should be done by the services that provide user interfaces, if required.

- When it comes to storage, all dates should be consistently stored in UTC without a zone offset. Localization should be done locally by the services that provide user interfaces, if required.

- Schema based JSON properties that are by design durations and intervals could be strings formatted as recommended by ISO 8601 (Appendix A of RFC 3399 contains a grammar for durations).

- Standards should be used for Language, Country and Currency

ISO 3166-1-alpha2 country (It's "GB", not "UK",
ISO 639-1 language code
BCP-47 (based on ISO 639-1) for language variants
ISO 4217 currency codes

## Naming

- Use lowercase separate words with hyphens for URI Path Segments

Example:

/shipment-orders/{shipment-order-id}
This applies to concrete path segments and not the names of path parameters. For example {shipment_order_id} would be ok as a path parameter.

Must: Use snake_case (never camelCase) for Query Parameters

Examples:

customer_number, order_id, billing_address
Must: Use Hyphenated HTTP Headers

Should: Prefer Hyphenated-Pascal-Case for HTTP header Fields

This is for consistency in your documentation (most other headers follow this convention). Avoid camelCase (without hyphens). Exceptions are common abbreviations like “ID.”

Examples:

Accept-Encoding
Apply-To-Redirect-Ref
Disposition-Notification-Options
Original-Message-ID
See also: HTTP Headers are case-insensitive (RFC 7230).

May: Use Standardized Headers

Use this list and mention its support in your OpenAPI definition.

Must: Pluralize Resource Names

Usually, a collection of resource instances is provided (at least API should be ready here). The special case of a resource singleton is a collection with cardinality 1.

May: Use /api as first Path Segment

In most cases, all resources provided by a service are part of the public API, and therefore should be made available under the root “/” base path. If the service should also support non-public, internal APIs — for specific operational support functions, for example — add “/api” as base path to clearly separate public and non-public API resources.

Must: Avoid Trailing Slashes

The trailing slash must not have specific semantics. Resource paths must deliver the same results whether they have the trailing slash or not.

May: Use Conventional Query Strings

If you provide query support for sorting, pagination, filtering functions or other actions, use the following standardized naming conventions:

q — default query parameter (e.g. used by browser tab completion); should have an entity specific alias, like sku
limit — to restrict the number of entries. See Pagination section below. Hint: You can use size as an alternate query string.
cursor — key-based page start. See Pagination section below.
offset — numeric offset page start. See Pagination section below. Hint: In combination with limit, you can use page as an alternative to offset.
sort — comma-separated list of fields to sort. To indicate sorting direction, fields my prefixed with + (ascending) or - (descending, default), e.g. /sales-orders?sort=+id
fields — to retrieve a subset of fields. See Support Filtering of Resource Fields below.
embed — to expand embedded entities (ie.: inside of an article entity, expand silhouette code into the silhouette object). Implementing “expand” correctly is difficult, so do it with care. See Embedding resources for more details.

[sourced from  here](http://blog.restcase.com/5-basic-rest-api-design-guidelines/)
> Names and Verbs
To describe your resources, use concrete names and not action verbs.
For decades, computer scientists used action verbs in order to expose services in an RPC way, for instance:
getUser(1234) createUser(user) deleteAddress(1234)
>
By contrast, the RESTful approach is to use:
GET /users/1234 POST /users (with JSON describing a user in the body) DELETE /addresses/1234
>
URI case
When it comes to naming resources in a program, there are 3 main types of case conventions: CamelCase, snake_case, and spinal-case. They are just a way of naming the resources to resemble natural language, while avoiding spaces, apostrophes and other exotic characters. This habit is universal in programming languages where only a finite set of characters is authorized for names.
>
CamelCase
>
CamelCase has been popularized by the Java language. It intends to emphasize the beginning of each word by making the first letter uppercase. E.g. camelCase, currentUser, etc. Aside from debates about its readability, its main drawback is to be ineffective in contexts which are not case sensitive.
>
snake_case
>
snakecase has been widely used for years by C programmers, and more recently in Ruby. Words are separated by underscores “”, thus letting a compiler or an interpreter understand it as a single symbol, but also allowing readers to separate words fluently. However, its popularity has decreased due to a lot of abuses in C programs with over-extended or too short names. Unlike camel case, there are very few contexts where snake case is not usable. Examples: snakecase, currentuser, etc.
>
spinal-case
>
spinal-case is a variant of snake case which uses hyphens “-” to separate words. The pros and cons are quite similar to those of snake case, with the exception that some languages do not allow hyphens in symbol names (for variable, class, or function naming). You may find it referred to as lisp-case because it is the usual way to name variables and functions in lisp dialects. It is also the traditional way of naming folders and files in UNIX and Linux systems. Examples: spinal-case, current-user, etc.
>
According to RFC3986, URLs are “case sensitive” (except for the scheme and the host).
In practice, though, a sensitive case may create dysfunctions with APIs hosted on a Windows system.
