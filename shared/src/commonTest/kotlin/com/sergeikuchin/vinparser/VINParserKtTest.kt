package com.sergeikuchin.vinparser

import kotlin.test.Test
import kotlin.test.assertEquals

internal class VINParserKtTest {

    @Test
    fun `getInfo VIN is valid`() {
        val testVin = "WBA8H81080A857577"
        assertEquals(
            VINInfoResult.Ok(
                VIN(
                    vin = "WBA8H81080A857577",
                    country = "Germany/West Germany",
                    manufacturer = "BMW",
                    region = "Europe"
                )
            ),
            getInfo(testVin)
        )
    }

    @Test
    fun `VIN subfunctions are correct`() {
        val testVin = "WBA8H8108JA857577"
        val vinObj = (getInfo(testVin) as? VINInfoResult.Ok)?.vin

        val expectedWMI = "WBA"
        assertEquals(expectedWMI, vinObj?.wmi())

        val expectedVDS = "8H8108"
        assertEquals(expectedVDS, vinObj?.vds())

        val expectedVIS = "JA857577"
        assertEquals(expectedVIS, vinObj?.vis())

        assertEquals(false, vinObj?.smallManufacturer())

        val expectedRegionCode = "W"
        assertEquals(expectedRegionCode, vinObj?.regionCode())

        val expectedCountryCode = "WB"
        assertEquals(expectedCountryCode, vinObj?.countryCode())

        val expectedYears = listOf(1988, 2018)
        assertEquals(expectedYears, vinObj?.years())
    }

    @Test
    fun `input is correct`() {
        assertEquals(VINCheckResult.Error(VINError.IncorrectLength), checkValidity(""))
        assertEquals(VINCheckResult.Ok, checkValidity("00000000000000000"))
        assertEquals(
            VINCheckResult.Error(VINError.InvalidCharacters(setOf('O', 'I', 'Q'))),
            checkValidity("0000O000I000Q0000")
        )
    }
}