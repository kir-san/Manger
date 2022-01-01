abstract class NestedModule(private val base: String) {
    fun module(name: String) = ":$base:$name"
}

object Modules {
    object UI: NestedModule("ui") {
        val viewer = module("viewer")
        val utils = module("utils")
        val latest = module("latest")
    }

    object Core : NestedModule("core") {
        val support = module("support")
        val utils = module("utils")
        val internet = module("internet")
        val download = module("download")
    }

    object Data : NestedModule("data") {
        val db = module("db")
        val parsing = module("parsing")
        val models = module("models")
        val store = module("store")
    }
}
