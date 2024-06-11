package si.seljaki

abstract class Expr {
    abstract fun eval(env: MutableMap<String, Any>): Any
}

data class PlotExpr(
    var name: String,
    var coordinates: MutableList<Pair<Double, Double>>,
    var type: PlotType
) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        if (coordinates.isEmpty()) {
            throw IllegalStateException("Plot $name has no coordinates.")
        }
        if (type == PlotType.UNDEFINED) {
            throw IllegalStateException("Plot $name has an undefined type.")
        }
        env["plots"] = (env["plots"] as MutableMap<String, PlotExpr>).apply { put(name, this@PlotExpr) }
        println("Defined plot $name with type $type and coordinates $coordinates")
        return this
    }
}

data class WorkExpr(
    var name: String,
    var path: MutableList<Pair<Double, Double>>,
    var timestamps: MutableList<String>,
    var action: String,
    var maxSpeed: Double,
    var implementWidth: Double,
    var plot: String
) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        if(implementWidth <= 0){
            throw IllegalStateException("Work $name has no implemented width, or it's 0 or less.")

        }
        if(action.isEmpty()){
            throw IllegalStateException("Work $name has no action.")
        }
        env["works"] = (env["works"] as MutableMap<String, WorkExpr>).apply { put(name, this@WorkExpr) }
        println("Defined work $name on plot $plot with action $action")
        return this
    }
}

data class IfExpr(val condition: String, val statements: List<String>) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        println("Evaluating if condition $condition")
        println(statements)
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        val works = env["works"] as MutableMap<String, WorkExpr>
        if(condition == "isValid"){
            if(statements[0] !in plots){
                throw IllegalStateException("$statements is not valid!")
            }
            /*
            val plot = plots[statements[0]]
            if (plot != null) {
                if(!isValidCoordinates(plot.coordinates)){
                    throw IllegalStateException("${plot.name}'s coordinates are not valid!")
                }
            }*/
        }
        if(condition == "contains"){
            println("Inside contains")
            if(statements[0] !in plots && statements[1] !in works){
                throw IllegalStateException("$statements is not valid!")
            }
            val plot = plots[statements[0]]
            val work = works[statements[1]]
            println(plot)
            println(work)
            if (plot != null && work !=null) {
                if(!ifContains(plot.coordinates, work.path)){
                    throw IllegalStateException("$condition is not valid!")
                }else{
                    throw IllegalStateException("$statements is not valid!")
                }
            }else{
                throw IllegalStateException("$statements is not valid!")
            }

        }
        return this
    }
}

data class VariableAssignmentExpr(val variable: String, val value: Double) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        env[variable] = value
        println("Assigned $value to variable $variable")
        return this
    }
}

data class FunctionCallExpr(val name: String, val args: List<String>) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        val works = env["works"] as MutableMap<String, WorkExpr>
        val plots = env["plots"] as MutableMap<String, PlotExpr>
        println("Calling function $name with arguments $args")
        if(name == "calculateAverageSpeed" || name == "calculateAreaCovered"){
            if (args[0] !in works){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            if(name == "calculateAverageSpeed" ){

                //TODO calculateAverageSpeed
            }else{
                //TODO calculateAreaCovered
            }
        }
        else if(name == "calculateArea"){
            if (args[0] !in plots){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            val plot = plots[args[0]]
            val area = calculateArea(plot!!.coordinates)
            println(area)
        }
        else{
            if(args[0] !in plots && args[1] !in works){
                throw IllegalStateException("Function $name has invalid parameters.")
            }
            if(name == "calculateEfficency"){
                //TODO calculateEfficency
            }else{
                //TODO calculatePath
            }
        }
        return this
    }
}
data class RealExpr(val value: Double) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return value
    }
}

data class VarExpr(val name: String) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return env[name] ?: throw IllegalStateException("Undefined variable: $name")
    }
}

data class PlusExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) + (right.eval(env) as Double)
    }
}

data class MinusExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) - (right.eval(env) as Double)
    }
}

data class TimesExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) * (right.eval(env) as Double)
    }
}

data class DivideExpr(val left: Expr, val right: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return (left.eval(env) as Double) / (right.eval(env) as Double)
    }
}

data class PowExpr(val base: Expr, val exponent: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return Math.pow((base.eval(env) as Double), (exponent.eval(env) as Double))
    }
}

data class UnaryMinusExpr(val expr: Expr) : Expr() {
    override fun eval(env: MutableMap<String, Any>): Any {
        return -(expr.eval(env) as Double)
    }
}
class Ref<T>(var value: T)
