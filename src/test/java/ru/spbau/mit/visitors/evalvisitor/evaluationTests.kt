package ru.spbau.mit.visitors.evalvisitor

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.Test
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import ru.spbau.mit.parser.FunVisitor
import java.io.StringWriter
import kotlin.test.assertEquals

class TestEvaluation {
    @Test
    fun testBinaryExpressionEvaluation() {
        val program = "println(1 + 2)\n" +
                "println(1 - 2)\n" +
                "println(2 * 3)\n" +
                "println(3 / 3)\n" +
                "println(2 % 3)\n" +
                "println(2 == 3)\n" +
                "println(2 == 2)\n" +
                "println(2 != 2)\n" +
                "println(2 != 3)\n" +
                "println(2 > 3)\n" +
                "println(3 > 2)\n" +
                "println(3 < 2)\n" +
                "println(2 < 3)\n" +
                "println(2 >= 3)\n" +
                "println(3 >= 2)\n" +
                "println(2 <= 3)\n" +
                "println(3 <= 2)\n" +
                "println((2 > 3) || (3 > 2))\n" +
                "println((2 > 3) || (1 > 2))\n" +
                "println((2 > 3) && (3 > 2))\n" +
                "println((3 > 2) && (3 > 2))\n"
        val writer = StringWriter()
        interpretProgram(program, writer)
        val results = writer.buffer.split(" \n")
        assertEquals(listOf("3", "-1", "6", "1", "2", "0", "1", "0", "1",
                "0", "1", "0", "1", "0", "1", "1", "0", "1",
                "0", "0", "1", ""), results)
    }

    @Test
    fun testNegativeNumbers() {
        val program = "println(-1)"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("-1", writer.buffer.split(" \n")[0])
    }

    @Test
    fun testWhileStatementEvaluation() {
        val program = "var i = 0\n" +
                "while (i < 10) {\n" +
                "    i = i + 1\n" +
                "}" +
                "println(i)"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("10", writer.toString().replace(" \n", ""))
    }

    @Test
    fun testReturnOnTopLevel() {
        val program = "return 1"
        val writer = StringWriter()
        val res = interpretProgram(program, writer)
        assertEquals(1, res)
    }

    @Test
    fun testIfStatementEvaluation() {
        val program = "var i = 5\n" +
                "if (i < 10) {\n" +
                "    i = i + 1\n" +
                "}" +
                "println(i)"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("6", writer.toString().replace(" \n", ""))
    }

    @Test
    fun testIfWithElseStatementEvaluation() {
        val program = "var i = 0\n" +
                "if (i > 10) {\n" +
                "    i = i + 1\n" +
                "} else {\n" +
                "    i = i + 2\n" +
                "}\n" +
                "println(i)"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("2", writer.toString().replace(" \n", ""))
    }

    @Test
    fun testFunctionDeclarationStatementEvaluation() {
        val program = "fun fib(n) {\n" +
                "    if (n <= 1) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return fib(n - 1) + fib(n - 2)\n" +
                "}\n" +
                "println(fib(5))"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("8", writer.toString().replace(" \n", ""))
    }

    @Test
    fun testVariableDeclarationStatementEvaluation() {
        val programVarWithValue = "var i = 9\n" +
                "println(i)"
        val writer1 = StringWriter()
        interpretProgram(programVarWithValue, writer1)
        assertEquals("9", writer1.toString().replace(" \n", ""))

        val programVarWithoutValue = "var i\n" +
                "i = 10\n" +
                "println(i)"
        val writer2 = StringWriter()
        interpretProgram(programVarWithoutValue, writer2)
        assertEquals("10", writer2.toString().replace(" \n", ""))
    }

    @Test(expected = UseOfNotDeclaredVariableException::class)
    fun testAssignmentOfUndeclaredVarStatementEvaluation() {
        val programVarWithoutValue = "i = 10"
        interpretProgram(programVarWithoutValue, StringWriter())
    }

    @Test
    fun testFunctionCallEvaluation() {
        val program = "fun fact(n) {\n" +
                "    if (n == 0) {\n" +
                "        return 1\n" +
                "    }\n" +
                "    return n * fact(n - 1)\n" +
                "}\n" +
                "println(fact(5))"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("120", writer.toString().replace(" \n", ""))
    }

    @Test
    fun testFunctionWithoutReturn() {
        val program = "fun foo() {\n" +
                "    var i = 0\n" +
                "}\n" +
                "println(foo())\n"
        val writer = StringWriter()
        interpretProgram(program, writer)
        assertEquals("0", writer.toString().replace(" \n", ""))
    }

    @Test(expected = UseOfNotDefinedVariableException::class)
    fun testUseOfNotDefinedVariable() {
        val program = "var i\n" +
                "println(i)\n"
        val writer = StringWriter()
        interpretProgram(program, writer)
    }

    @Test(expected = UseOfNotDeclaredFunctionException::class)
    fun testUseOfNotDeclaredFunction() {
        val program = "println(foo())"
        val writer = StringWriter()
        interpretProgram(program, writer)
    }

    @Test(expected = ConflictingVariableDeclarationsException::class)
    fun testDuplicateDeclaration() {
        val program = "var a = 2\n" +
                "println(a)\n" +
                "var a = 3\n" +
                "println(a)"
        interpretProgram(program, StringWriter())
    }

    private fun interpretProgram(program: String, writer: StringWriter): Int? {
        val funLexer = FunLexer(CharStreams.fromString(program))
        val funParser = FunParser(CommonTokenStream(funLexer))
        val tree = funParser.file()
        val visitor: FunVisitor<Int?> = FunEvalVisitor(writer)
        return visitor.visit(tree)
    }
}