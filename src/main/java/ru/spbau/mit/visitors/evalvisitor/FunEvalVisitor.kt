package ru.spbau.mit.visitors.evalvisitor

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser
import java.io.Writer
import java.util.*

class FunEvalVisitor(val writer: Writer) : FunBaseVisitor<Int?>() {
    private val scopes = Stack<Scope>()

    override fun visitFile(ctx: FunParser.FileContext): Int? {
        scopes.push(Scope())
        visitBlock(ctx.block())
        scopes.pop()
        return null
    }

    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        ctx.statement().forEach { statement ->
            run {
                val statementRes = visitStatement(statement)
                if (statementRes != null) {
                    return statementRes
                }
            }
        }
        return null
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext): Int? {
        return visitBlock(ctx.block())
    }

    override fun visitStatement(ctx: FunParser.StatementContext): Int? {
        return super.visitChildren(ctx)
    }

    override fun visitFunctionDeclaration(ctx: FunParser.FunctionDeclarationContext): Int? {
        val currentScope = scopes.peek()
        val argNames = ctx.parameterNames().Identifier().map { par -> par.text }
        currentScope.addNewFunction(ctx.Identifier().text, argNames, ctx.blockWithBraces())
        return null
    }

    override fun visitVariableDeclaration(ctx: FunParser.VariableDeclarationContext): Int? {
        val varName = ctx.Identifier().text
        when (ctx.expression()) {
            null -> scopes.peek().addNewVarWithoutValue(varName)
            else -> {
                val value = visit(ctx.expression())
                if (value != null) {
                    scopes.peek().addNewVarWithValue(varName, value)
                } else {
                    scopes.peek().addNewVarWithoutValue(varName)
                }
            }
        }
        return null
    }

    override fun visitWhileStatement(ctx: FunParser.WhileStatementContext): Int? {
        scopes.push(scopes.peek().clone() as Scope)
        var result: Int? = 0
        while (visit(ctx.expression()) == 1) {
            result = visitBlockWithBraces(ctx.blockWithBraces())
        }
        scopes.pop()
        return result
    }

    override fun visitIfStatement(ctx: FunParser.IfStatementContext): Int? {
        scopes.push(scopes.peek().clone() as Scope)
        val result = if (visit(ctx.expression()) == 1) {
            visitBlockWithBraces(ctx.blockWithBraces()[0])
        } else {
            if (ctx.blockWithBraces().size == 2) {
                visitBlockWithBraces(ctx.blockWithBraces()[1])
            } else {
                null
            }
        }
        scopes.pop()
        return result
    }

    override fun visitAssignment(ctx: FunParser.AssignmentContext): Int? {
        val expressionRes = visit(ctx.expression())
        if (expressionRes != null) {
            scopes.peek().changeVarValue(ctx.Identifier().text, expressionRes)
        } else {
            throw IncorrectProgramException("Trying to assign variable to null")
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext): Int? {
        return visit(ctx.expression())
    }

    override fun visitFunctionCall(ctx: FunParser.FunctionCallContext): Int? {
        if (ctx.Identifier().text == "println") {
            for (expression in ctx.arguments().expression()) {
                writer.write(visit(expression).toString() + " ")
            }
            writer.write("\n")
            writer.flush()
            return null
        }
        scopes.push(scopes.peek().clone() as Scope)
        val funcBody = scopes.peek().getFuncBody(ctx.Identifier().text)
        for (argIdx in 0 until funcBody.argsNames.size) {
            val expression = ctx.arguments().expression(argIdx)
            val expressionRes = visit(expression)
            if (expressionRes != null) {
                scopes.peek().addNewVarWithValue(funcBody.argsNames[argIdx], expressionRes)
            }
        }

        val result = visitBlockWithBraces(funcBody.body)
        scopes.pop()

        if (result == null) {
            return 0
        }
        return result
    }

    override fun visitOrExpression(ctx: FunParser.OrExpressionContext): Int? {
        when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes != null && secondRes != null) {
                    return if (firstRes + secondRes == 1) 1 else 0
                } else {
                    throw IncorrectProgramException("Wrong arguments for OR")
                }
            }
            else -> throw IncorrectProgramException("All alternatives in or expression are null")
        }
    }

    override fun visitAndExpression(ctx: FunParser.AndExpressionContext): Int? {
        when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) {
                    throw IncorrectProgramException("Wrong arguments for AND operation")
                }
                return if (firstRes * secondRes == 1) 1 else 0
            }
            else -> throw IncorrectProgramException("All alternatives in and expression are null")
        }
    }

    override fun visitEqExpression(ctx: FunParser.EqExpressionContext): Int? {
        when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) {
                    throw IncorrectProgramException("Wrong arguments for eq operation")
                }
                return when (ctx.EqualityOperations().text) {
                    "==" -> if (firstRes == secondRes) 1 else 0
                    "!=" -> if (firstRes != secondRes) 1 else 0
                    else -> throw IncorrectProgramException("Wrong equality operation")
                }
            }
            else -> throw IncorrectProgramException("All alternatives in eqExpression are null")
        }
    }

    override fun visitRelExpression(ctx: FunParser.RelExpressionContext): Int? {
        when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) {
                    throw IncorrectProgramException("Wrong arguments for relation")
                }
                return when (ctx.RelationOperations().text) {
                    ">" -> if (firstRes > secondRes) 1 else 0
                    "<" -> if (firstRes < secondRes) 1 else 0
                    ">=" -> if (firstRes >= secondRes) 1 else 0
                    "<=" -> if (firstRes <= secondRes) 1 else 0
                    else -> throw IncorrectProgramException("Wrong relation operation")
                }
            }
            else -> throw IncorrectProgramException("All alternatives in relExpression are null")
        }
    }

    override fun visitAddExpression(ctx: FunParser.AddExpressionContext): Int? {
        when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) {
                    throw IncorrectProgramException("Wrong arguments for additive operation")
                }
                return when (ctx.AdditionOperations().text) {
                    "-" -> firstRes - secondRes
                    "+" -> firstRes + secondRes
                    else -> throw IncorrectProgramException("Wrong mult operation")
                }
            }
            else -> throw IncorrectProgramException("All alternatives in mult expression are null")
        }
    }

    override fun visitMultExpression(ctx: FunParser.MultExpressionContext): Int? {
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) {
                    throw IncorrectProgramException("Wrong arguments for mult")
                }
                when (ctx.MultiplicationOperations().text) {
                    "*" -> firstRes * secondRes
                    "/" -> firstRes / secondRes
                    "%" -> firstRes % secondRes
                    else -> throw IncorrectProgramException("Wrong mult operation")
                }
            }
            else -> throw IncorrectProgramException("All alternatives in mult expression are null")
        }
    }

    override fun visitUnaryExpression(ctx: FunParser.UnaryExpressionContext): Int? {
        return when {
            ctx.expression() != null && ctx.expression() != null -> {
                val childrenResult = super.visitChildren(ctx) ?:
                        throw IllegalArgumentException("Wrong arguments for unary operation")
                when (ctx.UnaryOperations().text) {
                    "-" -> -childrenResult
                    "+" -> childrenResult
                    else -> throw IllegalArgumentException("Wrong unary operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives of unaryExpression are null")
        }
    }

    override fun visitAtomicExpression(ctx: FunParser.AtomicExpressionContext): Int? {
        return when {
            ctx.Number() != null -> ctx.Number().text.toInt()
            ctx.expression() != null -> visit(ctx.expression())
            ctx.Identifier() != null -> scopes.peek().getVarValue(ctx.Identifier().text)
            else -> throw IllegalArgumentException("All alternatives are null in atomic expression")
        }
    }
}