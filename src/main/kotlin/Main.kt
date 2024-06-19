package si.seljaki

import java.io.File

fun main(args: Array<String>) {
    if(args.size < 2)
        return

    val inputFile = File(args[0])
    val outputFile = File(args[0])
    try {
        val ast = SyntaxAnalyzer(Scanner(Lexicon, inputFile.inputStream())).parse()

        if(!ast.first)
            return

        val variables: MutableMap<String, Double> = mutableMapOf()
        val plots: MutableMap<String, Plot> = mutableMapOf()
        val works: MutableMap<String, Work> = mutableMapOf()

        for (statement in ast.second)
            statement.eval(plots, works, variables)

        convertPlotsAndWorkToGeoJson(plots.values.toList(), works.values.toList(), outputFile)
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
}