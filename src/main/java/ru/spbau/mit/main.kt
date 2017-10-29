package ru.spbau.mit

import java.util.*

fun getTitle(amountOfLetters: Int, pattern: String): String {
    val patternChars: CharArray = pattern.toCharArray()
    val patternLength = patternChars.size
    val occupiedLetters = mutableListOf<Boolean>()
    for (index in 0 until amountOfLetters) {
        occupiedLetters.add(index, false)
    }
    patternChars.forEach { letter ->
        run {
            if (letter != '?') {
                occupiedLetters[letter - 'a'] = true
            }
        }
    }
    val startIndex = if (patternLength % 2 == 0)
        patternLength / 2 - 1
    else
        patternLength / 2
    for (i in startIndex downTo 0) {
        if (patternChars[i] == '?') {
            val symmetricLetterIndex = patternLength - i - 1
            if (patternChars[patternLength - i - 1] == '?') {
                val theLastNotOccupiedLetterIndex = occupiedLetters.lastIndexOf(false)
                if (theLastNotOccupiedLetterIndex == -1) {
                    patternChars[i] = 'a'
                } else {
                    patternChars[i] = 'a' + theLastNotOccupiedLetterIndex
                    occupiedLetters[theLastNotOccupiedLetterIndex] = true
                }
                patternChars[patternLength - i - 1] = patternChars[i]
            } else {
                patternChars[i] = patternChars[symmetricLetterIndex]
            }
        }
    }

    (0 until patternLength)
            .filter { patternChars[it] == '?' }
            .forEach { patternChars[it] = patternChars[patternLength - it - 1] }
    (0 until patternLength)
            .filter { patternChars[it] != patternChars[patternLength - it - 1] }
            .forEach { return "IMPOSSIBLE" }

    if (occupiedLetters.contains(false)) {
        return "IMPOSSIBLE"
    }
    return patternChars.joinToString(separator = "")
}

fun main(args: Array<String>) {
    val input = Scanner(System.`in`)
    val amountOfLetters: Int = input.nextInt()
    val pattern: String = input.next()
    input.close()
    println(getTitle(amountOfLetters, pattern))
}