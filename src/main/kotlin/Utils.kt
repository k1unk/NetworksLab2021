import java.math.BigInteger
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

object Utils {
    const val OCTET_MODE = "octet"
    const val DATA_SIZE = 512
    const val PACKET_SIZE = DATA_SIZE + 4
    const val SOCKET_TIMEOUT = 4000
    const val SERVER_ACCEPT_TIMEOUT = 120000

    val CHARSET = Charsets.UTF_8

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

    fun unpackRequest(bytes: ByteArray): Pair<Opcode, List<String>> =
        if (BigInteger(bytes.copyOfRange(0, 2)).toShort() == Opcode.RRQ.code)
            Pair(Opcode.RRQ, bytes.copyOfRange(2, bytes.size).toString(CHARSET).split(0.toChar()).subList(0, 2))
        else
            Pair(Opcode.WRQ, bytes.copyOfRange(2, bytes.size).toString(CHARSET).split(0.toChar()).subList(0, 2))

    fun unpackData(bytes: ByteArray, size: Int): Pair<Short, ByteArray> =
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
}