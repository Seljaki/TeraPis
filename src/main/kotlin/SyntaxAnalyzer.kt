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

        }
        return false
    }
}