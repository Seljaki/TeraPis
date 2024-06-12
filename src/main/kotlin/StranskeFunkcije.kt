package si.seljaki

import java.text.SimpleDateFormat
import kotlin.math.abs


enum class PlotType{
    FOREST, FIELD, UNDEFINED
}
fun ifContains(coordinates: MutableList<Pair<Double, Double>>, path: MutableList<Pair<Double, Double>>):Boolean{
    val coordLength = coordinates.size
    val pathLength = path.size
    if (pathLength > coordLength) return false
    for (i in 0..(coordLength - pathLength)) {
        if (coordinates.subList(i, i + pathLength) == path) {
            return true
        }
    }

    return false
}
fun isValidCoordinates(coordinates: MutableList<Pair<Double, Double>>): Boolean {
    // Preveri, če ima seznam vsaj tri točke
    if (coordinates.size < 4) { // Za veljaven poligon potrebujemo vsaj tri točke in začetno enako končni točki
        return false
    }

    // Preveri, če je prva točka enaka zadnji točki (zapiranje poti)
    if (coordinates.first() != coordinates.last()) {
        return false
    }

    // Preverjanje, če točke tvorijo veljaven poligon brez presečišč
    // Za to potrebujemo preveriti vsako stranico s preostalimi
    for (i in 0 until coordinates.size - 1) {
        for (j in i + 2 until coordinates.size - 1) {
            if (i == 0 && j == coordinates.size - 2) continue // preskoči povezovanje prve in zadnje točke
            if (linesIntersect(coordinates[i], coordinates[i + 1], coordinates[j], coordinates[j + 1])) {
                return false
            }
        }
    }

    return true
}
fun linesIntersect(p1: Pair<Double, Double>, q1: Pair<Double, Double>,
                   p2: Pair<Double, Double>, q2: Pair<Double, Double>): Boolean {

    // Izračunaj orientacijo
    fun orientation(p: Pair<Double, Double>, q: Pair<Double, Double>, r: Pair<Double, Double>): Int {
        val value = (q.second - p.second) * (r.first - q.first) - (q.first - p.first) * (r.second - q.second)
        return when {
            value == 0.0 -> 0  // kolinearni
            value > 0 -> 1     // orijentacija v smeri urinega kazalca
            else -> 2          // orijentacija v nasprotni smeri urinega kazalca
        }
    }

    // Preveri, če se točke nahajajo na segmentu
    fun onSegment(p: Pair<Double, Double>, q: Pair<Double, Double>, r: Pair<Double, Double>): Boolean {
        return q.first <= maxOf(p.first, r.first) && q.first >= minOf(p.first, r.first) &&
                q.second <= maxOf(p.second, r.second) && q.second >= minOf(p.second, r.second)
    }

    val o1 = orientation(p1, q1, p2)
    val o2 = orientation(p1, q1, q2)
    val o3 = orientation(p2, q2, p1)
    val o4 = orientation(p2, q2, q1)

    if (o1 != o2 && o3 != o4) {
        return true
    }

    if (o1 == 0 && onSegment(p1, p2, q1)) return true
    if (o2 == 0 && onSegment(p1, q2, q1)) return true
    if (o3 == 0 && onSegment(p2, p1, q2)) return true
    if (o4 == 0 && onSegment(p2, q1, q2)) return true

    return false
}
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

fun calculatePath(plot: PlotExpr?, work: WorkExpr?) {
    if (plot == null || work == null) {
        println("Plot or work is null")
        return
    }
    work.path = plot.coordinates
    println("Calculated path: ${work.path}")
}

fun calculateEfficency(plot: PlotExpr?, work: WorkExpr?) {
    if (plot == null || work == null || work.timestamps.isEmpty()) {
        println("Plot, work or timestamps are null/empty")
        return
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val times = work.timestamps.map { dateFormat.parse(it) }
    val totalTime = times.zipWithNext { a, b -> (b.time - a.time) / 1000.0 }.sum()
    val averageWorkTime = totalTime / work.timestamps.size

    calculatePath(plot, work)
    val pathTime = work.path.size / work.maxSpeed
    val efficiency = pathTime / averageWorkTime

    println("Average work time: $averageWorkTime seconds")
    println("Path time: $pathTime seconds")
    println("Efficiency: $efficiency")
}

fun calculateAverageSpeed(work: WorkExpr?) {
    if (work == null || work.timestamps.isEmpty()) {
        println("Work or timestamps are null/empty")
        return
    }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    val times = work.timestamps.map { dateFormat.parse(it) }
    val totalTime = times.zipWithNext { a, b -> (b.time - a.time) / 1000.0 }.sum()
    val totalDistance = work.path.zipWithNext { a, b -> distance(a, b) }.sum()
    val averageSpeed = totalDistance / totalTime

    println("Average speed: $averageSpeed meters/second")
}

fun calculateAreaCovered(work: WorkExpr?) {
    if (work == null || work.path.size < 3) {
        println("Work or path is null/too short")
        return
    }

    val areaCovered = calculatePolygonArea(work.path)
    println("Area covered: $areaCovered square meters")
}

fun distance(p1: Pair<Double, Double>, p2: Pair<Double, Double>): Double {
    val (x1, y1) = p1
    val (x2, y2) = p2
    return Math.sqrt(Math.pow(x2 - x1, 2.0) + Math.pow(y2 - y1, 2.0))
}

fun calculatePolygonArea(coordinates: MutableList<Pair<Double, Double>>): Double {
    var area = 0.0
    val n = coordinates.size
    for (i in 0 until n - 1) {
        val (x1, y1) = coordinates[i]
        val (x2, y2) = coordinates[i + 1]
        area += x1 * y2 - x2 * y1
    }
    val (xLast, yLast) = coordinates[n - 1]
    val (xFirst, yFirst) = coordinates[0]
    area += xLast * yFirst - xFirst * yLast
    return abs(area) / 2.0
}
fun convertToGeoJSONString(env: Map<String, Any>): String {
    val features = mutableListOf<String>()
    val plots = env["plots"] as Map<String, PlotExpr>
    val works = env["works"] as Map<String, WorkExpr>
    for ((_, plot) in plots) {
        val coordinates = plot.coordinates.joinToString(", ") { "[${it.first}, ${it.second}]" }
        val polygon = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "Polygon",
                    "coordinates": [
                        [$coordinates]
                    ]
                },
                "properties": {
                    "name": ${plot.name},
                    "type": "${plot.type.name.toLowerCase()}"
                }
            }
        """.trimIndent()
        features.add(polygon)
    }
    for ((_, work) in works) {
        val coordinates = work.path.joinToString(", ") { "[${it.first}, ${it.second}]" }
        val lineString = """
            {
                "type": "Feature",
                "geometry": {
                    "type": "LineString",
                    "coordinates": [
                        $coordinates
                    ]
                },
                "properties": {
                    "action": ${work.action},
                    "max-speed": ${work.maxSpeed},
                    "implement-width": ${work.implementWidth},
                    "plot": "${work.plot}"
                }
            }
        """.trimIndent()
        features.add(lineString)
    }
    val geoJson = """
        {
            "type": "FeatureCollection",
            "features": [
                ${features.joinToString(",\n")}
            ]
        }
    """.trimIndent()

    return geoJson
}