package si.seljaki

import java.io.File

sealed class ASTNode

data class PlotDefinitionNode(val name: String, val body: PlotBodyNode) : ASTNode()
data class WorkDefinitionNode(val name: String, val body: WorkBodyNode) : ASTNode()
data class IfNode(val condition: IfConditionNode, val body: List<ASTNode>) : ASTNode()
data class FunctionCallNode(val name: String, val params: List<String>) : ASTNode()
data class VariableAssignmentNode(val name: String, val expr: ExprNode) : ASTNode()

sealed class PlotBodyNode : ASTNode()
data class CoordinatesNode(val points: List<PointNode>) : PlotBodyNode()
data class PlotTypeNode(val type: String) : PlotBodyNode()

sealed class WorkBodyNode : ASTNode()
data class PathNode(val points: List<PointTNode>) : WorkBodyNode()
data class ActionNode(val action: String) : WorkBodyNode()
data class MaxSpeedNode(val speed: ExprNode) : WorkBodyNode()
data class ImplementWidthNode(val width: ExprNode) : WorkBodyNode()
data class WorkPlotNode(val plotName: String) : WorkBodyNode()

data class PointNode(val x: ExprNode, val y: ExprNode) : ASTNode()
data class PointTNode(val x: ExprNode, val y: ExprNode, val timestamp: String) : ASTNode()

sealed class IfConditionNode : ASTNode()
data class IfIsValidNode(val plotName: String) : IfConditionNode()
data class IfContainsNode(val plotName: String, val workName: String) : IfConditionNode()

sealed class ExprNode : ASTNode()
data class RealNode(val value: Double) : ExprNode()
data class VariableNode(val name: String) : ExprNode()
data class BinaryOpNode(val left: ExprNode, val op: String, val right: ExprNode) : ExprNode()
data class UnaryOpNode(val op: String, val expr: ExprNode) : ExprNode()

class SemanticAnalyzer(private val scanner: Scanner) {
    private var currentToken: Token = scanner.getToken()
    private val declaredPlots = mutableSetOf<String>()
    private val declaredWorks = mutableSetOf<String>()
    private val declaredVariables = mutableSetOf<String>()
    private val semanticErrors = mutableListOf<String>()
    private val astNodes = mutableListOf<ASTNode>()

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
                val body = PlotBody()
                if (body != null && currentToken.symbol == Symbol.RCURLY) {
                    nextToken()
                    astNodes.add(PlotDefinitionNode(plotName, body))
                    return true
                }
            }
        }
        semanticErrors.add("Invalid plot definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun PlotBody(): PlotBodyNode? {
        println("Entering PlotBody with token: $currentToken")
        val elements = mutableListOf<PlotBodyNode>()
        while (true) {
            val element = PlotBody2() ?: break
            elements.add(element)
            if (currentToken.symbol != Symbol.COMMA) break
            nextToken()
        }
        return if (elements.isNotEmpty()) elements[0] else null
    }

    fun PlotBody2(): PlotBodyNode? {
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
                null
            }
        }
    }

    fun Coordinates(): CoordinatesNode? {
        println("Entering Coordinates with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                val points = mutableListOf<PointNode>()
                while (true) {
                    val point = Point() ?: break
                    points.add(point)
                    if (currentToken.symbol != Symbol.COMMA) break
                    nextToken()
                }
                if (currentToken.symbol == Symbol.RSQUARE) {
                    nextToken()
                    return CoordinatesNode(points)
                }
            }
        }
        semanticErrors.add("Invalid coordinates definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun Point(): PointNode? {
        println("Entering Point with token: $currentToken")
        if (currentToken.symbol == Symbol.POINT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val x = expr() ?: return null
                if (currentToken.symbol == Symbol.COMMA) {
                    nextToken()
                    val y = expr() ?: return null
                    if (currentToken.symbol == Symbol.RPAREN) {
                        nextToken()
                        return PointNode(x, y)
                    }
                }
            }
        }
        semanticErrors.add("Invalid point definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun PlotType(): PlotTypeNode? {
        println("Entering PlotType with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            return when (currentToken.symbol) {
                Symbol.FOREST, Symbol.FIELD -> {
                    val type = currentToken.lexeme
                    nextToken()
                    PlotTypeNode(type)
                }
                else -> {
                    semanticErrors.add("Invalid plot type at ${currentToken.startRow}:${currentToken.startColumn}")
                    null
                }
            }
        }
        semanticErrors.add("Invalid plot type definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun WorkDefinition(): Boolean {
        println("Entering WorkDefinition with token: $currentToken")
        if (currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            declaredWorks.add(workName)
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val body = WorkBody()
                if (body != null && currentToken.symbol == Symbol.RCURLY) {
                    nextToken()
                    astNodes.add(WorkDefinitionNode(workName, body))
                    return true
                }
            }
        }
        semanticErrors.add("Invalid work definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun WorkBody(): WorkBodyNode? {
        println("Entering WorkBody with token: $currentToken")
        val elements = mutableListOf<WorkBodyNode>()
        while (true) {
            val element = WorkBody2() ?: break
            elements.add(element)
            if (currentToken.symbol != Symbol.COMMA) break
            nextToken()
        }
        return if (elements.isNotEmpty()) elements[0] else null
    }

    fun WorkBody2(): WorkBodyNode? {
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
                null
            }
        }
    }

    fun Path(): PathNode? {
        println("Entering Path with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.LSQUARE) {
                nextToken()
                val points = mutableListOf<PointTNode>()
                while (true) {
                    val point = Pointt() ?: break
                    points.add(point)
                    if (currentToken.symbol != Symbol.COMMA) break
                    nextToken()
                }
                if (currentToken.symbol == Symbol.RSQUARE) {
                    nextToken()
                    return PathNode(points)
                }
            }
        }
        semanticErrors.add("Invalid path definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun Pointt(): PointTNode? {
        println("Entering Pointt with token: $currentToken")
        if (currentToken.symbol == Symbol.POINTT) {
            nextToken()
            if (currentToken.symbol == Symbol.LPAREN) {
                nextToken()
                val x = expr() ?: return null
                if (currentToken.symbol == Symbol.COMMA) {
                    nextToken()
                    val y = expr() ?: return null
                    if (currentToken.symbol == Symbol.COMMA) {
                        nextToken()
                        if (currentToken.symbol == Symbol.TIMESTAMP) {
                            val timestamp = currentToken.lexeme
                            nextToken()
                            if (currentToken.symbol == Symbol.RPAREN) {
                                nextToken()
                                return PointTNode(x, y, timestamp)
                            }
                        }
                    }
                }
            }
        }
        semanticErrors.add("Invalid PointT definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun Action(): ActionNode? {
        println("Entering Action with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val action = currentToken.lexeme
                nextToken()
                return ActionNode(action)
            }
        }
        semanticErrors.add("Invalid action definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun MaxSpeed(): MaxSpeedNode? {
        println("Entering MaxSpeed with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            val speed = expr() ?: return null
            return MaxSpeedNode(speed)
        }
        semanticErrors.add("Invalid max-speed definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun ImplementWidth(): ImplementWidthNode? {
        println("Entering ImplementWidth with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            val width = expr() ?: return null
            return ImplementWidthNode(width)
        }
        semanticErrors.add("Invalid implement-width definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun WorkPlot(): WorkPlotNode? {
        println("Entering WorkPlot with token: $currentToken")
        if (currentToken.symbol == Symbol.COLON) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val plotName = currentToken.lexeme
                if (plotName in declaredPlots) {
                    nextToken()
                    return WorkPlotNode(plotName)
                } else {
                    semanticErrors.add("Referenced undeclared plot '$plotName' at ${currentToken.startRow}:${currentToken.startColumn}")
                    return null
                }
            }
        }
        semanticErrors.add("Invalid work plot definition at ${currentToken.startRow}:${currentToken.startColumn}")
        return null
    }

    fun If(): Boolean {
        println("Entering If with token: $currentToken")
        if (currentToken.symbol == Symbol.PLOT) {
            nextToken()
            if (currentToken.symbol == Symbol.NAME) {
                val plotName = currentToken.lexeme
                if (plotName in declaredPlots) {
                    nextToken()
                    return If2(plotName)
                } else {
                    semanticErrors.add("Referenced undeclared plot in IF statement at ${currentToken.startRow}:${currentToken.startColumn}")
                    return false
                }
            }
        }
        semanticErrors.add("Invalid IF statement at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun If2(plotName: String): Boolean {
        println("Entering If2 with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.IS -> {
                nextToken()
                IfIsValid(plotName)
            }
            Symbol.CONTAINS -> {
                nextToken()
                IfContains(plotName)
            }
            else -> {
                semanticErrors.add("Invalid IF condition at ${currentToken.startRow}:${currentToken.startColumn}")
                false
            }
        }
    }

    fun IfIsValid(plotName: String): Boolean {
        println("Entering IfIsValid with token: $currentToken")
        if (currentToken.symbol == Symbol.VALID) {
            nextToken()
            if (currentToken.symbol == Symbol.LCURLY) {
                nextToken()
                val body = mutableListOf<ASTNode>()
                while (Statements()) {
                    body.add(astNodes.removeAt(astNodes.size - 1))
                }
                if (currentToken.symbol == Symbol.RCURLY) {
                    nextToken()
                    astNodes.add(IfNode(IfIsValidNode(plotName), body))
                    return true
                }
            }
        }
        semanticErrors.add("Invalid IF is valid statement at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun IfContains(plotName: String): Boolean {
        println("Entering IfContains with token: $currentToken")
        if (currentToken.symbol == Symbol.NAME) {
            val workName = currentToken.lexeme
            if (workName in declaredWorks) {
                nextToken()
                if (currentToken.symbol == Symbol.LCURLY) {
                    nextToken()
                    val body = mutableListOf<ASTNode>()
                    while (Statements()) {
                        body.add(astNodes.removeAt(astNodes.size - 1))
                    }
                    if (currentToken.symbol == Symbol.RCURLY) {
                        nextToken()
                        astNodes.add(IfNode(IfContainsNode(plotName, workName), body))
                        println("Exiting IfContains with success")
                        return true
                    } else {
                        semanticErrors.add("Expected '}' at ${currentToken.startRow}:${currentToken.startColumn}")
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
                                "CalculateEfficiency", "CalculatePath" -> firstParam in declaredPlots && secondParam in declaredWorks
                                else -> {
                                    semanticErrors.add("Unknown function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                                    false
                                }
                            }
                            if (validParams) {
                                nextToken()
                                println("Function $functionName processed successfully")
                                astNodes.add(FunctionCallNode(functionName, listOf(firstParam, secondParam)))
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
                        "CalculateAverageSpeed", "CalculateAreaCovered" -> firstParam in declaredWorks
                        else -> {
                            semanticErrors.add("Unknown function $functionName at ${currentToken.startRow}:${currentToken.startColumn}")
                            false
                        }
                    }
                    if (validParams) {
                        nextToken()
                        println("Function $functionName processed successfully")
                        astNodes.add(FunctionCallNode(functionName, listOf(firstParam)))
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
                val expr = expr() ?: return false
                declaredVariables.add(varName)
                astNodes.add(VariableAssignmentNode(varName, expr))
                return true
            }
        }
        semanticErrors.add("Invalid variable assignment at ${currentToken.startRow}:${currentToken.startColumn}")
        return false
    }

    fun expr(): ExprNode? {
        println("Entering expr with token: $currentToken")
        return additive()
    }

    fun additive(): ExprNode? {
        println("Entering additive with token: $currentToken")
        var left = multiplicative() ?: return null
        while (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            val op = currentToken.lexeme
            nextToken()
            val right = multiplicative() ?: return null
            left = BinaryOpNode(left, op, right)
        }
        return left
    }

    fun multiplicative(): ExprNode? {
        println("Entering multiplicative with token: $currentToken")
        var left = exponential() ?: return null
        while (currentToken.symbol == Symbol.MULTIPLY || currentToken.symbol == Symbol.DIVIDE) {
            val op = currentToken.lexeme
            nextToken()
            val right = exponential() ?: return null
            left = BinaryOpNode(left, op, right)
        }
        return left
    }

    fun exponential(): ExprNode? {
        println("Entering exponential with token: $currentToken")
        var left = unary() ?: return null
        while (currentToken.symbol == Symbol.POW) {
            val op = currentToken.lexeme
            nextToken()
            val right = unary() ?: return null
            left = BinaryOpNode(left, op, right)
        }
        return left
    }

    fun unary(): ExprNode? {
        println("Entering unary with token: $currentToken")
        return if (currentToken.symbol == Symbol.PLUS || currentToken.symbol == Symbol.MINUS) {
            val op = currentToken.lexeme
            nextToken()
            val expr = primary() ?: return null
            UnaryOpNode(op, expr)
        } else {
            primary()
        }
    }

    fun primary(): ExprNode? {
        println("Entering primary with token: $currentToken")
        return when (currentToken.symbol) {
            Symbol.REAL -> {
                val value = currentToken.lexeme.toDouble()
                nextToken()
                RealNode(value)
            }
            Symbol.VARIABLE -> {
                val varName = currentToken.lexeme
                if (varName in declaredVariables) {
                    nextToken()
                    VariableNode(varName)
                } else {
                    semanticErrors.add("Undeclared variable '$varName' used at ${currentToken.startRow}:${currentToken.startColumn}")
                    null
                }
            }
            Symbol.LPAREN -> {
                nextToken()
                val expr = additive() ?: return null
                if (currentToken.symbol == Symbol.RPAREN) {
                    nextToken()
                    expr
                } else {
                    semanticErrors.add("Expected ')' at ${currentToken.startRow}:${currentToken.startColumn}")
                    null
                }
            }
            else -> null
        }
    }

    fun getSemanticErrors(): List<String> {
        return semanticErrors
    }

    fun getASTNodes(): List<ASTNode> {
        return astNodes
    }
}

fun main() {
    val file = File("semantika_tests/05.txt")
    try {
        val analyzer = SemanticAnalyzer(Scanner(Lexicon, file.inputStream()))
        val result = analyzer.parse()
        if (analyzer.getSemanticErrors().isEmpty()) {
            println("No error")
            println("Is correct: $result")
            println("AST Nodes: ${analyzer.getASTNodes()}")
        } else {
            println("Semantic errors found:")
            analyzer.getSemanticErrors().forEach { println(it) }
        }
    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}
