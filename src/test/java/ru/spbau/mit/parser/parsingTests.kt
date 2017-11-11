//package ru.spbau.mit.parser

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
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

        val funParser  = FunParser(CommonTokenStream(funLexer))
        funParser.removeErrorListeners()
        funParser.addErrorListener(ErrorListener.INSTANCE)
        funParser.file()
    }


}
