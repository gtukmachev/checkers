package tga.checkers.exts

import java.time.Duration

fun sleep(d: Duration) = Thread.sleep( d.toMillis() )
