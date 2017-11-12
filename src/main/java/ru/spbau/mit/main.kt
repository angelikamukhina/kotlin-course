package ru.spbau.mit

import java.util.*

fun getTitle(amountOfLetters: Int, pattern: String): String {
    val patternChars: CharArray = pattern.toCharArray()
    val patternLength = patternChars.size
    val occupiedLetters = BitSet(amountOfLetters)
    occupiedLetters.set(0, amountOfLetters, false)
    patternChars.forEach { letter ->
        if (letter != '?') {
            occupiedLetters.set(letter - 'a')
        }
    }
    val middle = if (patternLength % 2 == 0)
        patternLength / 2 - 1
    else
        patternLength / 2
    for (i in middle downTo 0) {
        if (patternChars[i] == '?') {
            val symmetricIndex = getSymmetricIndex(i, patternLength)
            if (patternChars[symmetricIndex] == '?') {
                val theLastNotOccupiedLetterIndex = occupiedLetters.previousClearBit(amountOfLetters - 1)
                if (theLastNotOccupiedLetterIndex == -1) {
                    patternChars[i] = 'a'
                } else {
                    patternChars[i] = 'a' + theLastNotOccupiedLetterIndex
                    occupiedLetters.set(theLastNotOccupiedLetterIndex)
                }
                patternChars[symmetricIndex] = patternChars[i]
            } else {
                patternChars[i] = patternChars[symmetricIndex]
            }
        }
    }

    (patternLength - 1 downTo middle)
            .forEach {
                val symmetricIndex = getSymmetricIndex(it, patternLength)
                if (patternChars[it] == '?') {
                    patternChars[it] = patternChars[symmetricIndex]
                }
                if (patternChars[it] != patternChars[symmetricIndex]) {
                    return "IMPOSSIBLE"
                }
            }

    if (occupiedLetters.cardinality() != amountOfLetters) {
        return "IMPOSSIBLE"
    }
    return String(patternChars)
}

private fun getSymmetricIndex(index: Int, length: Int): Int {
    return length - index - 1
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val amountOfLetters: Int = input.nextInt()
    val pattern: String = input.next()
    input.close()
    println(getTitle(amountOfLetters, pattern))
}