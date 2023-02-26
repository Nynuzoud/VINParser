package com.sergeikuchin.vinparser

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

internal actual fun currentTimeMs(): Double = NSDate().timeIntervalSince1970 * 1000.0