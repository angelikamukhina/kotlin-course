//package ru.spbau.mit.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import org.junit.Assert.assertNotNull
import org.junit.Test
import ru.spbau.mit.errorlisteners.ErrorListener
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser

class TestParser {
    @Test(expected = ParseCancellationException::class)
    fun testParsingOfIncorrectProgram() {
        val incorrectProgram = "fun a {\n" +
                "}"

        val funLexer = FunLexer(CharStreams.fromString(incorrectProgram))
        funLexer.removeErrorListeners()
        funLexer.addErrorListener(ErrorListener.INSTANCE)

        val funParser = FunParser(CommonTokenStream(funLexer))
        funParser.removeErrorListeners()
        funParser.addErrorListener(ErrorListener.INSTANCE)
        funParser.file()
    }

    @Test
    fun testFunctionDeclarationParsing() {
        val program = "fun a() {\n" +
                "}"
        val parser = getParser(program)
        // if parsing is correct then mustn't have exception
        assertNotNull(parser.functionDeclaration())
    }

    @Test
    fun testVariableDeclarationParsing() {
        val program = "var i = 0"
        val parser = getParser(program)
        // if parsing is correct then mustn't throw an exception
        assertNotNull(parser.variableDeclaration())
    }

    @Test
    fun testWhileStatementParsing() {
        val program = "var i = 0\n" +
                "while (i < 10) {\n" +
                "    i = i + 1\n" +
                "}\n"
        val parser = getParser(program)
        // if parsing is correct then mustn't throw an exception
        assertNotNull(parser.file().block().statement(1).whileStatement())
    }

    @Test
    fun testIfStatementParsing() {
        val program = "if(5 < 10) {\n" +
                "    println(1)\n" +
                "}"
        val parser = getParser(program)
        assertNotNull(parser.ifStatement().blockWithBraces(0))
    }

    @Test
    fun testAssignmentParsing() {
        val program = "var i\n" +
                "i = 10\n"
        val parser = getParser(program)
        assertNotNull(parser.assignment())
    }

    @Test
    fun testReturnStatementParsing() {
        val program = "return 0"
        val parser = getParser(program)
        assertNotNull(parser.returnStatement())
    }

    @Test
    fun testFunctionCallExpressionParsing() {
        val program = "foo(n)"
        val parser = getParser(program)
        assertNotNull(parser.expression())
    }

    @Test
    fun testBinaryExpressionParsing() {
        val program = "a + b"
        val parser = getParser(program)
        assertNotNull(parser.expression())
    }

    @Test
    fun testIdentifierParsing() {
        val id = "var1111"
        assertNotNull(getParser(id).expression())
    }

    @Test
    fun testNumberParsing() {
        val id = "1111"
        assertNotNull(getParser(id).expression())
    }

    private fun getParser(program: String): FunParser {
        val funLexer = FunLexer(CharStreams.fromString(program))
        funLexer.removeErrorListeners()
        funLexer.addErrorListener(ErrorListener.INSTANCE)

        val funParser = FunParser(CommonTokenStream(funLexer))
        funParser.removeErrorListeners()
        return funParser
    }
}
