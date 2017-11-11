package ru.spbau.mit

import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.misc.ParseCancellationException
import ru.spbau.mit.errorlisteners.ErrorListener
import ru.spbau.mit.parser.FunLexer
import ru.spbau.mit.parser.FunParser
import ru.spbau.mit.parser.FunVisitor
import ru.spbau.mit.visitors.evalvisitor.FunEvalVisitor
import java.io.OutputStreamWriter

fun main(args: Array<String>) {
    val funLexer = FunLexer(CharStreams.fromFileName(args[0]))
    funLexer.removeErrorListeners()
    funLexer.addErrorListener(ErrorListener.INSTANCE)

    val funParser = FunParser(CommonTokenStream(funLexer))
    funParser.removeErrorListeners()
    funParser.addErrorListener(ErrorListener.INSTANCE)

    try {
        val tree = funParser.file()
        val visitor: FunVisitor<Int?> = FunEvalVisitor(OutputStreamWriter(System.out))
        visitor.visit(tree)
        printReturnCode(0)
    } catch (e: ParseCancellationException) {
        System.err.print(e.localizedMessage)
        System.err.flush()
        printReturnCode(1)
    } catch (e: Exception) {
        System.err.print(e.message)
        printReturnCode(1)
    }
}

fun printReturnCode(code: Int) {
    println("\nFunProgram finished with exit code " + code)
}
