import java.io.*
import java.net.*
import java.nio.file.Files


fun main(args: Array<String>) {
    val s = Server();
    s.start()
}

class Server {
    private lateinit var server: DatagramSocket

    fun start() {
        server = DatagramSocket(69)
        println("Server started")
        while (true) {
            server.soTimeout = Utils.SERVER_ACCEPT_TIMEOUT
            val inputPacket: DatagramPacket
            try {
                inputPacket = Utils.receive(server)
            } catch (e: SocketTimeoutException) {
                println("No connections to server.")
                break
            }
            val clientAddress = inputPacket.address
            val clientPort = inputPacket.port
            val request = Utils.unpackRequest(inputPacket.data)
            server.soTimeout = Utils.SOCKET_TIMEOUT
            if (request.first == Utils.Opcode.RRQ) {
                println("Server received RRQ")
                readMode(request, clientAddress, clientPort)
            } else if (request.first == Utils.Opcode.WRQ) {
                println("Server received WRQ")
                writeMode(request, clientAddress, clientPort)
            }
        }
        server.close()
        println("Server closed")
    }

    private fun writeMode(request: Pair<Utils.Opcode, List<String>>, address: InetAddress, port: Int) {
        Utils.send(server, Utils.packACK(0), address, port)
        val fileContent = ByteArrayOutputStream()
        var currentBlock: Short = 1
        while (true) {
            val inputPacket: DatagramPacket
            try {
                inputPacket = Utils.receive(server)
            } catch (e: SocketTimeoutException) {
                val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
                Utils.send(server, error, address, port)
                return
            }
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
                        Utils.send(server, Utils.packACK(currentBlock), address, port)
                        if (inputPacket.length != Utils.PACKET_SIZE) {
                            break
                        }
                        currentBlock++
                    } else
                        Utils.send(server, Utils.packACK(currentBlock), address, port)
                }
                else -> {
                    val error = Utils.packError(Utils.Error.IllegalTFTPOperation, "Expected ACK packet.")
                    Utils.send(server, error, address, port)
                }
            }
        }
        val fileName = request.second[0]
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun readMode(request: Pair<Utils.Opcode, List<String>>, address: InetAddress, port: Int) {
        val file: File
        val fileBytes: ByteArray
        try {
            file = File(request.second.first())
            fileBytes = Files.readAllBytes(file.toPath())
        } catch (e: Exception) {
            val error = Utils.packError(Utils.Error.FileNotFound, "File ${request.second.first()} not founded.")
            Utils.send(server, error, address, port)
            println("RRQ: File not found.")
            return
        }
        var currentBlock: Short = 1
        var notEnd = true
        while (notEnd) {
            val output: ByteArray
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
            Utils.send(server, output, address, port)

            val inputPacket: DatagramPacket
            try {
                inputPacket = Utils.receive(server)
            } catch (e: SocketTimeoutException) {
                val error = Utils.packError(Utils.Error.NotDefined, "Timeout expired")
                Utils.send(server, error, address, port)
                return
            }
            when (inputPacket.data[1].toShort()) {
                Utils.Opcode.ACK.code -> {
                    currentBlock = Utils.unpackACK(inputPacket.data)
                    currentBlock++
                }
                Utils.Opcode.Error.code -> {
                    val error = Utils.unpackError(inputPacket.data)
                    println("RRQ: Error ${error.first}: ${error.second}")
                    return
                }
                else -> {
                    val error = Utils.packError(Utils.Error.IllegalTFTPOperation, "Expected ACK packet.")
                    Utils.send(server, error, address, port)
                }
            }
        }
        println("RRQ: Server send ${fileBytes.size} bytes in ${currentBlock - 1} packets.")
        return
    }
}