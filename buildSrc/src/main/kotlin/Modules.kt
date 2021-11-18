abstract class NestedModule(private val base: String) {
    fun module(name: String) = "$base$name"
}

object Modules {
    const val ankofork = ":ankofork"

    object UI: NestedModule(":ui") {
        val utils = module(":utils")
    }
}
