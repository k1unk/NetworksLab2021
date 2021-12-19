import java.io.*
import java.lang.Exception
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
            val input = readLine()?.trim()?.replace("\\s+".toRegex(), " ")

            val splittedInput = input?.split(" ")
            if (splittedInput?.get(0) == "") {
                continue
            }

            if (splittedInput?.size == 1) {
                when ((splittedInput[0])) {
                    "exit" -> {
                        return
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }
            }
            if (splittedInput?.size == 2) {
                val inp0 = splittedInput[0]
                val inp1 = splittedInput[1]
                when (inp0) {
                    "r" -> {
                        readFile(inp1)
                        return
                    }
                    "w" -> {
                        writeFile(inp1)
                        return
                    }
                    else -> {
                        println("format: [r | w] pathname")
                        continue
                    }
                }

            }
            if (splittedInput?.size!! > 2) {
                println("format: [r | w] pathname")
                continue
            }

        }
    }

    private fun readFile(filePath: String) {
        val client = DatagramSocket(70)
        val address = InetAddress.getLocalHost()
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val request = Utils.packRequest(Utils.Opcode.RRQ, filePath.split('/').last())
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
            when (inputPacket.data[1].toShort()) {
                Utils.Opcode.Error.code -> {
                    val error = Utils.unpackError(inputPacket.data)
                    println("Error ${error.first}: ${error.second}")
                    return
                }
                Utils.Opcode.Data.code -> {
                    val packData = Utils.unpackData(inputPacket.data, inputPacket.length)
                    if (currentBlock == packData.first) {
                        fileContent.write(packData.second)
                        Utils.send(client, Utils.packACK(currentBlock), address, serverPort)
                        if (inputPacket.length != Utils.PACKET_SIZE) {
                            break
                        }
                        currentBlock++
                    } else
                        Utils.send(client, Utils.packACK(currentBlock), address, serverPort)
                }
                else -> {
                    val error = Utils.packError(Utils.Error.IllegalTFTPOperation, "Expected DATA packet.")
                    Utils.send(client, error, address, serverPort)
                }
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
        val fileName = filePath.split('/').last()
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun writeFile(filePath: String) {
        val client = DatagramSocket(70)
        val address = InetAddress.getLocalHost()
        client.soTimeout = Utils.SOCKET_TIMEOUT

        val file: File
        val fileBytes: ByteArray
        try {
            file = File(filePath)
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            println("File $filePath not found.")
            return
        }

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
            var output: ByteArray
            if (Utils.DATA_SIZE * currentBlock > fileBytes.size) {
                output = Utils.packDataBlock(
                    currentBlock, fileBytes.copyOfRange(
                        Utils.DATA_SIZE * (currentBlock - 1), fileBytes.size
                    )
                )
                notEnd = false
            } else {
                output = Utils.packDataBlock(
                    currentBlock, fileBytes.copyOfRange(
                        Utils.DATA_SIZE * (currentBlock - 1), Utils.DATA_SIZE * currentBlock
                    )
                )
            }
            Utils.send(client, output, address, serverPort)
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