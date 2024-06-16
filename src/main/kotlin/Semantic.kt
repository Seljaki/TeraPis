package si.seljaki

import kotlin.math.pow

class PlotDefinitionExpr(val plot: Plot): Statement {
    override fun eval(plots: MutableMap<String, Plot>, works: MutableMap<String, Work>, variables: MutableMap<String, Double>) {
        plots[plot.name] = plot
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