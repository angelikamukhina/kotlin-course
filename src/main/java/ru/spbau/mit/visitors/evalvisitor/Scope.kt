package ru.spbau.mit.visitors.evalvisitor

import ru.spbau.mit.parser.FunParser.BlockWithBracesContext

data class Scope(private val vars: HashMap<String, VarValue> = HashMap(),
                 private val functions: HashMap<String, FuncBody> = HashMap()) : Cloneable {

    override public fun clone(): Any {
        val newVars = HashMap<String, VarValue>()
        newVars.putAll(vars)
        val newFunctions = HashMap<String, FuncBody>()
        newFunctions.putAll(functions)
        return Scope(newVars, newFunctions)
    }

    fun changeVarValue(name: String, value: Int) {
        if (name in vars) {
            val varValue = vars[name]
            if (varValue != null) {
                varValue.hasValue = true
                varValue.value = value
                vars.replace(name, varValue)
            }
        } else {
            throw UseOfNotDeclaredVariableException("Variable " + name + "was not declared")
        }
    }

    fun addNewVarWithValue(name: String, value: Int) {
        if (name in vars) {
            vars.replace(name, VarValue(true, value))
        } else {
            vars.put(name, VarValue(true, value))
        }
    }

    fun addNewVarWithoutValue(name: String) {
        if (name in vars) {
            vars.replace(name, VarValue())
        } else {
            vars.put(name, VarValue())
        }
    }

    fun getVarValue(name: String): Int {
        val varValue = vars[name]
        if (varValue != null) {
            when (varValue.hasValue) {
                true -> return varValue.value
                false -> throw UseOfNotDefinedVariableException("The variable " + name + "was not defined")
            }
        } else {
            throw UseOfNotDeclaredVariableException("The variable " + name + "was not declared")
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

    data class VarValue(var hasValue: Boolean = false, var value: Int = 0)
    data class FuncBody(val argsNames: List<String>, val body: BlockWithBracesContext)
}