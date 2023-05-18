package taxipark

/*
 * Task #1. Find all the drivers who performed no trips.
 */
/* Solution comment:
* I am doing mapTo to create a Set of drivers.
* Doing map would create a list with possible repetitions.
* */
fun TaxiPark.findFakeDrivers(): Set<Driver> =
    allDrivers.subtract(
        trips.mapTo(mutableSetOf()) { it.driver }
    )

/*
 * Task #2. Find all the clients who completed at least the given number of trips.
 */
fun TaxiPark.findFaithfulPassengers(minTrips: Int): Set<Passenger> {
    val allPassengersInitialMap = allPassengers.map { it to 0 }.toMap(mutableMapOf())

    val passengerToTripCount =
        trips
            .flatMap { trip -> trip.passengers }
            .groupingBy { it }
            .eachCountTo(allPassengersInitialMap)

    return passengerToTripCount
        .filterValues { it >= minTrips }
        .keys
}

/*
 * Task #3. Find all the passengers, who were taken by a given driver more than once.
 */
fun TaxiPark.findFrequentPassengers(driver: Driver): Set<Passenger> {
    val passengerOfDriverToTripCount =
        trips
            .filter { trip -> trip.driver == driver }
            .flatMap { trip -> trip.passengers }
            .groupingBy { it }
            .eachCount()

    return passengerOfDriverToTripCount
        .filterValues { it > 1 }
        .keys
}

/*
 * Task #4. Find the passengers who had a discount for majority of their trips.
 */
fun TaxiPark.findSmartPassengers(): Set<Passenger> {
    fun passengerToDiscount(trip: Trip) = trip.passengers.map { passenger -> passenger to trip.discount }

    return trips
        .flatMap { trip -> passengerToDiscount(trip) }
        .groupBy({ (passenger, _) -> passenger }, { (_, discount) -> discount })
        .mapValues { (_, discounts) -> discounts.partition { it == null } }
        .filterValues { (withoutDiscount, withDiscount) -> withDiscount.count() > withoutDiscount.count() }
        .keys
}


/*
 * Task #5. Find the most frequent trip duration among minute periods 0..9, 10..19, 20..29, and so on.
 * Return any period if many are the most frequent, return `null` if there're no trips.
 */
fun TaxiPark.findTheMostFrequentTripDurationPeriod(): IntRange? {
    fun findMostFrequentDuration(): IntRange {
        val entryWithMax = trips.map { trip -> trip.duration }
            .groupingBy { it / 10 }
            .eachCount()
            .maxBy { (_, count) -> count }

        val maxRangeStart = entryWithMax.key * 10
        val maxRangeEnd = maxRangeStart + 9

        return maxRangeStart..maxRangeEnd
    }

    return if (trips.isEmpty()) null else findMostFrequentDuration()
}

/*
 * Task #6.
 * Check whether 20% of the drivers contribute 80% of the income.
 */
fun TaxiPark.checkParetoPrinciple(): Boolean {
    fun checkPareto(): Boolean {
        val driversThreshold = (allDrivers.count() * 0.2).toInt()
        val incomeThreshold = trips.map(Trip::cost).sum() * 0.8

        val mostProfitableDriversIncome =
            trips
                .groupingBy { trip -> trip.driver }
                .fold(0.0, { driverIncome, trip -> driverIncome + trip.cost })
                .map { (_, driverIncome) -> driverIncome }
                .sortedDescending()
                .take(driversThreshold)
                .sum()

        return mostProfitableDriversIncome >= incomeThreshold
    }

    return if (trips.isEmpty()) false else checkPareto()
}

