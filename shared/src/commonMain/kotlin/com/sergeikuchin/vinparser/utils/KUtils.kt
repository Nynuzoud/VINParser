package com.sergeikuchin.vinparser.utils

internal inline fun <reified T> Any.isInstanceOf(): T? = this as? T

internal inline fun CharArray.cycleUntil(predicate: () -> Boolean, action: (Char) -> Unit) {
    var iterator = iterator()
    while (predicate().not()) {
        if (iterator.hasNext().not()) {
            iterator = iterator()
        }
        action(iterator.next())
    }
}