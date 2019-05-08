package restify

import gorm.restapi.RestApi

import java.time.LocalDateTime

@RestApi(description = "Organisation domain")
class Organisation {
    String name
    String num
    ShipAddress address
    Date testDate
    LocalDateTime testDateTwo
    boolean isActive = true
    BigDecimal revenue = 0
    BigDecimal credit
    Long refId = 0L
    String event

    static getListFields(){
        ["*", "address.*"]
    }
    static quickSearchFields = ["name", "num"]
    static constraints = {
        name blank: false
        num nullable: true
        address nullable: true
        testDate nullable: true
        testDateTwo nullable: true
        credit nullable: true
        event nullable: true
    }
}
