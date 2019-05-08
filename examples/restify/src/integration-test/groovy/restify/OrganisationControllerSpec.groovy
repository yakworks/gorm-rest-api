package restify

import geb.spock.GebSpec
import gorm.restapi.testing.RestApiTestTrait
import grails.testing.mixin.integration.Integration

@Integration
class OrganisationControllerSpec extends GebSpec implements RestApiTestTrait {

    String getResourcePath() {
        "${baseUrl}api/organisation"
    }

    def "Check list"() {
        setup:
        100.times {
            new Organisation(name: "Organisation#$it",
                    num: "Organisation-num#$it",
                    revenue: 100 * it,
                    isActive: (it % 2 == 0),
                    credit: (it % 2 ? 5000 : null),
                    refId: it * 200 as Long,
                    testDate: (new Date() + it).clearTime(),
                    address: new ShipAddress(city: "City#$it", testId: it * 3).persist()).persist()
        }

        when:
        def response = restBuilder.get(resourcePath)
        then:
        response.json.data.size() == 10
    }

    def "Filter by Name eq"() {
        when:
        Map json = restBuilder.post(resourcePath + "/list") {
            json([criteria: [name: "Organisation#23"]])
        }.json
        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#23"
    }

    def "Filter by id eq"() {
        given:
        Map data = [criteria: [id: 24]]

        when:
        Map json = restBuilder.post(resourcePath + "/list") {
            json(data)
        }.json
        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#23"
    }

    def "Filter by id inList"() {
        given:
        Map data = [criteria: [id: [24, 25]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#23"
    }

    def "Filter by Name ilike"() {
        given:
        Map data = [criteria: [name: "Organisation#2%"]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 11
        json.data[0].name == "Organisation#2"
        json.data[1].name == "Organisation#20"
        json.data[10].name == "Organisation#29"
    }

    def "Filter by nested id"() {
        given:
        Map data = [criteria: [address: [id: 2]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#1"
        json.data[0].address.id == 2
    }

    def "Filter by nestedId"() {
        given:
        Map data = [criteria: ["address.id": 2]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#1"
        json.data[0].address.id == 2
    }

    def "Filter by nested id inList"() {
        given:
        Map data = [criteria: [address: [id: [24, 25, 26]]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 3
        json.data[0].name == "Organisation#23"
    }

    def "Filter by nested string"() {
        given:
        Map data = [criteria: [address: [city: "City#2"]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#2"
        json.data[0].address.id == 3
    }

    def "Filter by nested string ilike"() {
        given:
        Map data = [criteria: [address: [city: "City#2%"]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 11
        json.data[0].name == "Organisation#2"
        json.data[1].name == "Organisation#20"
        json.data[10].name == "Organisation#29"
        json.data[0].address.id == 3
        json.data[1].address.id == 21
        json.data[10].address.id == 30
    }

    def "Filter by boolean"() {
        given:
        Map data = [criteria: [isActive: true]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == Organisation.createCriteria().list() { eq "isActive", true }.size()
    }

    def "Filter by boolean in list"() {
        given:
        Map data = [criteria: [isActive: [false]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 50
        json.data[0].isActive == false
        json.data[1].isActive == false
    }

    def "Filter by BigDecimal"() {
        given:
        Map data = [criteria: [revenue: 200.0]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#2"
    }

    def "Filter by BigDecimal in list"() {
        given:
        Map data = [criteria: [revenue: [200.0, 500.0]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#2"
        json.data[1].name == "Organisation#5"
    }

    def "Filter by Date"() {
        given:
        Map data = [criteria: [testDate: (new Date() + 1).clearTime()]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#1"
    }

    def "Filter by Date le"() {
        given:
        Map data = [criteria: ['testDate.$lte': (new Date() + 1).clearTime()]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == Organisation.createCriteria().list() { le "testDate", (new Date() + 1).clearTime() }.size()
        json.data[0].name == Organisation.createCriteria().list() { le "testDate", (new Date() + 1).clearTime() }[0].name
    }

    def "Filter by xxxId 1"() {
        given:
        Map data = [criteria: [refId: 200]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#1"
    }

    def "Filter by xxxId 2"() {
        given:
        Map data = [criteria: ["address.testId": 9]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json
        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#3"
    }


    def "Filter by xxxId 3"() {
        given:
        Map data = [criteria: [address: [testId: 3]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 1
        json.data[0].name == "Organisation#1"
    }

    def "Filter by xxxId 4"() {
        given:
        Map data = [criteria: ["address.testId": [9, 12]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#3"
    }


    def "Filter with `or` "() {
        given:
        Map data = [criteria: ['$or': ["name": "Organisation#1", "address.id": 4]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#1"
        json.data[1].name == "Organisation#3"
    }

    def "Filter with `or` on low level"() {
        given:
        Map data = [criteria: [address: ['$or': ["city": "City#1", "id": 4]]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#1"
        json.data[1].name == "Organisation#3"
    }

    def "Filter with `or` with like"() {
        given:
        Map data = [criteria: ["\$or": ["name": "Organisation#2%", "address.id": 4]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 12
        json.data[0].name == "Organisation#2"
        json.data[1].name == "Organisation#3"
        json.data[2].name == "Organisation#20"
    }

    def "Filter with `between()`"() {
        given:
        Map data = [criteria: [id: ["\$between": [2, 10]]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 9
        json.data[0].name == "Organisation#1"
        json.data[1].name == "Organisation#2"
        json.data[-1].name == "Organisation#9"
    }

    def "Filter with `in()`"() {
        given:
        Map data = [criteria: [id: ["\$in": [24, 25]]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#23"
    }

    def "Filter with `inList()`"() {
        given:
        Map data = [criteria: [id: ["\$inList": [24, 25]]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 2
        json.data[0].name == "Organisation#23"
    }


    def "Filter by Name ilike()"() {
        given:
        Map data = [criteria: [name: ["\$ilike": "Organisation#2%"]]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json

        then:
        json.data.size() == 11
        json.data[0].name == "Organisation#2"
        json.data[1].name == "Organisation#20"
        json.data[10].name == "Organisation#29"
    }

    def "test paging, defaults"() {
        when:
        Map json = restBuilder.post(resourcePath + "/list").json

        then:
        json.data.size() == 10
    }

    def "test paging"() {
        when:
        List list = restBuilder.get(resourcePath + "/list?max=20").json
        then:
        json.data.size() == 20
        json.data[0].id == 1

        when:
        list = restBuilder.get(resourcePath + "/list?page=2").json
        then:
        json.data.size() == 10
        json.data[0].id == 11

    }

    def "test quick search"() {
        given:
        Map data = [criteria: ['$quickSearch': "Organisation#2%"]]

        when:
        Map json = restBuilder.post(resourcePath + "/list?max=150") {
            json(data)
        }.json
        then:
        json.data.size() == 11

    }

}
