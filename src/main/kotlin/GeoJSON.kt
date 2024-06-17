package si.seljaki

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import java.time.LocalDateTime

fun convertPlotsAndWorkToGeoJson(plots: List<Plot>, works: List<Work>): String {
    val featureCollection = FeatureCollection(
        "FeatureCollection",
        features = mutableListOf()
    )

    // Add plots as GeoJSON features
    plots.forEach { plot ->
        val feature = Feature(
            "Feature",
            geometry = createPolygonGeometry(plot.coordinates),
            properties = mapOf(
                "name" to plot.name,
                "type" to plot.type.toString(),
                "area" to plot.area
            )
        )
        featureCollection.features.add(feature)
    }

    // Add works as GeoJSON features (LineString for path)
    works.forEach { work ->
        val feature = Feature(
            "Feature",
            geometry = createLineStringGeometry(work.path),
            properties = mapOf(
                "name" to work.name,
                "action" to work.action,
                "plot" to work.plot,
                "areaCovered" to work.areaCovered,
                "averageSpeed" to work.averageSpeed,
                "efficiency" to work.efficiency
            )
        )
        featureCollection.features.add(feature)
    }

    // Convert FeatureCollection to GeoJSON string
    val mapper = ObjectMapper()
    return try {
        mapper.writeValueAsString(featureCollection)
    } catch (e: JsonProcessingException) {
        "Error converting to GeoJSON: ${e.message}"
    }
}

// Function to create Polygon geometry from coordinates
private fun createPolygonGeometry(coordinates: List<Pair<Double, Double>>): Geometry {
    return Geometry(type = "Polygon", coordinates = listOf(coordinatesToArray(coordinates)))
}

// Function to create LineString geometry from path
private fun createLineStringGeometry(path: List<Triple<Double, Double, LocalDateTime>>?): Geometry? {
    if (path == null || path.isEmpty()) {
        return null
    }
    val lineStringCoordinates = path.map { it.first to it.second }
    return Geometry(type = "LineString", coordinates = coordinatesToArray(lineStringCoordinates))
}

fun coordinatesToArray(coordinates: List<Pair<Double, Double>>): List<Array<Double>> {
    val coords: MutableList<Array<Double>> = mutableListOf();
    for (c in coordinates)
        coords.add(arrayOf(c.first, c.second))
    return coords
}

// Data classes for GeoJSON (Feature, FeatureCollection, Geometry)
data class FeatureCollection(
    val type: String,
    val features: MutableList<Feature>
)

data class Feature(
    val type: String,
    val geometry: Geometry?,
    val properties: Map<String, Any?>?,
)

data class Geometry(
    val type: String,
    val coordinates: Any?
)
