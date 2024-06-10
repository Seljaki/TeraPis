package si.seljaki

import java.awt.Desktop.Action
import java.beans.Expression
import java.io.File
import kotlin.math.exp
import kotlin.time.Duration.Companion.milliseconds

enum class PlotType{
    FOREST, FIELD, UNDEFINED
}
abstract class Expr {
    abstract fun eval(env: MutableMap<String, Any>): Any
}


data class PlotExpr(
    var name: String,
    var coordinates: MutableList<Pair<Double, Double>>,
    var type: PlotType
) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        if (coordinates.isEmpty()) {
            throw IllegalStateException("Plot $name has no coordinates.")
        }
        if (type == PlotType.UNDEFINED) {
            throw IllegalStateException("Plot $name has an undefined type.")
        }
        env["plots"] = (env["plots"] as MutableMap<String, PlotExpr>).apply { put(name, this@PlotExpr) }
        println("Defined plot $name with type $type and coordinates $coordinates")
        return this
    }
}

data class WorkExpr(
    var name: String,
    var path: MutableList<Pair<Double, Double>>,
    var timestamps: MutableList<String>,
    var action: String,
    var maxSpeed: Double,
    var implementWidth: Double,
    var plot: String
) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        if (plot !in plots) {
            throw IllegalStateException("Work $name references undefined plot or it's undefined.")
        }
        if(implementWidth <= 0){
            throw IllegalStateException("Work $name has no implemented width, or it's 0 or less.")

        }
        if(action.isEmpty()){
            throw IllegalStateException("Work $name has no action.")
        }
        env["works"] = (env["works"] as MutableMap<String, WorkExpr>).apply { put(name, this@WorkExpr) }
        println("Defined work $name on plot $plot with action $action")
        return this
    }
}



data class IfExpr(val condition: String, val statements: List<String>) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        println("Evaluating if condition $condition")
        println(statements)
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        if(condition == "isValid"){
            if(statements[0] !in plots){
                throw IllegalStateException("$statements is not valid!")
            }
        }
        // TODO ifContains, samo nevem kak
        return this
    }
}

data class VariableAssignmentExpr(val variable: String, val value: Double) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        env[variable] = value
        println("Assigned $value to variable $variable")
        return this
    }
}

data class FunctionCallExpr(val name: String, val args: List<String>) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        val works = env["works"] as MutableMap<String, WorkExpr>
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        println("Calling function $name with arguments $args")
        //TODO Kaj naj se tu zgodi? In kako se naj to zračuna?
        if(name == "calculateAverageSpeed" || name == "calculateAreaCovered"){
            if (args[0] !in works){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            if(name == "calculateAverageSpeed" ){
                //TODO calculateAverageSpeed
            }else{
                //TODO calculateAreaCovered
            }
        }
        else if(name == "calculateArea"){
            if (args[0] !in plots){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            //TODO calculateArea
        }
        else{
            if(args[0] !in plots && args[1] !in works){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            if(name == "calculateEfficency"){
                //TODO calculateAverageSpeed
            }else{
                //TODO calculatePath
            }
        }
        return this
    }
}
data class RealExpr(val value: Double) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return value
    }
}

data class VarExpr(val name: String) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return env[name] ?: throw IllegalStateException("Undefined variable: $name")
    }
}

data class PlusExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) + (right.eval(env) as Double)
    }
}

data class MinusExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) - (right.eval(env) as Double)
    }
}

data class TimesExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) * (right.eval(env) as Double)
    }
}

data class DivideExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) / (right.eval(env) as Double)
    }
}

data class PowExpr(val base: Expr, val exponent: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return Math.pow((base.eval(env) as Double), (exponent.eval(env) as Double))
    }
}

data class UnaryMinusExpr(val expr: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return -(expr.eval(env) as Double)
    }
}
class Ref<T>(var value: T)

class SemanticAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()
    private val env = mutableMapOf<String, Any>(
        "plots" to mutableMapOf<String, PlotExpr>(),
        "works" to mutableMapOf<String, WorkExpr>()
    )

    fun nextToken(): Token {
        currentToken = scanner.getToken()
        //println(currentToken)
        return currentToken
    }

    fun parse(): Boolean {
        return Statements()
    }

    fun Statements(): Boolean {
        if (Statement()) {
            if (Statements()) {
                return true
            }
            return true
        }
        return false
    }

    fun Statement(): Boolean {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if(PlotDefinition()) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.WORK) {
            nextToken()
            if(WorkDefinition()) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.IF) {
            nextToken()
            if(If()) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.VARIABLE) {
            if(VariableAssigment()) {
                return true
            }
        }
        if (Function()) {
            return true
        }
        return false
    }

    fun PlotDefinition(): Boolean {
        if (currentToken.symbol == Symbol.NAME) {
            val plotName = currentToken.lexeme
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val coordinates = mutableListOf<Pair<Double,Double>>()
                var plotType = Ref(PlotType.UNDEFINED)
                if(PlotBody(coordinates,plotType)) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        PlotExpr(plotName,coordinates,plotType.value).eval(env)
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotBody(coordinates: MutableList<Pair<Double, Double>>, plotType: Ref<PlotType>): Boolean {
        if (PlotBody2(coordinates,plotType)) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return PlotBody(coordinates,plotType)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotBody2(coordinates: MutableList<Pair<Double, Double>>, plotType: Ref<PlotType>): Boolean {
        if (currentToken.symbol == Symbol.COORDINATES) {
            nextToken()
            if (Coordinates(coordinates)) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.TYPE) {
            nextToken()
            if(PlotType(plotType)) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Coordinates(coordinates: MutableList<Pair<Double, Double>>): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if(Points(coordinates)) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Points(coordinates: MutableList<Pair<Double, Double>>): Boolean {
        if (Point(coordinates)) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points(coordinates)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Point(coordinates: MutableList<Pair<Double, Double>>): Boolean {
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val x = (expr().eval(env) as Double)
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        val y  = (expr().eval(env) as Double)
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                coordinates.add(Pair(x,y))
                                return true
                            }
                    }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotType(plotType: Ref<PlotType>): Boolean {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.FOREST) {
                plotType.value = PlotType.FOREST
                nextToken()
                return true
            }
            if(currentToken.symbol == Symbol.FIELD) {
                plotType.value = PlotType.FIELD
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkDefinition(): Boolean {
        if(currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val path = mutableListOf<Pair<Double,Double>>()
                val timestamps = mutableListOf<String>()
                var action = Ref("")
                var maxSpeed = Ref(0.0)
                var implementWidth = Ref(0.0)
                var plot = Ref("")
                if(WorkBody(path, timestamps, action, maxSpeed, implementWidth, plot)) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        WorkExpr(workName, path, timestamps, action.value, maxSpeed.value, implementWidth.value, plot.value).eval(env)
                        return true
                    } else {
                        throw IllegalStateException("Expected RCURLY but found: $currentToken")
                    }
                } else {
                    throw IllegalStateException("Invalid WorkBody structure.")
                }
            } else {
                throw IllegalStateException("Expected LCURLY but found: $currentToken")
            }
        }
        throw IllegalStateException("Expected NAME but found: $currentToken")
    }

    fun WorkBody(path: MutableList<Pair<Double,Double>>, timestamps: MutableList<String>, action: Ref<String>, maxSpeed: Ref<Double>, implementWidth: Ref<Double>, plot: Ref<String>): Boolean {
        if (WorkBody2(path, timestamps, action, maxSpeed, implementWidth, plot)) {
            while (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                if (!WorkBody2(path, timestamps, action, maxSpeed, implementWidth, plot)) {
                    throw IllegalStateException("Invalid token in WorkBody: $currentToken")
                }
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token in WorkBody: $currentToken")
    }

    fun WorkBody2(path: MutableList<Pair<Double,Double>>, timestamps: MutableList<String>, action: Ref<String>, maxSpeed: Ref<Double>, implementWidth: Ref<Double>, plot: Ref<String>): Boolean {
        //println("Current token in WorkBody2: $currentToken")
        return when {
            currentToken.symbol == Symbol.PATH -> {
                nextToken()
                if (Path(path, timestamps)) {
                    true
                } else {
                    throw IllegalStateException("Invalid PATH structure.")
                }
            }
            currentToken.symbol == Symbol.ACTION -> {
                nextToken()
                if (Action(action)) {
                    true
                } else {
                    throw IllegalStateException("Invalid ACTION structure.")
                }
            }
            currentToken.symbol == Symbol.MAX_SPEED -> {
                nextToken()
                if (MaxSpeed(maxSpeed)) {
                    true
                } else {
                    throw IllegalStateException("Invalid MAX_SPEED structure.")
                }
            }
            currentToken.symbol == Symbol.IMPLEMENT_WIDTH -> {
                nextToken()
                if (ImplementWidth(implementWidth)) {
                    //println("Finished processing implement-width, current token: $currentToken")
                    true
                } else {
                    throw IllegalStateException("Invalid IMPLEMENT_WIDTH structure.")
                }
            }
            currentToken.symbol == Symbol.PLOT -> {
                nextToken()
                if (WorkPlot(plot)) {
                    true
                } else {
                    throw IllegalStateException("Invalid PLOT structure.")
                }
            }
            else -> false
        }
    }

    fun Path(path: MutableList<Pair<Double,Double>>, timestamps: MutableList<String>): Boolean {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if(Pointts(path, timestamps)) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    } else {
                        throw IllegalStateException("Expected RSQUARE but found: $currentToken")
                    }
                } else {
                    throw IllegalStateException("Invalid POINTTS structure.")
                }
            } else {
                throw IllegalStateException("Expected LSQUARE but found: $currentToken")
            }
        } else {
            throw IllegalStateException("Expected COLON but found: $currentToken")
        }
    }

    fun Pointts(path: MutableList<Pair<Double,Double>>, timestamps: MutableList<String>): Boolean {
        if (Pointt(path, timestamps)) {
            while (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                if (!Pointt(path, timestamps)) {
                    throw IllegalStateException("Invalid token in POINTTS: $currentToken")
                }
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token in POINTTS: $currentToken")
    }

    fun Pointt(path: MutableList<Pair<Double, Double>>, timestamps: MutableList<String>): Boolean {
        if (currentToken.symbol == Symbol.POINTT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val x = (expr().eval(env) as Double)
                if (currentToken.symbol == Symbol.COMMA) {
                    nextToken()
                    val y = (expr().eval(env) as Double)
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        //println("Current token before timestamp: $currentToken")
                        val timestamp = currentToken.lexeme
                        if (currentToken.symbol == Symbol.TIMESTAMP) {
                            nextToken()
                            //println("Current token after timestamp: $currentToken")
                            if (currentToken.symbol == Symbol.COMMA) {
                                //println("Found COMMA after TIMESTAMP, moving to next token.")
                                nextToken()
                            }
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                path.add(Pair(x, y))
                                timestamps.add(timestamp)
                                return true
                            } else {
                                throw IllegalStateException("Expected RPAREN but found: $currentToken")
                            }
                        } else {
                            throw IllegalStateException("Expected TIMESTAMP but found: $currentToken")
                        }
                    } else {
                        throw IllegalStateException("Expected COMMA after y-coordinate but found: $currentToken")
                    }
                } else {
                    throw IllegalStateException("Expected COMMA after x-coordinate but found: $currentToken")
                }
            } else {
                throw IllegalStateException("Expected LPAREN but found: $currentToken")
            }
        } else {
            throw IllegalStateException("Expected POINTT but found: $currentToken")
        }
    }

    fun Action(action: Ref<String>): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
                action.value = currentToken.lexeme
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun MaxSpeed(maxSpeed: Ref<Double>): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            maxSpeed.value = (expr().eval(env) as Double)
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun ImplementWidth(implementWidth: Ref<Double>): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            implementWidth.value = (expr().eval(env) as Double)
            //println("Evaluated implement-width: ${implementWidth.value}")
            return true
        }
        throw IllegalStateException("Encountered invalid token in ImplementWidth: $currentToken")
    }

    fun WorkPlot(plot: Ref<String>): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
                plot.value = currentToken.lexeme
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun If(): Boolean {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val plotName = currentToken.lexeme
                nextToken()
                return If2(plotName)
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun If2(plotName: String): Boolean {
        if (currentToken.symbol == Symbol.IS) {
            nextToken()
            if(IfIsValid(plotName)) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.CONTAINS) {
            nextToken()
            if (IfContains(plotName)) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfIsValid(plotName: String): Boolean {
        if (currentToken.symbol == Symbol.VALID) {
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                var statements: List<String> = listOf(plotName)
                if (Statements()) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        IfExpr("isValid",statements).eval(env)
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfContains(plotName: String): Boolean {
        if (currentToken.symbol == Symbol.NAME) {
            val name = currentToken.lexeme
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val statements = listOf(plotName, name)
                if (Statements()) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        IfExpr("contains", statements).eval(env)
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Function(): Boolean {
        if (currentToken.symbol == Symbol.CALCULATE_PATH) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val arg1 = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            val arg2 = currentToken.lexeme
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                FunctionCallExpr("calculatePath", listOf(arg1,arg2)).eval(env)
                                return true
                            }
                        }
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        if (currentToken.symbol == Symbol.CALCULATE_AREA) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val arg = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        FunctionCallExpr("calculateArea", listOf(arg)).eval(env)
                        return true
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        if (currentToken.symbol == Symbol.CALCULATE_AREA_COVERED) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val arg = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        FunctionCallExpr("calculateAreaCovered", listOf(arg)).eval(env)
                        return true
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        if (currentToken.symbol == Symbol.CALCULATE_AVERAGE_SPEED) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val arg = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        FunctionCallExpr("calculateAverageSpeed", listOf(arg)).eval(env)
                        return true
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        if (currentToken.symbol == Symbol.CALCULATE_EFFICIENCY) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val arg1 = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            val arg2 = currentToken.lexeme
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                FunctionCallExpr("calculateEfficiency", listOf(arg1,arg2)).eval(env)
                                return true
                            }
                        }
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }
    fun VariableAssigment(): Boolean {
        val variable = currentToken.lexeme
        println("variable " + variable)
        nextToken()
        if (currentToken.symbol == Symbol.EQUALS) {
            nextToken()
            val value = (expr().eval(env) as Double)
            VariableAssignmentExpr(variable, value).eval(env)
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun expr(): Expr {
        return additive()
    }

    fun additive(): Expr {
        var expr = multiplicative()
        while (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            val operator = currentToken.symbol
            nextToken()
            val rightExpr = multiplicative()
            expr = if (operator == Symbol.PLUS) {
                PlusExpr(expr, rightExpr)
            } else {
                MinusExpr(expr, rightExpr)
            }
        }
        return expr
    }

    fun multiplicative(): Expr {
        var expr = exponential()
        while (currentToken.symbol == Symbol.MULTIPLY || currentToken.symbol == Symbol.DIVIDE) {
            val operator = currentToken.symbol
            nextToken()
            val rightExpr = exponential()
            expr = if (operator == Symbol.MULTIPLY) {
                TimesExpr(expr, rightExpr)
            } else {
                DivideExpr(expr, rightExpr)
            }
        }
        return expr
    }

    fun exponential(): Expr {
        var expr = unary()
        while (currentToken.symbol == Symbol.POW) {
            nextToken()
            val rightExpr = unary()
            expr = PowExpr(expr, rightExpr)
        }
        return expr
    }

    fun unary(): Expr {
        return if (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            val operator = currentToken.symbol
            nextToken()
            val rightExpr = primary()
            if (operator == Symbol.MINUS) {
                UnaryMinusExpr(rightExpr)
            } else {
                rightExpr
            }
        } else {
            primary()
        }
    }

    fun primary(): Expr {
        return when {
            currentToken.symbol == Symbol.REAL -> {
                val value = currentToken.lexeme.toDouble()
                nextToken()
                RealExpr(value)
            }
            currentToken.symbol == Symbol.VARIABLE -> {
                val variable = currentToken.lexeme
                nextToken()
                VarExpr(variable)
            }
            currentToken.symbol == Symbol.LPAREN -> {
                nextToken()
                val expr = additive()
                if (currentToken.symbol == Symbol.RPAREN) {
                    nextToken()
                    expr
                } else {
                    throw IllegalStateException("Expected closing parenthesis but found: $currentToken")
                }
            }
            else -> throw IllegalStateException("Encountered invalid token: $currentToken")
        }
    }
}

fun main() {
    var result = false
    val file = File("syntax_analyzer_tests/good/05.txt")
    try {
        result = SemanticAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
        println("No error")
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")
}