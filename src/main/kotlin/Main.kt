package si.seljaki

import java.io.File

fun main(args: Array<String>) {
    if(args.size < 2)
        return

    var result = false
    val file = File(args[0])
    try {
        result = SemanticAnalyzer(Scanner(Lexicon, file.inputStream())).parse(args[1])
        println("No error")
        println(result)
    } catch (e: Exception) {
        println("Error")
        println(e)
    }
    println("Is correct: $result")
}