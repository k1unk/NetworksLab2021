fun main() {
    println("Добро пожаловать")
    val (token, isAdmin) = auth()
    printHelp(isAdmin)
    while (true) {
        val command = readLine()
        when (command) {
            "SHOW" -> {
                getBets(token)
            }
            "BET" -> {
                if (!isAdmin) makeBet(token)
                else println("Вы должны быть игроком, чтобы сделать ставку")
            }
            "START" -> {
                if (isAdmin) startGame(token)
                else println("Вы должны быть крупье, чтобы начать игру")
            }
            "RESULT" -> {
                getResults(token)
            }
            "HELP" -> {
                printHelp(isAdmin)
            }
        }
    }
}

fun printHelp(isAdmin: Boolean) {
    println("Чтобы посмотреть список ставок - введите SHOW")
    if (!isAdmin) println("Чтобы сделать ставку - введите BET")
    if (isAdmin) println("Чтобы начать игру - введите START")
    println("Чтобы посмотреть победителя - введите RESULT")
    println("Помощь - HELP")
}