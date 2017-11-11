package ru.spbau.mit.visitors

import ru.spbau.mit.parser.FunParser.BlockWithBracesContext

data class Scope(private val vars: HashMap<String, VarValue> = HashMap(),
                 private val functions: HashMap<String, FuncBody> = HashMap()) : Cloneable{

    override public fun clone(): Any {
        return Scope(vars.clone() as HashMap<String, VarValue>, functions.clone() as HashMap<String, FuncBody>)
    }

    fun changeOrAddVar(name: String, value: Int) {
        if (name in vars) {
            vars.replace(name, VarValue(true, value))
        } else {
            vars.put(name, VarValue(true, value))
        }
    }

    fun addVarNameWithoutValue(name: String) {
        if (name in vars) {
            vars.replace(name, VarValue())
        } else {
            vars.put(name, VarValue())
        }
    }

    fun getVarValue(name: String): Int {
        val varValue = vars[name]
        if (varValue != null && varValue.hasValue) {
            return varValue.value
        } else {
            throw IllegalArgumentException("There is no var value")
        }
    }

    fun changeOrAddFunc(name: String, args: List<String>, definition: BlockWithBracesContext) {
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
            throw IllegalArgumentException("There is no such function")
        }
    }

    data class VarValue(val hasValue: Boolean = false, val value: Int = 0)
    data class FuncBody(val argsNames: List<String>, val body: BlockWithBracesContext)
}