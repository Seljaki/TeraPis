package tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import si.seljaki.Lexicon
import si.seljaki.Scanner
import si.seljaki.SemanticAnalyzer
import java.io.File

class SemanticAnalyzerTest {
    @Test
    fun testGood() {
        val dir = File("semantika_tests/good")
        val files = dir.listFiles()
        for (file in files) {
            println("\nTesting file ${file.name}")
            var result = false
            try {
                result = SemanticAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
                println("No error")
            } catch (e: Exception) {
                println("Error")
                println(e)
            }
            println(result)
            assertTrue(result)
        }

        @Test
        fun testBad() {
            val dir = File("semantika_tests/bad")
            val files = dir.listFiles()
            for (file in files) {
                println("\nTesting file ${file.name}")
                var result = false
                try {
                    result = SemanticAnalyzer(Scanner(Lexicon, file.inputStream())).parse()
                    println("No error")
                } catch (e: Exception) {
                    println("Error")
                    println(e)
                }
                println(result)
                assertFalse(result)
            }
        }
    }
}
