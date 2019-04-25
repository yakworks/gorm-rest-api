package restify

class BootStrap {
    BookRepo bookRepo
    def init = { servletContext ->
        bookRepo.create([title: "Shine"])
        2.times{
        new Organisation(name: "Organisation#$it",
            num: "Organisation-num#$it",
            revenue: 100 * it,
            isActive: (it % 2 == 0),
            credit: (it % 2 ? 5000 : null),
            refId: it * 200 as Long,
            testDate: (new Date() + it).clearTime(),
            address: new ShipAddress(city: "City#$it", testId: it * 3).persist()).persist()}
    }
    def destroy = {
    }
}
