package si.seljaki

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SyntaxAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()

    fun nextToken(): Token {
        currentToken = scanner.getToken()
        //println(currentToken)
        return currentToken
    }

    fun parse(): Pair<Boolean, List<Statement>> {
        return Statements()
    }

    fun Statements(): Pair<Boolean, List<Statement>> {
        val statements: MutableList<Statement> = mutableListOf()

        while (currentToken.symbol != Symbol.EOF) {
            val result = Statement()
            statements.add(result.second)
            if (!result.first)
                return Pair(false, statements)
        }

        return Pair(true, statements)
    }

    fun Statement(): Pair<Boolean, Statement> {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            return PlotDefinition()
        }
        if (currentToken.symbol == Symbol.WORK) {
            nextToken()
            return WorkDefinition()
        }
        if (currentToken.symbol == Symbol.IF) {
            nextToken()
            return If()
        }
        if (currentToken.symbol == Symbol.VARIABLE) {
            val name = currentToken.lexeme;
            nextToken()
            val result = VariableAssigment(name)
            if(result.first) {
                return VariableAssigment(name)
            }
        }
        val functionResult = Function()
        if (functionResult.first) {
            return functionResult
        }
        return Pair(false, EmptyStatement())
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
                plot.coordinatesExpr = result.second
                return true
            }
        }
        if (currentToken.symbol == Symbol.TYPE) {
            nextToken()
            if(PlotType(plot)) {
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Coordinates(): Pair<Boolean, CoordinatesExpr> {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                val points: MutableList<Pair<Expr, Expr>> = mutableListOf()
                if(Points(points)) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return Pair(true, CoordinatesExpr(points))
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Points(points: MutableList<Pair<Expr, Expr>>): Boolean {
        val result = Point()
        if (result.first) {
            points.add(result.second)
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points(points)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Point(): Pair<Boolean, Pair<Expr, Expr>> {
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val expr1 = expr()
                if (expr1.first) {
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        val expr2 = expr()
                        if (expr2.first) {
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                return Pair(true, Pair(expr1.second, expr2.second))
                            }
                        }
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotType(plot: Plot): Boolean {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.FOREST) {
                nextToken()
                plot.type = PlotType.FOREST
                return true
            }
            if(currentToken.symbol == Symbol.FIELD) {
                nextToken()
                plot.type = PlotType.FIELD
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkDefinition(): Pair<Boolean, WorkDefinitionExpr> {
        if(currentToken.symbol == Symbol.NAME) {
            val workDefinitionExpr = WorkDefinitionExpr(currentToken.lexeme)
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if(WorkBody(workDefinitionExpr)) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return Pair(true, workDefinitionExpr)
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkBody(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if (WorkBody2(workDefinitionExpr)) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return WorkBody(workDefinitionExpr)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkBody2(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if(currentToken.symbol == Symbol.PATH) {
            nextToken()
            val result = Path()
            if (result.first) {
                workDefinitionExpr.pathExpr = result.second
                return true
            }
        }
        if(currentToken.symbol == Symbol.ACTION) {
            nextToken()
            if (Action(workDefinitionExpr)) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.MAX_SPEED) {
            nextToken()
            if (MaxSpeed(workDefinitionExpr)) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.IMPLEMENT_WIDTH) {
            nextToken()
            if (ImplementWidth(workDefinitionExpr)) {
                return true
            }
        }
        if(currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (WorkPlot(workDefinitionExpr)) {
                return true
            }
        }
        return false
    }

    fun Path(): Pair<Boolean, PathExpr> {
        if(currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.LSQUARE) {
                val points: MutableList<Triple<Expr, Expr, LocalDateTime>> = mutableListOf()
                nextToken()
                if(Pointts(points)) {
                    if(currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return Pair(true, PathExpr(points))
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Pointts(points: MutableList<Triple<Expr, Expr, LocalDateTime>>): Boolean {
        val result = Pointt()
        if (result.first) {
            points.add(result.second)
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Pointts(points)
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Pointt(): Pair<Boolean, Triple<Expr, Expr, LocalDateTime>> {
        if (currentToken.symbol == Symbol.POINTT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val expr1 = expr()
                if (expr1.first) {
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        val expr2 = expr()
                        if (expr2.first) {
                            if (currentToken.symbol == Symbol.COMMA) {
                                nextToken()
                                if(currentToken.symbol == Symbol.TIMESTAMP) {
                                    val customFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmssX")
                                    val time = LocalDateTime.parse(currentToken.lexeme, customFormatter)
                                    nextToken()
                                    if (currentToken.symbol == Symbol.RPAREN) {
                                        nextToken()
                                        return Pair(true, Triple(
                                            expr1.second,
                                            expr2.second,
                                            time
                                        ))
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

    fun Action(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
                workDefinitionExpr.action = currentToken.lexeme
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun MaxSpeed(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            val result = expr()
            if(result.first) {
                workDefinitionExpr.maxSpeed = result.second
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun ImplementWidth(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            val result = expr()
            if(result.first) {
                workDefinitionExpr.implementWidth = result.second
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkPlot(workDefinitionExpr: WorkDefinitionExpr): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.NAME) {
                workDefinitionExpr.plot = currentToken.lexeme
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun If(): Pair<Boolean, Statement> {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val name = currentToken.lexeme
                nextToken()
                return If2(name)
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun If2(plotName: String): Pair<Boolean, Statement> {
        if (currentToken.symbol == Symbol.IS) {
            nextToken()
            return IfIsValid(plotName)
        }
        if (currentToken.symbol == Symbol.CONTAINS) {
            nextToken()
            return IfContains(plotName)
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfIsValid(plotName: String): Pair<Boolean, IfPlotIsValidExpr> {
        if (currentToken.symbol == Symbol.VALID) {
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val result = Statements()
                if (result.first) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return Pair(true, IfPlotIsValidExpr(plotName, result.second))
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfContains(plotName: String): Pair<Boolean, IfContainsExpr> {
        if (currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            nextToken()
            if(currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val result = Statements()
                if (result.first) {
                    if(currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return Pair(true, IfContainsExpr(plotName, workName, result.second))
                    }
                }
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun Function(): Pair<Boolean, Statement> {
        if (currentToken.symbol == Symbol.CALCULATE_PATH) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    val plotName = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            val workName = currentToken.lexeme
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                return Pair(true, CalculatePathFunction(workName, plotName))
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
                    val plotName = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        return Pair(true, CalculateAreaFunction(plotName))
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
                    val workName = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        return Pair(true, CalculateAreaCoveredFunction(workName))
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
                    val workName = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        return Pair(true, CalculateAverageSpeedFunction(workName))
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
                    val plotName = currentToken.lexeme
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.NAME) {
                            val workName = currentToken.lexeme
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                return Pair(true, CalculateEfficiencyFunction(workName, plotName))
                            }
                        }
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return Pair(false, EmptyStatement())
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
        val ast =  SyntaxAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
        result = ast.first
        println("No error")
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")
}