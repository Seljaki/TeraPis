package si.seljaki

import org.locationtech.jts.geom.*
import org.locationtech.jts.operation.valid.IsSimpleOp
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.DurationUnit


fun calculateArea(coordinates: MutableList<Pair<Double, Double>>): Double {
    // Preveri, če ima seznam vsaj tri točke (poligon mora imeti vsaj tri točke)
    if (coordinates.size < 3) {
        throw IllegalArgumentException("A polygon must have at least 3 points")
    }

    var area = 0.0
    val n = coordinates.size

    // Izračun površine s Shoelace theorem
    for (i in 0 until n - 1) {
        val (x1, y1) = coordinates[i]
        val (x2, y2) = coordinates[i + 1]
        area += x1 * y2 - x2 * y1
    }

    // Zadnja iteracija za zaprtje poligona
    val (xLast, yLast) = coordinates[n - 1]
    val (xFirst, yFirst) = coordinates[0]
    area += xLast * yFirst - xFirst * yLast

    // Površina je absolutna vrednost polovične vsote
    area = kotlin.math.abs(area) / 2.0

    // Vrnitev površine v kvadratnih metrih
    return area
}






fun isPointInPolygon(point: Pair<Double, Double>, polygon: List<Pair<Double, Double>>): Boolean {
    var result = false
    val n = polygon.size
    var j = n - 1
    for (i in 0 until n) {
        if ((polygon[i].second > point.second) != (polygon[j].second > point.second) &&
            (point.first < (polygon[j].first - polygon[i].first) * (point.second - polygon[i].second) / (polygon[j].second - polygon[i].second) + polygon[i].first)) {
            result = !result
        }
        j = i
    }
    return result
}

fun isWorkInPlot(work: Work, plot: Plot): Boolean {
    if (work.plot == plot.name)
        return true

    if (work.path == null)
        return false

    for (point in work.path!!) {
        if(isPointInPolygon(Pair(point.first, point.second), plot.coordinates)) {
            println("WORK IS IN PLOT POLYGON")
            return true
        }
    }

    return false
}

fun generateBoustrophedonPath(coordinates: List<Pair<Double, Double>>, width: Double): List<Pair<Double, Double>> {
    if (coordinates.size < 4) throw IllegalArgumentException("At least four coordinates needed to define a field")

    // Find the min and max coordinates to define the bounding box
    val minX = coordinates.minByOrNull { it.first }!!.first
    val maxX = coordinates.maxByOrNull { it.first }!!.first
    val minY = coordinates.minByOrNull { it.second }!!.second
    val maxY = coordinates.maxByOrNull { it.second }!!.second

    val path = mutableListOf<Pair<Double, Double>>()
    var currentX = minX
    var moveRight = true

    while (currentX <= maxX) {
        if (moveRight) {
            path.add(Pair(currentX, minY))
            path.add(Pair(currentX, maxY))
        } else {
            path.add(Pair(currentX, maxY))
            path.add(Pair(currentX, minY))
        }
        currentX += width
        moveRight = !moveRight
    }

    return path
}

fun hasIntersectingEdges(coordinates: List<Pair<Double, Double>>): Boolean {
    if (coordinates.size < 4 || coordinates.first() != coordinates.last()) {
        // A polygon must have at least 4 points, and the first and last point must be the same
        return true
    }

    val geometryFactory = GeometryFactory()
    val jtsCoordinates = coordinates.map { Coordinate(it.first, it.second) }.toTypedArray()

    val linearRing: LinearRing = geometryFactory.createLinearRing(jtsCoordinates)
    val polygon: Polygon = geometryFactory.createPolygon(linearRing)

    // Check if the polygon is simple (has no self-intersections)
    val isSimple = IsSimpleOp(polygon).isSimple

    // If the polygon is not simple, it means there are intersecting edges
    return !isSimple
}



fun calculateAverageSpeed(coordinates: List<Triple<Double, Double, LocalDateTime>>): Double {
    if (coordinates.size < 2) {
        throw IllegalArgumentException("At least two coordinates are needed to calculate speed")
    }

    var totalDistance = 0.0
    var totalTime = 0.0

    for (i in 1 until coordinates.size) {
        val (lat1, lon1, time1) = coordinates[i - 1]
        val (lat2, lon2, time2) = coordinates[i]

        // Convert latitudes and longitudes from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Haversine formula to calculate the distance between two points on the Earth
        val dlat = lat2Rad - lat1Rad
        val dlon = lon2Rad - lon1Rad
        val a = sin(dlat / 2).pow(2) + cos(lat1Rad) * cos(lat2Rad) * sin(dlon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val earthRadiusKm = 6371.0
        val distance = earthRadiusKm * c

        totalDistance += distance

        // Calculate the time difference in hours
        val duration = ChronoUnit.MINUTES.between(time1, time2) / 60.0
        totalTime += duration
    }

    return if (totalTime > 0) totalDistance / totalTime else 0.0
}


fun createTractorPath(polygon: List<Pair<Double, Double>>, implementWidth: Double, maxSpeed: Double): List<Triple<Double, Double, LocalDateTime>> {
    // Check for valid input
    if (polygon.size < 3) {
        throw IllegalArgumentException("Polygon must have at least 3 points")
    }

    val offsetPolygon = createOffsetPolygon(polygon, implementWidth / 2.0)

    // Generate timestamps based on desired speed and path length
    val pathLength = calculatePathLength(offsetPolygon)
    val timestamps = generateTimestamps(pathLength, maxSpeed)

    // Combine offset polygon coordinates with timestamps
    val path = mutableListOf<Triple<Double, Double, LocalDateTime>>()
    for (i in offsetPolygon.indices) {
        val point = offsetPolygon[i]
        val timestamp = timestamps[i % timestamps.size] // Wrap around for cyclic path
        path.add(Triple(point.first, point.second, timestamp))
    }
    return path
}

fun addTimestamp(points: List<Pair<Double, Double>>): List<Triple<Double, Double, LocalDateTime>> {
    val pointsTime: MutableList<Triple<Double, Double, LocalDateTime>> = mutableListOf()
    for(p in points)
        pointsTime.add(Triple(p.first, p.second, LocalDateTime.now()))

    return pointsTime
}

// Function to create a buffer polygon with specified offset distance
private fun createOffsetPolygon(polygon: List<Pair<Double, Double>>, offset: Double): List<Pair<Double, Double>> {
    // Implement an offset algorithm here (e.g., using libraries like JTS)
    // This example assumes a simple inward buffer by shifting each point slightly inwards
    // Replace with a proper offset implementation
    return polygon.map { point ->
        val (lat, lon) = point
        val newLat = lat - offset * Math.sin(Math.toRadians(lat))
        val newLon = lon + offset * Math.cos(Math.toRadians(lat))
        Pair(newLat, newLon)
    }
}

// Function to calculate the total path length of the polygon
private fun calculatePathLength(polygon: List<Pair<Double, Double>>): Double {
    val distances = polygon.zipWithNext { (lat1, lon1), (lat2, lon2) ->
        val deltaLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        val radiusEarth = 6371e3  // Earth radius in meters (customize if needed)
        radiusEarth * c
    }
    return distances.sum()
}

// Function to generate timestamps based on path length and speed
private fun generateTimestamps(pathLength: Double, speed: Double): List<LocalDateTime> {
    if (speed <= 0.0) {
        throw IllegalArgumentException("Speed cannot be zero or negative")
    }
    val travelTime = pathLength / speed

    val startTime = LocalDateTime.now()
    val timestamps = mutableListOf(startTime)
    var currentTime = startTime
    while (currentTime.plusNanos((travelTime * 1000 * 60 * 60).nanoseconds.toLong(DurationUnit.NANOSECONDS)) < LocalDateTime.now()) {
        currentTime = currentTime.plusNanos((travelTime * 1000 * 60 * 60).nanoseconds.toLong(DurationUnit.NANOSECONDS))
        timestamps.add(currentTime)
    }
    return timestamps
}

fun calculateCoveredArea(path: List<Triple<Double, Double, LocalDateTime>>, implementWidth: Double): Double {
    if (path.size < 3) {
        throw IllegalArgumentException("Path must have at least 3 points")
    }

    val offsetPolygon = path.map { it.first to it.second }.toSet() // Extract coordinates as a Set

    val shoelaceFormula = offsetPolygon.zipWithNext { p1, p2 ->
        p1.first * p2.second - p2.first * p1.second
    }.sum()

    val area = Math.abs(shoelaceFormula / 2.0)

    return area * implementWidth
}

fun getDifferenceInSeconds(dateTime1: LocalDateTime, dateTime2: LocalDateTime): Long {
    return ChronoUnit.MILLIS.between(dateTime1, dateTime2)
}