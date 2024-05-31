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
        if (Function()) {
            return true
        }
        return false
    }

    fun PlotDefinition(): Boolean {
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

    fun PlotBody(): Boolean {
        if (PlotBody2()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return PlotBody()
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun PlotBody2(): Boolean {
        if (currentToken.symbol == Symbol.COORDINATES) {
            nextToken()
            if (Coordinates()) {
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

    fun Coordinates(): Boolean {
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

    fun Points(): Boolean {
        if (Point()) {
            if (currentToken.symbol == Symbol.COMMA) {
                nextToken()
                return Points()
            }
            return true
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
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
            if(currentToken.symbol == Symbol.REAL) {
                nextToken()
                return true
            }
        }
        throw IllegalStateException("Encountered invalid token: $currentToken")
    }

    fun ImplementWidth(): Boolean {
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if(currentToken.symbol == Symbol.REAL) {
                nextToken()
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