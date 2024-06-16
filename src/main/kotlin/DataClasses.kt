package si.seljaki

import java.time.LocalDateTime

interface Expr {
    fun eval(variables: MutableMap<String, Double>): Double
}

interface Statement {
    fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>)
}

enum class PlotType{
    FOREST, FIELD, UNDEFINED
}

data class Plot (
    var name: String,
    var coordinates: MutableList<Pair<Double, Double>> = mutableListOf(),
    var coordinatesExpr: CoordinatesExpr? = null,
    var type: PlotType = PlotType.UNDEFINED,
    var area: Double? = null,
)

data class Work (
    var name: String,
    var path: MutableList<Triple<Double, Double, LocalDateTime>> ?= null,
    var action: String ?= null,
    var maxSpeed: Double ?= null,
    var implementWidth: Double ?= null,
    var plot: String? = null,
    var areaCovered: Double? = null,
    var averageSpeed: Double? = null,
    var efficiency: Double? = null
)
