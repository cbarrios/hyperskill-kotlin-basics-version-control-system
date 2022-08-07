package svcs

import java.io.File
import java.security.MessageDigest

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
            "commit" -> handleCommit(baseDir, args.getOrNull(1))
            "log" -> handleLog(baseDir)
            "checkout" -> handleCheckout(baseDir, args.getOrNull(1))
            else -> println(map.getOrDefault(first, "'$first' is not a SVCS command."))
        }
    }
}

fun handleCheckout(baseDir: File, second: String?) {
    if (second == null) {
        println("Commit id was not passed.")
        return
    }

    val commitsDir = baseDir.resolve("commits")
    val commits = commitsDir.list()!!
    if(!commitsDir.exists() || second !in commits) {
        println("Commit does not exist.")
        return
    }

    val foundCommitDir = commitsDir.resolve(second)
    val files = foundCommitDir.listFiles()!!
    val storagePath = "."
    val storageDir = File(storagePath)
    files.forEach {
        val newFile = storageDir.resolve(it.name)
        val content = it.readText()
        newFile.writeText(content)
    }

    println("Switched to commit $second.")
}

fun String.sha1(): String {
    val bytes = MessageDigest.getInstance("SHA-1").digest(this.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}

fun handleCommit(baseDir: File, second: String?) {
    if (second == null) {
        println("Message was not passed.")
        return
    }

    val indexFile = baseDir.resolve("index.txt")
    val configFile = baseDir.resolve("config.txt")
    if (!indexFile.exists() || indexFile.readText().isEmpty() || !configFile.exists() || configFile.readText()
            .isEmpty()
    ) {
        println("Nothing to commit.")
        return
    }

    val commitsDirPath = "vcs/commits"
    val commitsDir = File(commitsDirPath)
    if (!commitsDir.exists()) commitsDir.mkdir()

    val logFile = baseDir.resolve("log.txt")
    if (!logFile.exists()) logFile.createNewFile()
    val trackedFiles = indexFile.readLines()
    var combinedText = ""
    for (i in 1..trackedFiles.lastIndex) {
        val fileName = trackedFiles[i]
        val file = File(fileName)
        val fileContent = file.readText()
        combinedText += fileContent
    }

    val logCommits = logFile.readLines().filter { it.contains("commit") }.map { it.substringAfter(" ") }
    val hash = combinedText.sha1()
    if (hash in logCommits) {
        println("Nothing to commit.")
        return
    }

    val user = configFile.readText().split(" ").last().dropLast(1)
    val logText = logFile.readText()
    logFile.writeText("commit $hash\nAuthor: $user\n$second\n")
    if (logText.isNotEmpty()) logFile.appendText("\n$logText")
    val hashDir = File("$commitsDirPath/$hash")
    hashDir.mkdir()
    for (i in 1..trackedFiles.lastIndex) {
        val fileName = trackedFiles[i]
        val newFile = hashDir.resolve(fileName)
        newFile.createNewFile()
        val file = File(fileName)
        newFile.writeText(file.readText())
    }
    println("Changes are committed.")
}

fun handleLog(baseDir: File) {
    val logFile = baseDir.resolve("log.txt")
    if (!logFile.exists() || logFile.readText().isEmpty()) {
        println("No commits yet.")
        return
    }
    println(logFile.readText())
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