package si.seljaki

import java.time.LocalDateTime
import kotlin.math.pow


class CalculatePathFunction(val workName: String, val plotName: String): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val plot = plots[plotName]
        val work = works[workName]
        if(work == null || plot == null || work.implementWidth == null || work.maxSpeed == null)
            return

        //work.path = createTractorPath(plot.coordinates, work.implementWidth!!, work.maxSpeed!!).toMutableList()
        work.path = addTimestamp(generateBoustrophedonPath(plot.coordinates, work.implementWidth!! / 70000)).toMutableList()
        work.plot = plotName
    }
}

class CalculateAreaFunction(val plotName: String): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val plot = plots[plotName] ?: return

        plot.area = calculateArea(plot.coordinates)
    }
}

class CalculateAreaCoveredFunction(val workName: String): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val work = works[workName] ?: return
        if(work.path == null || work.implementWidth == null)
            return

        work.areaCovered = calculateCoveredArea(work.path!!, work.implementWidth!!)
    }
}

class CalculateAverageSpeedFunction(val workName: String): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val work = works[workName] ?: return
        if (work.path == null)
            return

        work.averageSpeed = calculateAverageSpeed(work.path!!)
    }
}

class CalculateEfficiencyFunction(val workName: String, val plotName: String): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val plot = plots[plotName]
        val work = works[workName]
        if(work == null || plot == null || work.path == null)
            return

        val bestPath = createTractorPath(plot.coordinates, work.implementWidth!!, work.maxSpeed!!)
        val originalTime = getDifferenceInSeconds(work.path!![0].third, work.path!!.last().third)
        val newTime = getDifferenceInSeconds(bestPath[0].third, bestPath.last().third)

        work.efficiency = (newTime / originalTime).toDouble() * 100.0
    }
}

class IfContainsExpr(val workName: String, val plotName: String, val statements: List<Statement>): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val plot = plots[plotName]
        val work = works[workName]
        if(work == null || plot == null)
            return

        if (isWorkInPlot(work, plot)) {
            for (st in statements) {
                st.eval(plots, works, variables)
            }
        }
    }
}

class IfPlotIsValidExpr(val plotName: String, val statements: List<Statement>): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        val plot = plots[plotName] ?: return
        if (!hasIntersectingEdges(plot.coordinates)) {
            println("PLOT IS VALID!!!")
            for (st in statements) {
                st.eval(plots, works, variables)
            }
        }
    }
}

class CoordinatesExpr(val points: MutableList<Pair<Expr, Expr>>) {
    fun eval(variables: MutableMap<String, Double>): MutableList<Pair<Double, Double>> {
        val coordinates: MutableList<Pair<Double, Double>> = mutableListOf()

        for (point in points) {
            coordinates.add(Pair(point.first.eval(variables), point.second.eval(variables)))
        }

        return coordinates
    }
}

class PlotDefinitionExpr(val plot: Plot): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        if(plot.coordinatesExpr != null) {
            plot.coordinates = plot.coordinatesExpr!!.eval(variables)
            plots[plot.name] = plot
        }
    }
}

class PathExpr(val points: MutableList<Triple<Expr, Expr, LocalDateTime>>) {
    fun eval(variables: MutableMap<String, Double>): MutableList<Triple<Double, Double, LocalDateTime>> {
        val coordinates: MutableList<Triple<Double, Double, LocalDateTime>> = mutableListOf()

        for (point in points) {
            coordinates.add(Triple(point.first.eval(variables), point.second.eval(variables), point.third))
        }

        return coordinates
    }
}

class WorkDefinitionExpr (
    var name: String,
    var pathExpr: PathExpr? = null,
    var action: String ?= null,
    var maxSpeed: Expr ?= null,
    var implementWidth: Expr ?= null,
    var plot: String? = null,
): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        works[name] = Work(
            name,
            pathExpr?.eval(variables),
            action,
            maxSpeed?.eval(variables),
            implementWidth?.eval(variables),
            plot
        )
    }
}

class Assignment(val variableName: String, val expr: Expr): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        variables[variableName] = expr.eval(variables)
    }
}

class plus(val expr1: Expr, val expr2: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return expr1.eval(variables) + expr2.eval(variables)
    }
}

class minus(val expr1: Expr, val expr2: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return expr1.eval(variables) - expr2.eval(variables)
    }
}

class times(val expr1: Expr, val expr2: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return expr1.eval(variables) * expr2.eval(variables)
    }
}

class divides(val expr1: Expr, val expr2: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return expr1.eval(variables) / expr2.eval(variables)
    }
}

class pow(val expr1: Expr, val expr2: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        val result = expr1.eval(variables).pow(expr2.eval(variables))
        return result
    }
}

class unary_plus(val expr: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return expr.eval(variables)
    }
}

class unary_minus(val expr: Expr): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return -expr.eval(variables)
    }
}

class variable(val value: String): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return variables[value] ?: throw Exception("Variable doesnt exist")
    }
}

class real(val value: Double): Expr {
    override fun eval(variables: MutableMap<String, Double>): Double {
        return value
    }
}

class EmptyStatement(): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
    }
}

fun main() {
    /*var result = false
    val file = File("semantika_tests/good/02.txt")
    try {
        result = SemanticAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
        println("No error")
        println(result)
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")*/
}