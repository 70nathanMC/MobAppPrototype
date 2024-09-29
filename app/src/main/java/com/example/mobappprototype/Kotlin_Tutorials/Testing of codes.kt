package com.example.mobappprototype.Kotlin_Tutorials

fun calculate(x: Int, y: Int, operation: (Int, Int) -> Int): Int {
    return operation(x, y)
}

fun sum(x: Int, y: Int) = x + y

fun main() {
    val sumResult = calculate(1, 7, ::sum)
    val mulResult = calculate(1, 7) { a, b -> a * b }
    println("sumResult $sumResult, mulResult $mulResult")
}
