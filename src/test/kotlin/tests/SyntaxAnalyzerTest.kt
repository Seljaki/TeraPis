package tests

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import si.seljaki.Lexicon
import si.seljaki.Scanner
import si.seljaki.SyntaxAnalyzer
import java.io.File

class SyntaxAnalyzerTest {
    @Test
    fun testGood() {
        println("***** TESTING GOOD *****")
        val dir = File("syntax_analyzer_tests/good")
        val files = dir.listFiles()
        for (file in files) {
            println("Testing file ${file.name}")
            var result = false
            try {
                result = SyntaxAnalyzer(Scanner(Lexicon, file.inputStream())).parse().first
                println("No error")
            } catch (e: Exception) {
                println("Error")
                println(e)
            }
            println(result)
            assertTrue(result)
        }
    }

    @Test
    fun testBad() {
        println("***** TESTING BAD *****")
        val dir = File("syntax_analyzer_tests/bad")
        val files = dir.listFiles()
        for (file in files) {
            println("Testing file ${file.name}")
            var result = false
            try {
                result = SyntaxAnalyzer(Scanner(Lexicon, file.inputStream())).parse().first
                println("No error")
            } catch (e: Exception) {
                println("Error")
                println(e)
            } catch (e: Error) {

            }
            println(result)
            assertFalse(result)
        }
    }
}