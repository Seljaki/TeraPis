package si.seljaki

import java.io.File

class SemanticAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()
    private val declaredPlots = mutableSetOf<String>()
    private val declaredWorks = mutableSetOf<String>()
    private val declaredVariables = mutableSetOf<String>()
    private val semanticErrors = mutableListOf<String>()

    fun nextToken(): Token {
        currentToken = scanner.getToken()
        println("Next token: $currentToken")
        return currentToken
    }

    fun parse(): Boolean {
        println("Starting parse")
        return Statements()
    }

    fun Statements(): Boolean {
        println("Entering Statements")
        while (currentToken.symbol != Symbol.EOF) {
            if (currentToken.symbol == Symbol.RCURLY) {
                println("Detected RCURLY in Statements, exiting")
                return true
            }
            if (!Statement()) {
                println("Exiting Statements with failure")
                return false
            }
            println("Processed a statement")
        }
        println("Exiting Statements")
        return currentToken.symbol == Symbol.EOF
    }

    fun Statement(): Boolean {
        println("Entering Statement with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.PLOT -> {
                nextToken()
                PlotDefinition()
            }
            Symbol.WORK -> {
                nextToken()
                WorkDefinition()
            }
            Symbol.IF -> {
                nextToken()
                If()
            }
            Symbol.VARIABLE -> {
                VariableAssignment()
            }
            Symbol.EOF -> false
            else -> Function()
        }
    }

    fun PlotDefinition(): Boolean {
        println("Entering PlotDefinition with token: $currentToken")
        if (currentToken.symbol == Symbol.NAME) {
            val plotName = currentToken.lexeme
            declaredPlots.add(plotName)
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (PlotBody()) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        semanticErrors.add("Invalid plot definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun PlotBody(): Boolean {
        println("Entering PlotBody with token: $currentToken")
        if (PlotBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return PlotBody()
            }
            return true
        }
        semanticErrors.add("Invalid plot body at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun PlotBody2(): Boolean {
        println("Entering PlotBody2 with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.COORDINATES -> {
                nextToken()
                Coordinates()
            }
            Symbol.TYPE -> {
                nextToken()
                PlotType()
            }
            else -> {
                semanticErrors.add("Invalid plot body element at ${currentToken.startRow}:${currentToken.startColumn}")
                false
            }
        }
    }

    fun Coordinates(): Boolean {
        println("Entering Coordinates with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if (Points()) {
                    if (currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        semanticErrors.add("Invalid coordinates definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun Points(): Boolean {
        println("Entering Points with token: $currentToken")
        if (Point()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points()
            }
            return true
        }
        semanticErrors.add("Invalid points definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun Point(): Boolean {
        println("Entering Point with token: $currentToken")
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
        semanticErrors.add("Invalid point definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun PlotType(): Boolean {
        println("Entering PlotType with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            return when (currentToken.symbol) {
                Symbol.FOREST, Symbol.FIELD -> {
                    nextToken()
                    true
                }
                else -> {
                    semanticErrors.add("Invalid plot type at ${currentToken.startRow}:${currentToken.startColumn}")
                    false
                }
            }
        }
        semanticErrors.add("Invalid plot type definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun WorkDefinition(): Boolean {
        println("Entering WorkDefinition with token: $currentToken")
        if (currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            declaredWorks.add(workName)
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (WorkBody()) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        semanticErrors.add("Invalid work definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun WorkBody(): Boolean {
        println("Entering WorkBody with token: $currentToken")
        if (WorkBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return WorkBody()
            }
            return true
        }
        semanticErrors.add("Invalid work body at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun WorkBody2(): Boolean {
        println("Entering WorkBody2 with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.PATH -> {
                nextToken()
                Path()
            }
            Symbol.ACTION -> {
                nextToken()
                Action()
            }
            Symbol.MAX_SPEED -> {
                nextToken()
                MaxSpeed()
            }
            Symbol.IMPLEMENT_WIDTH -> {
                nextToken()
                ImplementWidth()
            }
            Symbol.PLOT -> {
                nextToken()
                WorkPlot()
            }
            else -> {
                semanticErrors.add("Invalid work body element at ${currentToken.startRow}:${currentToken.startColumn}")
                false
            }
        }
    }

    fun Path(): Boolean {
        println("Entering Path with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                if (Pointts()) {
                    if (currentToken.symbol == Symbol.RSQUARE) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        semanticErrors.add("Invalid path definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun Pointts(): Boolean {
        println("Entering Pointts with token: $currentToken")
        if (Pointt()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Pointts()
            }
            return true
        }
        semanticErrors.add("Invalid path points definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun Pointt(): Boolean {
        println("Entering Pointt with token: $currentToken")
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
                                if (currentToken.symbol == Symbol.TIMESTAMP) {
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
        semanticErrors.add("Invalid PointT definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun Action(): Boolean {
        println("Entering Action with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                nextToken()
                return true
            }
        }
        semanticErrors.add("Invalid action definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun MaxSpeed(): Boolean {
        println("Entering MaxSpeed with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (expr()) {
                return true
            }
        }
        semanticErrors.add("Invalid max-speed definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun ImplementWidth(): Boolean {
        println("Entering ImplementWidth with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (expr()) return true
        }
        semanticErrors.add("Invalid implement-width definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun WorkPlot(): Boolean {
        println("Entering WorkPlot with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                if (currentToken.lexeme in declaredPlots) {
                    nextToken()
                    return true
                } else {
                    semanticErrors.add("Referenced undeclared plot '${currentToken.lexeme}' at ${currentToken.startRow}:${currentToken.startColumn}")
                    return false
                }
            }
        }
        semanticErrors.add("Invalid work plot definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun If(): Boolean {
        println("Entering If with token: $currentToken")
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val plotName = currentToken.lexeme
                if (plotName in declaredPlots) {
                    nextToken()
                    return If2()
                } else {
                    semanticErrors.add("Referenced undeclared plot in IF statement at ${currentToken.startRow}:${currentToken.startColumn}")
                    return false
                }
            }
        }
        semanticErrors.add("Invalid IF statement at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun If2(): Boolean {
        println("Entering If2 with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.IS -> {
                nextToken()
                IfIsValid()
            }
            Symbol.CONTAINS -> {
                nextToken()
                IfContains()
            }
            else -> {
                semanticErrors.add("Invalid IF condition at ${currentToken.startRow}:${currentToken.startColumn}")
                false
            }
        }
    }

    fun IfIsValid(): Boolean {
        println("Entering IfIsValid with token: $currentToken")
        if (currentToken.symbol == Symbol.VALID) {
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (Statements()) {
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        return true
                    }
                }
            }
        }
        semanticErrors.add("Invalid IF is valid statement at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun IfContains(): Boolean {
        println("Entering IfContains with token: $currentToken")
        if (currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            if (workName in declaredWorks) {
                nextToken()
                if (currentToken.symbol == Symbol.LCURLY) {
                    nextToken()
                    if (Statements()) {
                        if (currentToken.symbol == Symbol.RCURLY) {
                            nextToken()
                            println("Exiting IfContains with success")
                            return true
                        } else {
                            semanticErrors.add("Expected '}' at ${currentToken.startRow}:${currentToken.startColumn}")
                        }
                    }
                } else {
                    semanticErrors.add("Expected '{' after work name at ${currentToken.startRow}:${currentToken.startColumn}")
                }
            } else {
                semanticErrors.add("Referenced undeclared work in IF statement at ${currentToken.startRow}:${currentToken.startColumn}")
            }
        } else {
            semanticErrors.add("Expected work name in IF contains statement at ${currentToken.startRow}:${currentToken.startColumn}")
        }
        println("Exiting IfContains with failure")
        return false
    }

    fun Function(): Boolean {
        println("Entering Function with token: $currentToken")
        val functionName = currentToken.lexeme
        nextToken()
        if (currentToken.symbol == Symbol.LPAREN) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val firstParam = currentToken.lexeme
                nextToken()
                if (currentToken.symbol == Symbol.COMMA) {
                    nextToken()
                    if (currentToken.symbol == Symbol.NAME) {
                        val secondParam = currentToken.lexeme
                        nextToken()
                        if (currentToken.symbol == Symbol.RPAREN) {
                            val validParams = when (functionName) {
                                "CalculateEfficiency" -> firstParam in declaredPlots && secondParam in declaredWorks
                                "CalculatePath" -> firstParam in declaredPlots && secondParam in declaredWorks
                                else -> {
                                    semanticErrors.add("Unknown function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                                    false
                                }
                            }
                            if (validParams) {
                                nextToken()
                                println("Function $functionName processed successfully")
                                return true
                            } else {
                                semanticErrors.add("Invalid parameters for function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                            }
                        } else {
                            semanticErrors.add("Expected ')' at ${currentToken.startRow}:${currentToken.startColumn}")
                        }
                    } else {
                        semanticErrors.add("Expected second parameter name at ${currentToken.startRow}:${currentToken.startColumn}")
                    }
                } else if (currentToken.symbol == Symbol.RPAREN) {
                    val validParams = when (functionName) {
                        "CalculateArea" -> firstParam in declaredPlots
                        "CalculateAverageSpeed" -> firstParam in declaredWorks
                        "CalculateAreaCovered" -> firstParam in declaredWorks
                        else -> {
                            semanticErrors.add("Unknown function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                            false
                        }
                    }
                    if (validParams) {
                        nextToken()
                        println("Function $functionName processed successfully")
                        return true
                    } else {
                        semanticErrors.add("Invalid parameters for function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                    }
                } else {
                    semanticErrors.add("Expected ',' or ')' at ${currentToken.startRow}:${currentToken.startColumn}")
                }
            } else {
                semanticErrors.add("Expected first parameter name at ${currentToken.startRow}:${currentToken.startColumn}")
            }
        } else {
            semanticErrors.add("Expected '(' after function name at ${currentToken.startRow}:${currentToken.startColumn}")
        }
        println("Exiting Function with failure")
        return false
    }


    fun VariableAssignment(): Boolean {
        println("Entering VariableAssignment with token: $currentToken")
        if (currentToken.symbol == Symbol.VARIABLE) {
            val varName = currentToken.lexeme
            nextToken()
            if (currentToken.symbol == Symbol.EQUALS) {
                nextToken()
                if (expr()) {
                    declaredVariables.add(varName)
                    return true
                }
            }
        }
        semanticErrors.add("Invalid variable assignment at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun expr(): Boolean {
        println("Entering expr with token: $currentToken")
        return additive()
    }

    fun additive(): Boolean {
        println("Entering additive with token: $currentToken")
        return multiplicative() && additive2()
    }

    fun additive2(): Boolean {
        println("Entering additive2 with token: $currentToken")
        if (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            nextToken()
            return multiplicative() && additive2()
        }
        return true
    }

    fun multiplicative(): Boolean {
        println("Entering multiplicative with token: $currentToken")
        return exponential() && multiplicative2()
    }

    fun multiplicative2(): Boolean {
        println("Entering multiplicative2 with token: $currentToken")
        if (currentToken.symbol == Symbol.MULTIPLY || currentToken.symbol == Symbol.DIVIDE) {
            nextToken()
            return exponential() && multiplicative2()
        }
        return true
    }

    fun exponential(): Boolean {
        println("Entering exponential with token: $currentToken")
        return unary() && exponential2()
    }

    fun exponential2(): Boolean {
        println("Entering exponential2 with token: $currentToken")
        if (currentToken.symbol == Symbol.POW) {
            nextToken()
            return unary() && exponential2()
        }
        return true
    }

    fun unary(): Boolean {
        println("Entering unary with token: $currentToken")
        return if (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            nextToken()
            primary()
        } else {
            primary()
        }
    }

    fun primary(): Boolean {
        println("Entering primary with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.REAL -> {
                nextToken()
                true
            }
            Symbol.VARIABLE -> {
                val varName = currentToken.lexeme
                if (varName in declaredVariables) {
                    nextToken()
                    true
                } else {
                    semanticErrors.add("Undeclared variable '$varName' used at ${currentToken.startRow}:${currentToken.startColumn}")
                    false
                }
            }
            Symbol.LPAREN -> {
                nextToken()
                if (additive()) {
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }

    fun getSemanticErrors(): List<String> {
        return semanticErrors
    }
}

fun main() {
    val file = File("semantika_tests/02.txt")
    try {
        val analyzer = SemanticAnalyzer(Scanner(Lexicon, file.inputStream()))
        val result = analyzer.parse()
        if (analyzer.getSemanticErrors().isEmpty()) {
            println("No error")
            println("Is correct: $result")
        } else {
            println("Semantic errors found:")
            analyzer.getSemanticErrors().forEach { println(it) }
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
