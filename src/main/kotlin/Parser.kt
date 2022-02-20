fun parser(result: String, i: Int, char: Char): String {
    var value = ""
    var j: Int
    if (result[i] == char && result[i + 1] == '"' && result[i + 2] == ':') {
        j = i + 3
        while (result[j] != ',' && result[j] != '}') {
            value += result[j]
            j++
        }
    }
    return value
}

fun parserWithoutQuotes(result: String, i: Int, char: Char): String {
    var value = ""
    var j: Int
    if (result[i] == char && result[i + 1] == ':') {
        j = i + 3
        while (result[j] != ',' && result[j] != '}') {
            value += result[j]
            j++
        }
    }
    return value
}