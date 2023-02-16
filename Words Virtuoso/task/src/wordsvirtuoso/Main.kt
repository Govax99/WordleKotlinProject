package wordsvirtuoso

import java.io.BufferedReader
import java.io.File
import java.util.Dictionary
import java.util.SortedSet
import kotlin.system.exitProcess
import kotlin.random.Random

fun checkWord(string: String, isSilent: Boolean = true): Boolean {
    //val regex = """\s*\w+\s*""".toRegex()
    //var word = regex.find(string)?.value ?: return false
    val word = string.trim()

    if (word.length != 5) {
        if (!isSilent) println("The input isn't a 5-letter word.")
        return false
    }

    for (l in word) {
        if (l !in 'a'..'z' && l !in 'A'..'Z') {
            if (!isSilent) println("One or more letters of the input aren't valid.")
            return false
        }
    }

    for (l in word.groupBy { it }.values) {
        if (l.size != 1) {
            if (!isSilent) println("The input has duplicate letters.")
            return false
        }
    }

    //println("The input is a valid string.")
    return true
}

fun checkConsistency(pathDictionary:  String, pathCandidates: String) {
    var included = 0
    var total = 0
    val dictionary = File(pathDictionary)
    val candidates = File(pathCandidates)


    candidates.bufferedReader().use {
        it.forEachLine { s ->
            total++
            dictionary.bufferedReader().use { d ->
                d.forEachLine { w ->
                    if (s.lowercase() == w.lowercase()) included++
                }
            }
        }
    }

    if (total != included) {
        println("Error: ${total - included} candidate words" +
                " are not included in the ${dictionary.name} file.")
        exitProcess(1)
    }

}

fun checkWordFile(path: String, isCandidate: Boolean = false) {
    var irregularCount = 0
    val file = File(path)
    val modifier = if (isCandidate) "candidate " else ""
    if (!file.exists()) {
        println("Error: The ${modifier}words file ${file.name} doesn't exist.")
        exitProcess(1)
    }
    var result = true
    file.bufferedReader().use {
        it.forEachLine { s ->
            val isCorrect = checkWord(s)
            if (!isCorrect) irregularCount++
            result = result && isCorrect
        }
    }
    if (result) {
        //println("All words are valid!")
    } else {
        println("Error: $irregularCount invalid words were found in the ${file.name} file.")
        exitProcess(1)
    }
}

fun linesInFile(path: String): Int {
    val file = File(path)
    var wordCount = 0
    file.bufferedReader().use {
        it.forEachLine { s ->
            wordCount++
        }
    }
    return wordCount
}

fun isWordInFile(path: String, word: String): Boolean {
    var found = false
    var wordCount = 0
    File(path).bufferedReader().use {
        it.forEachLine { s ->
            if (s == word) {
                found = true
                return@forEachLine
            }
        }
    }
    return found
}



class WordsVirtuoso() {
    var secretWord = ""
    var numCandidates = 0
    var guessList = mutableListOf<String>()
    var wrongChar = mutableSetOf<String>()

    var words = "."
        set(value) {
            checkWordFile(value)
            field = value
            if (candidates != ".") {
                checkConsistency(words, candidates)
            }

        }
    var candidates = "."
        set(value) {
            checkWordFile(value,true)
            field = value
            if (words != ".") {
                checkConsistency(words, candidates)
            }
            numCandidates = linesInFile(value)

        }

    fun chooseRandom(): String {
        /*val randomInt = Random.nextInt(0, numCandidates)
        val file = File(candidates)
        var word = ""*/
        var word = File(candidates).readLines().random()
        /*var wordCount = 0
        file.bufferedReader().use {
            it.forEachLine { s ->
                if (wordCount == randomInt) {
                    word = s
                    return@forEachLine
                }
                wordCount++
            }
        }*/
        return word.lowercase()
    }

    fun clueString(word: String): String {
        fun green(str: String): String = "\u001B[48:5:10m$str\u001B[0m"
        fun yellow(str: String): String = "\u001B[48:5:11m$str\u001B[0m"
        fun grey(str: String): String = "\u001B[48:5:7m$str\u001B[0m"
        fun azure(str: String): String = "\u001B[48:5:14m$str\u001B[0m"

        val clue = MutableList<String>(5) { "" }
        val secretSet = secretWord.toSet()
        for ((i, l) in word.withIndex()) {
            val L = l.uppercase()
            if (l == secretWord[i]) {
                clue[i] = green(L)
            } else if (l in secretSet) {
                clue[i] = yellow(L)
            } else {
                clue[i] = grey(L)
                wrongChar.add(azure(L))
            }
        }
        guessList.add(clue.joinToString(""))
        return clue.joinToString("")
    }

    fun mainOperation(args: Array<String>) {
        val generator = Random(System.currentTimeMillis())
        if (args.size != 2) {
            println("Error: Wrong number of arguments.")
            exitProcess(1)
        }


        words = args[0]
        candidates = args[1]
        println("Words Virtuoso")
        secretWord = chooseRandom()


        while (true) {
            println("Input a 5-letter word:")
            print("\u001B[38:5:10m")
            val start = System.currentTimeMillis()
            val input = readln().lowercase()
            print("\u001B[0m")
            if (input == "exit") {
                println("The game is over.")
                exitProcess(1)
            }

            val isValid = checkWord(input, false)
            if (!isValid) {
                continue
            }

            if (!isWordInFile(words, input)) {
                println("The input word isn't included in my words list.")
                continue
            }

            // print all the clue list in chronological order
            val clue = clueString(input)
            guessList.forEach {
                println(it)
            }

            // Check if won
            if (input == secretWord) {
                val end = System.currentTimeMillis()
                val duration = end - start  // Milliseconds as a Long
                println("Correct!")
                if (guessList.size == 1) {
                    println("Amazing luck! The solution was found at once.")
                    exitProcess(0)
                }
                println("The solution was found after ${guessList.size} tries in ${duration / 1000} seconds.")
                exitProcess(0)
            }

            println(wrongChar.sorted().joinToString(""))

        }
    }


}



fun main(args: Array<String>) {
    val game = WordsVirtuoso()
    game.mainOperation(args)


}
