package ru.spbau.mit.visitors

import org.antlr.v4.runtime.tree.ErrorNode
import ru.spbau.mit.parser.FunBaseVisitor
import ru.spbau.mit.parser.FunParser
import java.util.*

class FunEvalVisitor : FunBaseVisitor<Int?>() {
    private val scopes = Stack<Scope>()

    override fun visitErrorNode(node: ErrorNode?): Int? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun visitFile(ctx: FunParser.FileContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no program in the file")
        return visitBlock(ctx.block())
    }

    override fun visitBlock(ctx: FunParser.BlockContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no block")
        if (scopes.empty()) {
            scopes.push(Scope())
        } else {
            scopes.push(scopes.peek().copy())
        }
        super.visitChildren(ctx)
        val retStatements = ctx.statement().filter { st -> st.returnStatement() != null }
        if (retStatements.size > 1) throw IllegalArgumentException("Too mach return statements")
        val result =  if (retStatements.isEmpty()) 0 else visitReturnStatement(retStatements[0].returnStatement())
        scopes.pop()
        return result
    }

    override fun visitBlockWithBraces(ctx: FunParser.BlockWithBracesContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no block with braces")
        var result: Int = 0
        for (statement in ctx.block().statement()) {
            val statementRes = visitStatement(statement)
            if (statementRes != null) {
                result = statementRes
                break
            }
        }
        return result
    }

    override fun visitStatement(ctx: FunParser.StatementContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no statement")
        return super.visitChildren(ctx)
    }

    override fun visitFunctionDeclaration(ctx: FunParser.FunctionDeclarationContext?): Int? {
        if (ctx != null) {
            val currentScope = scopes.peek()
            val argNames = ctx.parameterNames().Identifier().map {par -> par.text}
            currentScope.changeOrAddFunc(ctx.Identifier().text, argNames, ctx.blockWithBraces())
            return null
        } else {
            throw IllegalArgumentException("There is no function declaration")
        }
    }

    override fun visitVariableDeclaration(ctx: FunParser.VariableDeclarationContext?): Int? {
        if (ctx != null) {
            val varName = ctx.Identifier().text
            when(ctx.expression()) {
                null -> scopes.peek().addVarNameWithoutValue(varName)
                else -> {
                    val value = visit(ctx.expression())
                    if (value != null) {
                        scopes.peek().changeOrAddVar(varName, value)
                    } else {
                        scopes.peek().addVarNameWithoutValue(varName)
                    }
                }
            }
            return null
        } else {
            throw IllegalArgumentException("There is no variable declaration")
        }
    }

    override fun visitWhileStatement(ctx: FunParser.WhileStatementContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no while statement")
        scopes.push(scopes.peek().clone() as Scope)
        var result: Int? = 0
        while (visit(ctx.expression()) == 1) {
            result = visitBlockWithBraces(ctx.blockWithBraces())
        }
        return result
    }

    override fun visitIfStatement(ctx: FunParser.IfStatementContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no if statement")
        scopes.push(scopes.peek().copy())
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

    override fun visitAssignment(ctx: FunParser.AssignmentContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no assignment")
        val expressionRes = visit(ctx.expression())
        if (expressionRes != null) {
            scopes.peek().changeOrAddVar(ctx.Identifier().text, expressionRes)
        } else {
            scopes.peek().addVarNameWithoutValue(ctx.Identifier().text)
        }
        return null
    }

    override fun visitReturnStatement(ctx: FunParser.ReturnStatementContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no return node")
        return visit(ctx.expression())
    }

    override fun visitFunctionCall(ctx: FunParser.FunctionCallContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no function node")

        if (ctx.Identifier().text == "println") {
            for (expression in ctx.arguments().expression()) {
                print(visit(expression).toString() + " ")
            }
            print("\n")
            return null
        }
        scopes.push(scopes.peek().clone() as Scope)
        val funcBody = scopes.peek().getFuncBody(ctx.Identifier().text)
        for (argIdx in 0 until funcBody.argsNames.size) {
            val expression = ctx.arguments().expression(argIdx)
            val expressionRes = visit(expression)
            if (expressionRes != null) {
                scopes.peek().changeOrAddVar(funcBody.argsNames[argIdx], expressionRes)
            }
        }

        val result = visitBlockWithBraces(funcBody.body)
        scopes.pop()
        return result
    }

    override fun visitOrExpression(ctx: FunParser.OrExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression() != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes != null && secondRes != null) {
                    return if (firstRes + secondRes > 0) 1 else 0
                } else {
                    throw IllegalArgumentException("Wrong arguments for OR")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in or expression are null")
        }
    }

    override fun visitAndExpression(ctx: FunParser.AndExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes != null && secondRes != null) {
                    if (firstRes * secondRes > 0) 1 else 0
                } else {
                    throw IllegalArgumentException("Wrong arguments for AND")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in and expression are null")
        }
    }

    override fun visitEqExpression(ctx: FunParser.EqExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                when (ctx.EqualityOperations().text) {
                    "==" -> if (firstRes == secondRes) 0 else 1
                    "!=" -> if (firstRes != secondRes) 0 else 1
                    else -> throw IllegalArgumentException("Wrong equality operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in eqExpression are null")
        }
    }

    override fun visitRelExpression(ctx: FunParser.RelExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) throw IllegalArgumentException("Wrong arguments for relation")
                when (ctx.RelationOperations().text) {
                    ">" -> if (firstRes > secondRes) 1 else 0
                    "<" -> if (firstRes < secondRes) 1 else 0
                    ">=" -> if (firstRes >= secondRes) 1 else 0
                    "<=" -> if (firstRes <= secondRes) 1 else 0
                    else -> throw IllegalArgumentException("Wrong relation operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in relExpression are null")
        }
    }

    override fun visitAddExpression(ctx: FunParser.AddExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) throw IllegalArgumentException("Wrong arguments for additive operation")
                when (ctx.AdditionOperations().text) {
                    "-" -> firstRes - secondRes
                    "+" -> firstRes + secondRes
                    else -> throw IllegalArgumentException("Wrong mult operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in mult expression are null")
        }
    }

    override fun visitMultExpression(ctx: FunParser.MultExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression(0) != null && ctx.expression(1) != null -> {
                val firstRes = visit(ctx.expression(0))
                val secondRes = visit(ctx.expression(1))
                if (firstRes == null || secondRes == null) throw IllegalArgumentException("Wrong arguments for mult")
                when (ctx.MultiplicationOperations().text) {
                    "*" -> firstRes * secondRes
                    "/" -> firstRes / secondRes
                    "%" -> firstRes % secondRes
                    else -> throw IllegalArgumentException("Wrong mult operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives in mult expression are null")
        }
    }

    override fun visitUnaryExpression(ctx: FunParser.UnaryExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.expression() != null && ctx.expression() != null -> {
                val childrenResult = super.visitChildren(ctx) ?: throw IllegalArgumentException("Wrong arguments for unary operation")
                when(ctx.UnaryOperations().text) {
                    "-" -> -childrenResult
                    "+" -> childrenResult
                    else -> throw IllegalArgumentException("Wrong unary operation")
                }
            }
            else -> throw IllegalArgumentException("All alternatives of unaryExpression are null")
        }

    }

    override fun visitAtomicExpression(ctx: FunParser.AtomicExpressionContext?): Int? {
        if (ctx == null) throw IllegalArgumentException("There is no expression")
        return when {
            ctx.Number() != null -> ctx.Number().text.toInt()
            ctx.expression() != null -> visit(ctx.expression())
            ctx.Identifier() != null -> scopes.peek().getVarValue(ctx.Identifier().text)
            else -> throw IllegalArgumentException("All alternatives are null in atomic expression")
        }
    }
}