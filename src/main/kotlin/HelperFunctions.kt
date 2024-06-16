package si.seljaki

import java.text.SimpleDateFormat
import kotlin.math.abs



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