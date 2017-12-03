package ru.spbau.mit.visitors.evalvisitor

import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser
import java.io.Writer

class FunEvalVisitor(private val writer: Writer) : FunBaseVisitor<Int?>() {
    private val scopes = Scopes()

    override fun visitFile(ctx: FunParser.FileContext): Int? {
        scopes.pushCurrentScope()
        val result = visitBlock(ctx.block())
        scopes.popScope()
        return result
    }

    override fun visitBlock(ctx: FunParser.BlockContext): Int? {
        for (statement in ctx.statement()) {
            if (statement.returnStatement() != null) {
                return visit(statement.returnStatement())
            }
            val statementRes = visitStatement(statement)
            if (statementRes != null) {
                return statementRes
            }
        }
        return null
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext): Int? {
        scopes.pushCurrentScope()
        val result = visitBlock(ctx.block())
        scopes.popScope()
        return result
    }

    override fun visitFunctionDeclaration(ctx: FunParser.FunctionDeclarationContext): Int? {
        val currentScope = scopes.getCurrentScope()
        val argNames = ctx.parameterNames().Identifier().map { par -> par.text }
        currentScope.addNewFunction(ctx.Identifier().text, argNames, ctx.blockWithBraces())
        return null
    }

    override fun visitVariableDeclaration(ctx: FunParser.VariableDeclarationContext): Int? {
        val varName = ctx.Identifier().text
        val currentScope = scopes.getCurrentScope()
        if (ctx.expression() == null) {
            currentScope.addNewVarWithoutValue(varName)
        } else {
            val value = visit(ctx.expression())
            if (value != null) {
                currentScope.addNewVarWithValue(varName, value)
            } else {
                currentScope.addNewVarWithoutValue(varName)
            }
        }
        return null
    }

    override fun visitWhileStatement(ctx: FunParser.WhileStatementContext): Int? {
        scopes.createNotPreparedScope()
        fun evalExpression(): Boolean {
            scopes.createNotPreparedScope()
            val result = (visit(ctx.expression()) == 1)
            if (!result) {
                scopes.pushCurrentScope()
            }
            return result
        }
        while (evalExpression()) {
            val result = visitBlockWithBraces(ctx.blockWithBraces())
            if (result != null) return result
        }
        return null
    }

    override fun visitIfStatement(ctx: FunParser.IfStatementContext): Int? {
        scopes.createNotPreparedScope()
        return if (visit(ctx.expression()) == 1) {
            visitBlockWithBraces(ctx.blockWithBraces()[0])
        } else {
            if (ctx.blockWithBraces().size == 2) {
                visitBlockWithBraces(ctx.blockWithBraces()[1])
            } else {
                null
            }
        }
    }

    override fun visitAssignment(ctx: FunParser.AssignmentContext): Int? {
        val expressionRes = visit(ctx.expression())
        if (expressionRes != null) {
            scopes.getCurrentScope().changeVarValue(ctx.Identifier().text, expressionRes)
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
        scopes.createNotPreparedScope()
        val funcBody = scopes.getCurrentScope().getFuncBody(ctx.Identifier().text)
        for (argIdx in 0 until funcBody.argsNames.size) {
            val expression = ctx.arguments().expression(argIdx)
            val expressionRes = visit(expression)
            if (expressionRes != null) {
                scopes.getCurrentScope().addNewVarWithValue(funcBody.argsNames[argIdx], expressionRes)
            }
        }

        return visitBlockWithBraces(funcBody.body) ?: return 0
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

    override fun visitLiteral(ctx: FunParser.LiteralContext): Int? {
        return ctx.Number().text.toInt()
    }

    override fun visitVariableIdentifier(ctx: FunParser.VariableIdentifierContext): Int? {
        return scopes.getCurrentScope().getVarValue(ctx.Identifier().text)
    }

    override fun visitAtomicExpression(ctx: FunParser.AtomicExpressionContext): Int? {
        return visit(ctx.expression())
    }
}