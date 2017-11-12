package ru.spbau.mit.visitors.evalvisitor

class IncorrectProgramException(message: String) : Exception(message)

class UseOfNotDeclaredVariableException(message: String) : Exception(message)
class UseOfNotDefinedVariableException(message: String) : Exception(message)

class UseOfNotDeclaredFunctionException(message: String) : Exception(message)