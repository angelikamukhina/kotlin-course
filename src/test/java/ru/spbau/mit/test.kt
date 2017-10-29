package ru.spbau.mit

import org.junit.Test
import kotlin.test.assertEquals

class TestSource {
    @Test
    fun testGetTitle() {
        assertEquals("IMPOSSIBLE", getTitle(3, "a?c"))
        assertEquals("abba", getTitle(2, "a??a"))
        assertEquals("abba", getTitle(2, "?b?a"))
    }
}
