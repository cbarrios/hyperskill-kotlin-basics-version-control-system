package svcs

import java.io.File

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
        val baseDirPath = "vcs"
        val baseDir = File(baseDirPath)
        when (val first = args.first()) {
            "config" -> handleConfig(baseDir, args.getOrNull(1))
            "add" -> handleAdd(baseDir, args.getOrNull(1))
            else -> println(map.getOrDefault(first, "'$first' is not a SVCS command."))
        }
    }
}

fun handleConfig(baseDir: File, second: String?) {
    val configFile = baseDir.resolve("config.txt")
    if (second != null) {
        if (!baseDir.exists()) baseDir.mkdir()
        configFile.createNewFile()
        configFile.writeText("The username is $second.")
        println(configFile.readText())
    } else {
        if (!configFile.exists()) {
            println("Please, tell me who you are.")
        } else {
            println(configFile.readText())
        }
    }
}

fun handleAdd(baseDir: File, second: String?) {
    val indexFile = baseDir.resolve("index.txt")
    if (second != null) {
        if (!File(second).exists()) {
            println("Can't find '$second'.")
            return
        }
        if (!indexFile.exists()) {
            if (!baseDir.exists()) baseDir.mkdir()
            indexFile.createNewFile()
            indexFile.writeText("Tracked files:\n$second")
        } else {
            indexFile.appendText("\n$second")
        }
        println("The file '$second' is tracked.")
    } else {
        if (!indexFile.exists()) {
            println(map["add"])
        } else {
            println(indexFile.readText())
        }
    }
}

fun help() {
    println("These are SVCS commands:")
    for ((key, value) in map) {
        println(key + (" ".repeat(12 - key.length)) + value)
    }
}