package com.sergeikuchin.vinparser

import com.sergeikuchin.vinparser.utils.cycleUntil
import kotlin.test.Test
import kotlin.test.assertEquals

internal class KUtilsKtTest {

    @Test
    fun `cycle is infinite until return`() {
        val input = "123"
        val expectedResult = listOf('1', '2', '3', '1', '2', '3', '1', '2')

        val result = mutableListOf<Char>()
        var counter = 0
        input.toCharArray().cycleUntil({ counter == 8 }) { char ->
            result.add(char)
            counter++
        }

        assertEquals(expectedResult, result)
    }
}