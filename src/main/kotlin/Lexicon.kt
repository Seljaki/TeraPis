package si.seljaki

import java.io.File
import java.io.IOException
import java.io.InputStream

const val ERROR_STATE = 0

enum class Symbol { //TODO DONE?
    NAME,
    REAL,
    TIMESTAMP,
    COMMA,
    COLON,
    LPAREN,
    RPAREN,
    LCURLY,
    RCURLY,
    LSQUARE,
    RSQUARE,
    PLOT,
    COORDINATES,
    TYPE,
    FOREST,
    FIELD,
    MEADOW,
    WORK,
    PATH,
    POINT,
    POINTT,
    ACTION,
    MAX_SPEED,
    IMPLEMENT_WIDTH,
    IF,
    IS,
    VALID,
    CONTAINS,
    CALCULATE_PATH,
    CALCULATE_AREA,
    CALCULATE_EFFICIENCY,
    CALCULATE_AVERAGE_SPEED,
    CALCULATE_AREA_COVERED,
    SKIP,
    EOF,

}

const val EOF = -1
const val NEWLINE = '\n'.code

interface DFA {
    val states: Set<Int>
    val alphabet: IntRange
    fun next(state: Int, code: Int): Int
    fun symbol(state: Int): Symbol
    val startState: Int
    val finalStates: Set<Int>
}

object Lexicon : DFA {
    override val states = (1..160).toSet() //TODO
    override val alphabet = 0..255
    override val startState = 1
    override val finalStates = setOf(4,5,7,10,11,12,13,14,15,16,17,18,22,25,30,31,42,48,52,58,62,68,77,82,87,89,90,104,117,124,128,138,149,153,159,160) //TODO

    private val numberOfStates = states.max() + 1 // plus the ERROR_STATE
    private val numberOfCodes = alphabet.max() + 1 // plus the EOF
    private val transitions = Array(numberOfStates) { IntArray(numberOfCodes) }
    private val errorTransitions = Array(numberOfStates) { (numberOfCodes) }

    private val values = Array(numberOfStates) { Symbol.SKIP }

    private fun setTransition(from: Int, chr: Char, to: Int) {
        transitions[from][chr.code + 1] = to // + 1 because EOF is -1 and the array starts at 0
    }

    private fun setTransition(from: Int, code: Int, to: Int) {
        transitions[from][code + 1] = to
    }

    /*private fun setError(from: Int, to: Int) {
        errorTransitions[from] = to // + 1 because EOF is -1 and the array starts at 0
    }*/

    private fun setSymbol(state: Int, symbol: Symbol) {
        values[state] = symbol
    }

    override fun next(state: Int, code: Int): Int {
        assert(states.contains(state))
        //assert(alphabet.contains(code))
        return transitions[state][code + 1]
    }

    override fun symbol(state: Int): Symbol {
        assert(states.contains(state))
        return values[state]
    }

    private fun setTransitionNumberRange(from: Int, to: Int) { //true: crke, false: stevila
        var c ='0'
        while (c <= '9') {
            setTransition(from, c, to)
            ++c
        }
    }

    private fun setStrictTransition(from: Int, fullWord: String, to: Int) {
        var InnerFrom = from
        var innerTo = to
        for (c in fullWord) {
            setTransition(InnerFrom, c, innerTo)
            InnerFrom=innerTo++

        }
    }

    private fun setAllTransition(from: Int,firstChar: Int, lastChar: Int ,to: Int) {
        for (i in firstChar..lastChar) {
            setTransition(from, i.toChar(), to)
        }

    }

    init {
        //SKIP
        setAllTransition(1,0,32,160)
        setAllTransition(160,0,32,160)
        // EOF
        setTransition(1, EOF, 159)
        // NAME- "anything"
        setTransition(1, '"', 2)
        setAllTransition(2,32,126,3)
        setAllTransition(3,32,126,3)
        setTransition(3, '"', 4)
        //REAL
        setTransitionNumberRange(1,5)
        setTransitionNumberRange(5,5)
        setTransition(5, '.', 6)
        setTransitionNumberRange(6,7)
        setTransitionNumberRange(7,7)
        //TIMESTAMP
        setTransition(5, 'T', 8)
        setTransitionNumberRange(8,9)
        setTransitionNumberRange(9,9)
        setTransition(9,'Z',10)
        //BRACKETS AND EXTRA
        setTransition(1, ',', 11)
        setTransition(1, ':', 12)
        setTransition(1, '(', 13)
        setTransition(1, ')', 14)
        setTransition(1, '{', 15)
        setTransition(1, '}', 16)
        setTransition(1, '[', 17)
        setTransition(1, ']', 18)
        //PLOT and PATH
        setStrictTransition(1,"plot",19)
        setStrictTransition(19,"ath",23)
        //Point and PointT
        setStrictTransition(1,"Point",26)
        setStrictTransition(30,"T",31)
        //COORDINATES and CONTAINS
        setStrictTransition(1,"coordinates",32)
        setStrictTransition(33,"ntains",43)
        //TYPE
        setStrictTransition(1,"type",49)
        //FOREST and FIELD
        setStrictTransition(1,"forest",53)
        setStrictTransition(53,"ield",59)
        //ACTION
        setStrictTransition(1,"action",63)
        //MEADOW and MAX-SPEED
        setStrictTransition(1,"max-speed",69)
        setStrictTransition(69,"eadow",78)
        //VALID
        setStrictTransition(1,"valid",83)
        //IF, IS, IMPLEMENT-WIDTH
        setStrictTransition(1,"if",88)
        setTransition(88, 's', 90)
        setStrictTransition(88,"mplement-width",91)
        //CALCULATE_PATH, CALCULATE_AREA, CALCULATE_EFFICIENCY,CALCULATE_AVERAGE_SPEED and CALCULATE_AREA_COVERED
        setStrictTransition(1,"CalculateArea",105)
        setStrictTransition(117,"Covered",118)
        setStrictTransition(114,"verageSpeed",139)
        setStrictTransition(113,"Path",125)
        setStrictTransition(113,"Efficiency",129)
        setStrictTransition(1,"work",150)




        setSymbol(4, Symbol.NAME)
        setSymbol(159, Symbol.EOF)
        setSymbol(5, Symbol.REAL)
        setSymbol(7, Symbol.REAL)
        setSymbol(10, Symbol.TIMESTAMP)
        setSymbol(11, Symbol.COMMA)
        setSymbol(12, Symbol.COLON)
        setSymbol(13, Symbol.LPAREN)
        setSymbol(14, Symbol.RPAREN)
        setSymbol(15, Symbol.LCURLY)
        setSymbol(16, Symbol.RCURLY)
        setSymbol(17, Symbol.LSQUARE)
        setSymbol(18, Symbol.RSQUARE)
        setSymbol(22, Symbol.PLOT)
        setSymbol(25, Symbol.PATH)
        setSymbol(30, Symbol.POINT)
        setSymbol(31, Symbol.POINTT)
        setSymbol(42, Symbol.COORDINATES)
        setSymbol(48, Symbol.CONTAINS)
        setSymbol(52, Symbol.TYPE)
        setSymbol(58, Symbol.FOREST)
        setSymbol(62, Symbol.FIELD)
        setSymbol(68, Symbol.ACTION)
        setSymbol(77, Symbol.MAX_SPEED)
        setSymbol(82, Symbol.MEADOW)
        setSymbol(87, Symbol.VALID)
        setSymbol(89, Symbol.IF)
        setSymbol(90, Symbol.IS)
        setSymbol(104, Symbol.IMPLEMENT_WIDTH)
        setSymbol(117, Symbol.CALCULATE_AREA)
        setSymbol(124, Symbol.CALCULATE_AREA_COVERED)
        setSymbol(128, Symbol.CALCULATE_PATH)
        setSymbol(138, Symbol.CALCULATE_EFFICIENCY)
        setSymbol(149, Symbol.CALCULATE_AVERAGE_SPEED)
        setSymbol(153, Symbol.WORK)
    }


}

data class Token(val symbol: Symbol, val lexeme: String, val startRow: Int, val startColumn: Int)

class Scanner(private val automaton: DFA, private val stream: InputStream) {
    private var last: Int? = null
    private var row = 1
    private var column = 1

    private fun updatePosition(code: Int) {
        if (code == NEWLINE) {
            row += 1
            column = 1
        } else {
            column += 1
        }
    }

    fun getToken(): Token {
        val startRow = row
        val startColumn = column
        val buffer = mutableListOf<Char>()

        var code = last ?: stream.read()
        var state = automaton.startState
        while (true) {
            val nextState = automaton.next(state, code)
            if (nextState == ERROR_STATE) break

            state = nextState
            updatePosition(code)
            buffer.add(code.toChar())
            code = stream.read()
        }
        last = code

        if (automaton.finalStates.contains(state)) {
            val symbol = automaton.symbol(state)
            return if (symbol == Symbol.SKIP) {
                getToken()
            } else {
                val lexeme = String(buffer.toCharArray())
                Token(symbol, lexeme, startRow, startColumn)
            }
        } else {
            throw Error("Invalid pattern at ${row}:${column}, TOKEN: ${automaton.symbol(state)}")
        }
    }
}

fun name(symbol: Symbol) =
    when (symbol) {
        Symbol.NAME -> "name"
        Symbol.REAL -> "real"
        Symbol.TIMESTAMP -> "timestamp"
        Symbol.COMMA -> "comma"
        Symbol.COLON -> "colon"
        Symbol.LPAREN -> "lparen"
        Symbol.RPAREN -> "rparen"
        Symbol.LCURLY -> "lcurly"
        Symbol.RCURLY -> "rcurly"
        Symbol.LSQUARE -> "lsquare"
        Symbol.RSQUARE -> "rsquare"
        Symbol.PLOT -> "plot"
        Symbol.COORDINATES -> "coordinates"
        Symbol.TYPE -> "type"
        Symbol.FOREST -> "forest"
        Symbol.FIELD -> "field"
        Symbol.MEADOW -> "meadow"
        Symbol.WORK -> "work"
        Symbol.PATH -> "path"
        Symbol.POINT -> "Point"
        Symbol.POINTT -> "PointT"
        Symbol.ACTION -> "action"
        Symbol.MAX_SPEED -> "max-speed"
        Symbol.IMPLEMENT_WIDTH -> "implementWidth"
        Symbol.IF -> "if"
        Symbol.IS -> "is"
        Symbol.VALID -> "valid"
        Symbol.CONTAINS -> "contains"
        Symbol.CALCULATE_PATH -> "CalculatePath"
        Symbol.CALCULATE_AREA -> "CalculateArea"
        Symbol.CALCULATE_EFFICIENCY -> "CalculateEfficiency"
        Symbol.CALCULATE_AVERAGE_SPEED -> "CalculateAverageSpeed"
        Symbol.CALCULATE_AREA_COVERED -> "CalculateAreaCovered"
        else -> throw Error("Invalid symbol")
    }


fun printTokens(scanner: Scanner) {
    //val writer = File(fileName).printWriter()

    var token = scanner.getToken()
    while (token.symbol != Symbol.EOF) {
        //println(token)
        println("${name(token.symbol)}(\"${token.lexeme}\") ")
        //writer.append("${name(token.symbol)}(\"${token.lexeme}\") ")
        token = scanner.getToken()
    }
    /*writer.appendLine()
    writer.flush()*/
}

fun returnTokens(args: Array<String>){
    var text=""
    File(args[0]).forEachLine { text +=it }

    try {
        printTokens(Scanner(Lexicon, text.byteInputStream()))
    } catch (e: IOException) {
        println("Error reading the input file: ${e.message}")
    } catch (e: Error) {
        println("Error occurred during tokenization: ${e.message}")
    }
}