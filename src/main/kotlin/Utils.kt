import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

object Utils {
    private const val OCTET_MODE = "octet"
    private const val DATA_SIZE = 512
    private const val PACKET_SIZE = DATA_SIZE + 4
    const val SOCKET_TIMEOUT = 4000
    const val SERVER_ACCEPT_TIMEOUT = 120000

    private val CHARSET = Charsets.UTF_8

    enum class Opcode(val code: Short) {
        RRQ(1),
        WRQ(2),
        Data(3),
        ACK(4),
        Error(5)
    }

    enum class Error(val code: Short) {
        NotDefined(0),
        FileNotFound(1),
        AccessViolation(2),
        DiskFullOrAllocationExceeded(3),
        IllegalTFTPOperation(4),
        UnknownTransferID(5),
        FileAlreadyExists(6),
        NoSuchUser(7);
    }

    fun getError(code: Int): Error = when (code) {
        0 -> Error.NotDefined
        1 -> Error.FileNotFound
        2 -> Error.AccessViolation
        3 -> Error.DiskFullOrAllocationExceeded
        4 -> Error.IllegalTFTPOperation
        5 -> Error.UnknownTransferID
        6 -> Error.FileAlreadyExists
        else -> Error.NoSuchUser
    }

    fun packRequest(opcode: Opcode, fileName: String, mode: String = OCTET_MODE): ByteArray =
        ByteBuffer.allocate(4 + fileName.length + mode.length)
            .putShort(opcode.code)
            .put(fileName.toByteArray())
            .put(0)
            .put(mode.toByteArray())
            .put(0)
            .array()

    fun packDataBlock(num: Short, content: ByteArray): ByteArray = ByteBuffer.allocate(4 + content.size)
        .putShort(Opcode.Data.code)
        .putShort(num)
        .put(content)
        .array()

    fun packACK(num: Short): ByteArray = ByteBuffer.allocate(4)
        .putShort(Opcode.ACK.code)
        .putShort(num)
        .array()

    fun packError(error: Error, string: String): ByteArray = ByteBuffer.allocate(5 + string.length)
        .putShort(Opcode.Error.code)
        .putShort(error.code)
        .put(string.toByteArray())
        .put(0)
        .array()

    fun unpackOpcode(bytes: ByteArray): Opcode =
        if (BigInteger(bytes.copyOfRange(0, 2)).toShort() == Opcode.RRQ.code) Opcode.RRQ
        else Opcode.WRQ

    fun unpackBytes(bytes: ByteArray): List<String> =
            bytes.copyOfRange(2, bytes.size).toString(CHARSET).split(0.toChar()).subList(0, 2)

    private fun unpackData(bytes: ByteArray, size: Int): Pair<Short, ByteArray> =
        Pair(BigInteger(bytes.copyOfRange(2, 4)).toShort(), bytes.copyOfRange(4, size))

    fun unpackACK(bytes: ByteArray): Short = BigInteger(bytes.copyOfRange(2, 4)).toShort()

    fun unpackError(bytes: ByteArray): Pair<Short, String> =
        Pair(BigInteger(bytes.copyOfRange(2, 4)).toShort(), bytes.copyOfRange(4, bytes.size).toString(CHARSET).split(0.toChar()).first())

    fun receive(socket: DatagramSocket): DatagramPacket {
        val input = ByteArray(PACKET_SIZE)
        val inputPacket = DatagramPacket(input, input.size)
        socket.receive(inputPacket)
        return inputPacket
    }

    fun send(socket: DatagramSocket, output: ByteArray, address: InetAddress, port: Int) {
        val outputPacket = DatagramPacket(output, output.size, address, port)
        socket.send(outputPacket)
    }

    fun receiveData(
        inputPacket: DatagramPacket,
        currentBlock: Short,
        fileContent: ByteArrayOutputStream,
        address: InetAddress,
        port: Int,
        server: DatagramSocket
    ): Pair<Short, Int> {
        var currentBlockLocal = currentBlock
        when (inputPacket.data[1].toShort()) {
            Opcode.Error.code -> {
                val error = unpackError(inputPacket.data)
                println("Error ${error.first}: ${error.second}")
                return Pair(0, 1)
            }
            Opcode.Data.code -> {
                val packData = unpackData(inputPacket.data, inputPacket.length)
                if (currentBlock == packData.first) {
                    fileContent.write(packData.second)
                    send(server, packACK(currentBlock), address, port)
                    if (inputPacket.length != PACKET_SIZE) {
                        return Pair(0, 2)
                    }
                    currentBlockLocal++
                } else
                    send(server, packACK(currentBlock), address, port)
            }
            else -> {
                val error = packError(Error.IllegalTFTPOperation, "Expected ACK packet.")
                send(server, error, address, port)
            }
        }
        return Pair(currentBlockLocal, 0)
    }

    fun sendData(
        currentBlock: Short,
        fileBytes: ByteArray,
        server: DatagramSocket,
        address: InetAddress,
        port: Int
    ): Boolean {
        var result = true
        val output: ByteArray
        if (DATA_SIZE * currentBlock > fileBytes.size) {
            output = packDataBlock(
                currentBlock, fileBytes.copyOfRange(
                    DATA_SIZE * (currentBlock - 1), fileBytes.size
                )
            )
            result = false
        } else {
            output = packDataBlock(
                currentBlock, fileBytes.copyOfRange(
                    DATA_SIZE * (currentBlock - 1), DATA_SIZE * currentBlock
                )
            )
        }
        send(server, output, address, port)
        return result
    }
}