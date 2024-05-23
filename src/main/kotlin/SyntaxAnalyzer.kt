package si.seljaki

class SyntaxAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()

    fun nextToken(): Token {
        currentToken = scanner.getToken()
        println(currentToken)
        return currentToken
    }

    fun parse(): Boolean {

        return false
    }

    fun PlotDefinition(): Boolean {
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                nextToken()
                if (currentToken.symbol == Symbol.LCRURLY) {
                    nextToken()
                    if(PlotBody()) {
                        if (currentToken.symbol == Symbol.RCURCLY) {
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

    }

    fun PlotBody2(): Boolean {

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

    }

    fun Point(): Boolean {
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol = Symbol.LCRURLY) {
                nextToken()
                if (currentToken.symbol == Symbol.REAL) {
                    nextToken()
                    if (currentToken.symbol == Symbol.)
                }
            }
            throw IllegalStateException("Encountered invalid token: $currentToken")
        }
        return false
    }
}