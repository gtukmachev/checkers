package tga.checkers.exts

import java.time.Duration

fun Number.sec(): Duration = Duration.ofSeconds(this.toLong())
fun Number.millis(): Duration = Duration.ofMillis(this.toLong())
fun Number.min(): Duration = Duration.ofMinutes(this.toLong())
