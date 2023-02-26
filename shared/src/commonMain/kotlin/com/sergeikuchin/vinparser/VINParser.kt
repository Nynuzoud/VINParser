package com.sergeikuchin.vinparser

import com.sergeikuchin.vinparser.dicts.ALLOWED_CHARS
import com.sergeikuchin.vinparser.dicts.CHECK_DIGIT_REGIONS
import com.sergeikuchin.vinparser.dicts.UNKNOWN
import com.sergeikuchin.vinparser.dicts.VALUE_MAP
import com.sergeikuchin.vinparser.dicts.WEIGHTS
import com.sergeikuchin.vinparser.dicts.getCountry
import com.sergeikuchin.vinparser.dicts.getManufacturer
import com.sergeikuchin.vinparser.dicts.getRegion
import com.sergeikuchin.vinparser.utils.cycleUntil
import com.sergeikuchin.vinparser.utils.isInstanceOf
import kotlin.math.roundToInt

data class ChecksumErrorInfo(
    val expected: Char,
    val received: Char,
)

sealed interface VINCheckResult {
    object Ok : VINCheckResult
    data class Error(val error: VINError) : VINCheckResult
}

sealed interface VINError {
    object IncorrectLength : VINError
    data class InvalidCharacters(val chars: Set<Char>) : VINError
    data class ChecksumError(val err: ChecksumErrorInfo) : VINError
    data class UnknownManufacturer(val source: String) : VINError
    data class UnknownCountry(val source: String) : VINError
}

sealed interface VINInfoResult {
    data class Ok(val vin: VIN) : VINInfoResult
    data class Error(val vinError: VINError) : VINInfoResult
}

data class VIN(
    val vin: String,
    val country: String,
    val manufacturer: String,
    val region: String
) {
    fun wmi() = vin.substring(0..2)
    fun vds() = vin.substring(3..8)
    fun vis() = vin.substring(9..16)
    fun smallManufacturer() = wmi()[2] == '9'
    fun regionCode() = wmi().substring(0, 1)
    fun countryCode() = wmi().substring(0, 2)
    fun years(): List<Int> {
        val letters = "ABCDEFGHJKLMNPRSTVWXY123456789"
        val yearLetter = vis()[0]

        var year = 1979
        val curSeconds = currentTimeMs() / 1000.0
        val curYear = curSeconds / (3600.0 * 24.0 * 365.25) + 1970.0
        val curYearInt = (curYear.roundToInt() + 2)
        val result = mutableListOf<Int>()

        letters.toCharArray().cycleUntil({ year == curYearInt }) {
            year++

            if (it == yearLetter) {
                result.add(year)
            }
        }

        return result
    }
}

fun checkValidity(vin: String): VINCheckResult {
    val upperCaseVin = vin.uppercase()

    // check length
    if (upperCaseVin.length != 17) {
        return VINCheckResult.Error(VINError.IncorrectLength)
    }

    // check alphabet
    val usedChars = upperCaseVin.toSet()
    val oddChars = usedChars - ALLOWED_CHARS
    if (oddChars.isNotEmpty()) {
        return VINCheckResult.Error(VINError.InvalidCharacters(oddChars))
    }

    return VINCheckResult.Ok
}

fun verifyChecksum(vin: String): VINCheckResult {
    val upperCaseVin = vin.uppercase()
    checkValidity(upperCaseVin)

    // verify checksum
    var checksum = 0
    upperCaseVin.forEachIndexed { i, c ->
        checksum += WEIGHTS[i] * VALUE_MAP[c]!!
    }
    val expectedChecksum = checksum % 11
    val receivedChecksum = upperCaseVin[8]

    if (expectedChecksum == 10 && receivedChecksum == 'X' ||
        expectedChecksum == receivedChecksum.code - 48
    ) {
        return VINCheckResult.Ok
    }

    val errorInfo = ChecksumErrorInfo(
        expected = ('0' + expectedChecksum),
        received = receivedChecksum,
    )
    return VINCheckResult.Error(VINError.ChecksumError(errorInfo))
}

fun getInfo(vin: String): VINInfoResult {
    val vinUpper = vin.uppercase()

    checkValidity(vinUpper).isInstanceOf<VINCheckResult.Error>()
        ?.let { return VINInfoResult.Error(it.error) }

    val regionCode = vinUpper[0]
    val region = getRegion(regionCode.toString())
    verifyChecksum(vinUpper)
        .takeIf { CHECK_DIGIT_REGIONS.contains(regionCode) }
        ?.isInstanceOf<VINCheckResult.Error>()
        ?.let { return VINInfoResult.Error(it.error) }

    val countryCode = vinUpper.substring(0, 2)
    val country = getCountry(countryCode)
        .takeIf { it != UNKNOWN }
        ?: return VINInfoResult.Error(VINError.UnknownCountry(countryCode))

    val manufacturerCode = vinUpper.substring(0, 3)
    val manufacturer = getManufacturer(manufacturerCode)
        .takeIf { it != UNKNOWN }
        ?: return VINInfoResult.Error(VINError.UnknownManufacturer(manufacturerCode))

    return VINInfoResult.Ok(
        VIN(
            vinUpper,
            country,
            manufacturer,
            region
        )
    )
}
