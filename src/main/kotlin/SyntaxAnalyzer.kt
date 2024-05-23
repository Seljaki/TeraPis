package si.seljaki

import java.io.File

class SyntaxAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()

    fun nextToken(): Token {
        currentToken = scanner.getToken()
        println(currentToken)
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
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                if (currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.REAL) {
                            nextToken()
                            if (currentToken.symbol == Symbol.RCURLY) {
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
}

fun main() {
    var result = false
    val file = File("examples/01.txt")
    try {
        result = SyntaxAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
        println("No error")
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")
}