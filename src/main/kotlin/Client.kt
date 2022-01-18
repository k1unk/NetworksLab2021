import java.io.*
import java.net.*
import java.nio.file.Files

fun main(args: Array<String>) {
    val c = Client()
    c.start()
}

class Client {
    private var serverPort = 69

    fun start() {
        println("format: [r | w] pathname")
        while (true) {
            val input = readLine()?.trim()
            val splittedInput = input?.split("\\s+".toRegex())

            if (splittedInput!![0] == "exit") {
                break
            }

            if (splittedInput.size == 2) {
                when (splittedInput[0]) {
                    "r" -> {
                        readMode(splittedInput[1])
                        break
                    }
                    "w" -> {
                        writeMode(splittedInput[1])
                        break
                    }
                }
            }

            println("format: [r | w] pathname")
        }
    }

    private fun readMode(filePath: String) {
        val client = DatagramSocket(70)
        val address = InetAddress.getLocalHost()
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val fileName = File(filePath).name
        val request = Utils.packRequest(Utils.Opcode.RRQ, fileName)
        Utils.send(client, request, address, serverPort)

        var inputPacket: DatagramPacket
        try {
            inputPacket = Utils.receive(client)
            serverPort = inputPacket.port
        } catch (e: SocketTimeoutException) {
            val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
            Utils.send(client, error, address, serverPort)
            println("Error: Timeout expired. Downloading canceled.")
            return
        }

        val fileContent = ByteArrayOutputStream()
        var currentBlock: Short = 1
        while (true) {
            val res = Utils.receiveData(inputPacket, currentBlock, fileContent, address, serverPort, client)
            when (res.second) {
                0 -> currentBlock = res.first
                1 -> return
                2 -> break
            }
            try {
                inputPacket = Utils.receive(client)
            } catch (e: SocketTimeoutException) {
                val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
                Utils.send(client, error, address, serverPort)
                println("Error: Timeout expired. Downloading canceled.")
                return
            }
        }
        client.close()
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun writeMode(filePath: String) {
        val client = DatagramSocket(70)
        val address = InetAddress.getLocalHost()
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val file = File(filePath)
        if (!file.exists()) {
            println("File $filePath not found.")
            return
        }
        val fileBytes = Files.readAllBytes(file.toPath())

        val request = Utils.packRequest(Utils.Opcode.WRQ, filePath.split('/').last())
        Utils.send(client, request, address, serverPort)

        var inputPacket: DatagramPacket
        try {
            inputPacket = Utils.receive(client)
            serverPort = inputPacket.port
        } catch (e: SocketTimeoutException) {
            val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
            Utils.send(client, error, address, serverPort)
            println("Error: Timeout expired. Sending canceled.")
            return
        }
        var currentBlock: Short = 0
        var notEnd = true

        while (notEnd) {
            when (inputPacket.data[1].toShort()) {
                Utils.Opcode.ACK.code -> {
                    currentBlock = Utils.unpackACK(inputPacket.data)
                    currentBlock++
                }
                Utils.Opcode.Error.code -> {
                    val error = Utils.unpackError(inputPacket.data)
                    println("Error ${error.first}: ${error.second}")
                    return
                }
                else -> {
                    val error = Utils.packError(Utils.Error.IllegalTFTPOperation, "Expected ACK packet.")
                    Utils.send(client, error, address, serverPort)
                }
            }
            notEnd = Utils.sendData(currentBlock, fileBytes, client, address, serverPort)

            try {
                inputPacket = Utils.receive(client)
            } catch (e: SocketTimeoutException) {
                val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
                Utils.send(client, error, address, serverPort)
                println("Error: Timeout expired. Sending canceled.")
                return
            }
        }
        println("File $filePath sent successfully.")
        client.close()
    }
}