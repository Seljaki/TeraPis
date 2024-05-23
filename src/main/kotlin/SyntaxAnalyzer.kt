package si.seljaki

import java.awt.Desktop.Action
import java.io.File

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
        if(PlotDefinition()) {
            return true
        }
        if(WorkDefinition()) {
            return true
        }
        if(If()) {
            return true
        }
        if (Function()) {
            return true
        }
        return false
    }

    fun PlotDefinition(): Boolean {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                nextToken()
                if (currentToken.symbol == Symbol.LCURLY) {
                    nextToken()
                    if(PlotBody()) {
                        if (currentToken.symbol == Symbol.RCURLY) {
                            nextToken()
                            return true
                        }
                    }
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun PlotBody(): Boolean {
        if (PlotBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return PlotBody()
            }
            return true
        }
        return false
    }

    fun PlotBody2(): Boolean {
        if (Coordinates()) {
            return true
        }
        if(PlotType()) {
            return true
        }
        return false
    }

    fun Coordinates(): Boolean {
        if (currentToken.symbol == Symbol.COORDINATES) {
            nextToken()
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
        return false
    }

    fun Points(): Boolean {
        if (Point()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points()
            }
            return true
        }
        return false
    }

    fun Point(): Boolean {
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.REAL) {
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

    fun PlotType(): Boolean {
        if(currentToken.symbol == Symbol.TYPE) {
            nextToken()
            if(currentToken.symbol == Symbol.COLON) {
                nextToken()
                return PlotType2()
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun PlotType2(): Boolean {
        if(currentToken.symbol == Symbol.FOREST) {
            nextToken()
            return true
        }
        if(currentToken.symbol == Symbol.FIELD) {
            nextToken()
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun WorkDefinition(): Boolean {
        if(currentToken.symbol == Symbol.WORK) {
            nextToken()
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
        return false
    }

    fun WorkBody(): Boolean {
        if (WorkBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return WorkBody()
            }
            return true
        }
        return false
    }

    fun WorkBody2(): Boolean {
        if (Path()) {
            return true
        }
        if (Action()) {
            return true
        }
        if (MaxSpeed()) {
            return true
        }
        if(ImplementWidth()) {
            return true
        }
        if (WorkPlot()) {
            return true
        }
        return false
    }

    fun Path(): Boolean {
        if(currentToken.symbol == Symbol.PATH) {
            nextToken()
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
        return false
    }

    fun Pointts(): Boolean {
        if (Pointt()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Pointts()
            }
            return true
        }
        return false
    }

    fun Pointt(): Boolean {
        if (currentToken.symbol == Symbol.POINTT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                if (currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.REAL) {
                            nextToken()
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
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun Action(): Boolean {
        if (currentToken.symbol == Symbol.ACTION) {
            nextToken()
            if (currentToken.symbol == Symbol.COLON) {
                nextToken()
                if(currentToken.symbol == Symbol.NAME) {
                    nextToken()
                    return true
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun MaxSpeed(): Boolean {
        if (currentToken.symbol == Symbol.MAX_SPEED) {
            nextToken()
            if (currentToken.symbol == Symbol.COLON) {
                nextToken()
                if(currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    return true
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun ImplementWidth(): Boolean {
        if (currentToken.symbol == Symbol.IMPLEMENT_WIDTH) {
            nextToken()
            if (currentToken.symbol == Symbol.COLON) {
                nextToken()
                if(currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    return true
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun WorkPlot(): Boolean {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.COLON) {
                nextToken()
                if(currentToken.symbol == Symbol.NAME) {
                    nextToken()
                    return true
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun If(): Boolean {
        if (currentToken.symbol == Symbol.IF) {
            nextToken()
            if (currentToken.symbol == Symbol.PLOT) {
                nextToken()
                if (currentToken.symbol == Symbol.NAME) {
                    nextToken()
                    return If2()
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }

    fun If2(): Boolean {
        if(IfIsValid()) {
            return true
        }
        if (IfContains()) {
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun IfIsValid(): Boolean {
        if (currentToken.symbol == Symbol.IS) {
            nextToken()
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
        return false
    }

    fun IfContains(): Boolean {
        if (currentToken.symbol == Symbol.CONTAINS) {
            nextToken()
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
        return false
    }

    fun Function(): Boolean {
        if (CalculatePath()) {
            return true
        }
        if (CalculateArea()) {
            return true
        }
        if (CalculateAreaCovered()) {
            return true
        }
        if (CalculateAverageSpeed()) {
            return true
        }
        if (CalculateEfficiency()) {
            return true
        }
        return false
    }

    fun CalculatePath(): Boolean {
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
        return false
    }

    fun CalculateArea(): Boolean {
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
        return false
    }

    fun CalculateAreaCovered(): Boolean {
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
        return false
    }

    fun CalculateAverageSpeed(): Boolean {
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
        return false
    }

    fun CalculateEfficiency(): Boolean {
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