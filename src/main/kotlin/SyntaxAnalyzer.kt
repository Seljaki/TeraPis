package si.seljaki

import java.awt.Desktop.Action
import java.beans.Expression
import java.io.File
import kotlin.math.exp

class SyntaxAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()

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
            val name = currentToken.lexeme;
            nextToken()
            val result = VariableAssigment(name)
            if(result.first) {
                return VariableAssigment(name)
            }
        }
        if (Function()) {
            return true
        }
        return false
    }

    fun PlotDefinition(): Pair<Boolean, PlotDefinitionExpr> {
        if (currentToken.symbol == Symbol.NAME) {
            val plot = Plot(currentToken.lexeme)
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if(PlotBody(plot)) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return Pair(true, PlotDefinitionExpr(plot))
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotBody(plot: Plot): Boolean {
        if (PlotBody2(plot)) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return PlotBody(plot)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotBody2(plot: Plot): Boolean {
        if (currentToken.symbol == Symbol.COORDINATES) {
            nextToken()
            val result = Coordinates()
            if (result.first) {
                plot.coordinates = result.second
                return true
            }
        }
        if (currentToken.symbol == Symbol.TYPE) {
            nextToken()
            if(PlotType()) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Coordinates(): Pair<Boolean, MutableList<Pair<Double, Double>>> {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if(Points()) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Points(points: MutableList<Pair<Double, Double>>): Boolean {
        if (Point(points)) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points(points)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Point(points: MutableList<Pair<Double, Double>>): Boolean {
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (expr()) {
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (expr()) {
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                return true
                            }
                        }
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotType(): Boolean {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.FOREST) {
                nextToken()
                return true
            }
            if(currentToken.symbol == Symbol.FIELD) {
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkDefinition(): Boolean {
        if(currentToken.symbol == Symbol.NAME) {
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if(WorkBody()) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkBody(): Boolean {
        if (WorkBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return WorkBody()
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkBody2(): Boolean {
        if(currentToken.symbol == Symbol.PATH) {
            nextToken()
            if (Path()) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.ACTION) {
            nextToken()
            if (Action()) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.MAX_SPEED) {
            nextToken()
            if (MaxSpeed()) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.IMPLEMENT_WIDTH) {
            nextToken()
            if (ImplementWidth()) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (WorkPlot()) {
                return true
            }
        }
        return false
    }

    fun Path(): Boolean {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if(Pointts()) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Pointts(): Boolean {
        if (Pointt()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Pointts()
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Pointt(): Boolean {
        if (currentToken.symbol == Symbol.POINTT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (expr()) {
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (expr()) {
                            if (currentToken.symbol == Symbol.COMMA) {
                                nextToken()
                                if(currentToken.symbol == Symbol.TIMESTAMP) {
                                    nextToken()
                                    if (currentToken.symbol == Symbol.RPAREN) {
                                        nextToken()
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Action(): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun MaxSpeed(): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(expr()) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun ImplementWidth(): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(expr()) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkPlot(): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
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
                nextToken()
                return If2()
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun If2(): Boolean {
        if (currentToken.symbol == Symbol.IS) {
            nextToken()
            if(IfIsValid()) {
                return true
            }
        }
        if (currentToken.symbol == Symbol.CONTAINS) {
            nextToken()
            if (IfContains()) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfIsValid(): Boolean {
        if (currentToken.symbol == Symbol.VALID) {
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (Statements()) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfContains(): Boolean {
        if (currentToken.symbol == Symbol.NAME) {
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (Statements()) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
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
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
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
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
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
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
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
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
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
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
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

    fun VariableAssigment(variableName: String): Pair<Boolean, Assignment> {
        if (currentToken.symbol == Symbol.EQUALS) {
            nextToken()
            val result = expr()
            if(result.first) {
                return Pair(true, Assignment(variableName, result.second))
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun expr(): Pair<Boolean, Expr>  {
        return additive()
    }
    fun additive(): Pair<Boolean, Expr>  {
        val result = multiplicative()
        return additive2(result.second)
    }
    fun additive2(inVal: Expr): Pair<Boolean, Expr>  {
        if(currentToken.symbol == Symbol.PLUS) {
            nextToken()
            val plus = plus(inVal, multiplicative().second)
            return additive2(plus)
        } else if(currentToken.symbol == Symbol.MINUS) {
            nextToken()
            val minus = minus(inVal, multiplicative().second)
            return additive2(minus)
        }
        return Pair(true, inVal)
    }
    fun multiplicative(): Pair<Boolean, Expr>  {
        val result = exponential()
        return multiplicative2(result.second)
    }
    fun multiplicative2(inVal: Expr): Pair<Boolean, Expr>  {
        if(currentToken.symbol == Symbol.MULTIPLY) {
            nextToken()
            val result = times(inVal, exponential().second)
            return multiplicative2(result)
        } else if( currentToken.symbol == Symbol.DIVIDE) {
            nextToken()
            val result = divides(inVal, exponential().second)
            return multiplicative2(result)
        }

        return Pair(true, inVal)
    }
    fun exponential(): Pair<Boolean, Expr>  {
        val result = unary()
        return exponential2(result.second)
    }
    fun exponential2(inVal: Expr): Pair<Boolean, Expr>  {
        if (currentToken.symbol == Symbol.POW) {
            nextToken()
            val result = exponential2(unary().second)
            return Pair(result.first, pow(inVal, result.second))
        }
        return Pair(true, inVal)
    }
    fun unary(): Pair<Boolean, Expr>  {
        if (currentToken.symbol == Symbol.PLUS) {
            nextToken()
            return Pair(true, unary_plus(primary().second))
        } else if (currentToken.symbol == Symbol.MINUS) {
            nextToken()
            return Pair(true, unary_minus(primary().second))
        }
        return primary()
    }
    fun primary(): Pair<Boolean, Expr>  {
        if (currentToken.symbol == Symbol.REAL) {
            val result =  real(currentToken.lexeme.toDouble())
            nextToken()
            return Pair(true, result)
        } else if (currentToken.symbol == Symbol.VARIABLE) {
            val result = variable(currentToken.lexeme)
            nextToken()
            return Pair(true, result)
        } else if (currentToken.symbol == Symbol.LPAREN) {
            nextToken()
            val expr = additive()
            if(currentToken.symbol == Symbol.RPAREN) {
                nextToken()
                return expr
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }
}

fun main() {
    var result = false
    val file = File("syntax_analyzer_tests/good/02.txt")
    try {
        result = SyntaxAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
        println("No error")
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")
}