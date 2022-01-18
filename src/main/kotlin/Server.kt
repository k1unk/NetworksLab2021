import java.io.*
import java.net.*
import java.nio.file.Files


fun main(args: Array<String>) {
    val s = Server()
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
            val requestOpcode = Utils.unpackOpcode(inputPacket.data)
            val requestBytes = Utils.unpackBytes(inputPacket.data)
            server.soTimeout = Utils.SOCKET_TIMEOUT
            if (requestOpcode == Utils.Opcode.RRQ) {
                println("Server received RRQ")
                readMode(requestBytes, clientAddress, clientPort)
            } else if (requestOpcode == Utils.Opcode.WRQ) {
                println("Server received WRQ")
                writeMode(requestBytes, clientAddress, clientPort)
            }
        }
        server.close()
        println("Server closed")
    }

    private fun writeMode(requestBytes: List<String>, address: InetAddress, port: Int) {
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
            val res = Utils.receiveData(inputPacket, currentBlock, fileContent, address, port, server);
            if (res.second == 1) return
            if (res.second == 2) break
            currentBlock = res.first
        }
        val fileName = requestBytes[0]
        File(fileName).writeBytes(fileContent.toByteArray())
        println("File $fileName downloaded successfully.")
    }

    private fun readMode(requestBytes: List<String>, address: InetAddress, port: Int) {
        val file = File(requestBytes.first())
        if (!file.exists()) {
            val error = Utils.packError(Utils.Error.FileNotFound, "File ${requestBytes.first()} not founded.")
            Utils.send(server, error, address, port)
            println("RRQ: File not found.")
            return
        }
        val fileBytes: ByteArray = Files.readAllBytes(file.toPath())
        var currentBlock: Short = 1
        var notEnd = true
        while (notEnd) {
            notEnd = Utils.sendData(currentBlock, fileBytes, server, address, port)

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
                    if (currentBlock == Utils.unpackACK(inputPacket.data)) currentBlock++
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