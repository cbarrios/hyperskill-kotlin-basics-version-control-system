package svcs

val map = mapOf(
    "config" to "Get and set a username.",
    "add" to "Add a file to the index.",
    "log" to "Show commit logs.",
    "commit" to "Save changes.",
    "checkout" to "Restore a file."
)

fun main(args: Array<String>) {
    if (args.isEmpty() || args.first() == "--help") {
       help()
    } else {
        println(map.getOrDefault(args.first(),"'${args.first()}' is not a SVCS command."))
    }
}

fun help() {
    println("These are SVCS commands:")
    for ((key, value) in map) {
        println(key + (" ".repeat(12 - key.length)) + value)
    }
}