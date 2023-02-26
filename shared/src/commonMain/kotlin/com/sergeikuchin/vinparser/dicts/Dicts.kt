package com.sergeikuchin.vinparser.dicts

internal const val UNKNOWN = "Unknown"

internal val WEIGHTS = intArrayOf(8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2)

internal val VALUE_MAP = mapOf(
    'A' to 1, 'B' to 2, 'C' to 3, 'D' to 4, 'E' to 5, 'F' to 6,
    'G' to 7, 'H' to 8, 'J' to 1, 'K' to 2, 'L' to 3, 'M' to 4,
    'N' to 5, 'P' to 7, 'R' to 9, 'S' to 2, 'T' to 3, 'U' to 4,
    'V' to 5, 'W' to 6, 'X' to 7, 'Y' to 8, 'Z' to 9, '1' to 1,
    '2' to 2, '3' to 3, '4' to 4, '5' to 5, '6' to 6, '7' to 7,
    '8' to 8, '9' to 9, '0' to 0
)

internal val ALLOWED_CHARS = VALUE_MAP.keys.toHashSet()

internal val CHECK_DIGIT_REGIONS = setOf('1', '2', '3', '4', '5')

internal val REGIONS = listOf(
    setOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H') to "Africa",
    setOf('J', 'K', 'L', 'M', 'N', 'P', 'R') to "Asia",
    setOf('S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z') to "Europe",
    CHECK_DIGIT_REGIONS to "North America",
    setOf('6', '7') to "Oceania",
    setOf('8', '9') to "South America"
)

internal fun getRegion(rCode: String): String {
    val rCodeChar = rCode.first()
    for ((codes, region) in REGIONS) {
        if (codes.contains(rCodeChar)) {
            return region
        }
    }
    return UNKNOWN
}

internal fun unpackCountries(countries: Map<String, String>): Map<String, String> {
    val seq = "ABCDEFGHJKLMNPRSTUVWXYZ1234567890"
    val result = mutableMapOf<String, String>()

    for ((code, title) in countries) {
        val first = code[0]
        val from = code[2]
        val to = code[3]

        val allChars = seq.substring(seq.indexOf(from)..seq.indexOf(to))
        for (ch in allChars) {
            val key = first.toString() + ch
            result[key] = title
        }
    }

    return result
}

internal fun getCountry(cCode: String): String = COUNTRIES[cCode] ?: UNKNOWN

internal fun getManufacturer(mCode: String): String {
    val result: String? = MANUFACTURERS[mCode] ?: MANUFACTURERS[mCode.take(2)]
    return result ?: UNKNOWN
}