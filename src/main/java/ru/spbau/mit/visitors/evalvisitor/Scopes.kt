package ru.spbau.mit.visitors.evalvisitor

import ru.spbau.mit.parser.FunParser.BlockWithBracesContext
import java.util.*

class Scopes {
    private val scopes : Stack<Scope> = Stack()
    private var notPreparedScope : Scope? = null

    fun createNotPreparedScope() {
        notPreparedScope = if (scopes.empty()) {
            Scope()
        } else {
            scopes.peek().clone()
        }
    }

    fun getCurrentScope() : Scope {
        return notPreparedScope ?: scopes.peek()
    }

    fun pushCurrentScope() {
        if (notPreparedScope != null) {
            scopes.push(notPreparedScope)
            notPreparedScope = null
        } else {
            if (scopes.empty()) {
                scopes.push(Scope())
            } else {
                scopes.push(scopes.peek().clone())
            }
        }
    }

    fun popScope() {
        scopes.pop()
    }
}

class Scope(private val vars: HashMap<String, VarValue> = HashMap(),
            private val functions: HashMap<String, FuncBody> = HashMap()) : Cloneable {
    private val varsDeclaredInThisScope = mutableListOf<String>()
    override public fun clone(): Scope {
        val newVars = HashMap<String, VarValue>()
        newVars.putAll(vars)
        val newFunctions = HashMap<String, FuncBody>()
        newFunctions.putAll(functions)
        return Scope(newVars, newFunctions)
    }

    fun changeVarValue(name: String, value: Int) {
        val varValue = vars[name] ?:
                throw UseOfNotDeclaredVariableException("Variable " + name + "was not declared")
        varValue.hasValue = true
        varValue.value = value
    }

    fun addNewVarWithValue(name: String, value: Int) {
        if (name in varsDeclaredInThisScope) {
            throw ConflictingVariableDeclarationsException("Variable " + name + "already has declaration")
        } else {
            varsDeclaredInThisScope.add(name)
            vars.put(name, VarValue(true, value))
        }
    }

    fun addNewVarWithoutValue(name: String) {
        if (name in varsDeclaredInThisScope) {
            throw ConflictingVariableDeclarationsException("Variable " + name + "already has declaration")
        } else {
            varsDeclaredInThisScope.add(name)
            vars.put(name, VarValue())
        }
    }

    fun getVarValue(name: String): Int {
        val varValue = vars[name] ?:
                throw UseOfNotDeclaredVariableException("The variable " + name + "was not declared")
        if (varValue.hasValue) {
            return varValue.value
        } else {
            throw UseOfNotDefinedVariableException("The variable " + name + "was not defined")
        }
    }

    fun addNewFunction(name: String, args: List<String>, definition: BlockWithBracesContext) {
        if (name in functions) {
            functions.replace(name, FuncBody(args, definition))
        } else {
            functions.put(name, FuncBody(args, definition))
        }
    }

    fun getFuncBody(name: String): FuncBody {
        val funcBody = functions[name]
        if (funcBody != null) {
            return funcBody
        } else {
            throw UseOfNotDeclaredFunctionException("The function " + name + "was not declared")
        }
    }

    class VarValue(var hasValue: Boolean = false, var value: Int = 0)
    class FuncBody(val argsNames: List<String>, val body: BlockWithBracesContext)
}
