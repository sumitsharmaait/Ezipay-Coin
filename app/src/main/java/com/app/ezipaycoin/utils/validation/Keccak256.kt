package com.app.ezipaycoin.utils.validation

/**
 * Keccak-256 (the Ethereum variant, padding byte 0x01) in plain Kotlin so address checksums
 * can be verified in JVM unit tests without the native wallet-core library.
 */
object Keccak256 {
    private const val RATE = 136

    private val roundConstants = longArrayOf(
        0x0000000000000001uL.toLong(), 0x0000000000008082uL.toLong(), 0x800000000000808AuL.toLong(),
        0x8000000080008000uL.toLong(), 0x000000000000808BuL.toLong(), 0x0000000080000001uL.toLong(),
        0x8000000080008081uL.toLong(), 0x8000000000008009uL.toLong(), 0x000000000000008AuL.toLong(),
        0x0000000000000088uL.toLong(), 0x0000000080008009uL.toLong(), 0x000000008000000AuL.toLong(),
        0x000000008000808BuL.toLong(), 0x800000000000008BuL.toLong(), 0x8000000000008089uL.toLong(),
        0x8000000000008003uL.toLong(), 0x8000000000008002uL.toLong(), 0x8000000000000080uL.toLong(),
        0x000000000000800AuL.toLong(), 0x800000008000000AuL.toLong(), 0x8000000080008081uL.toLong(),
        0x8000000000008080uL.toLong(), 0x0000000080000001uL.toLong(), 0x8000000080008008uL.toLong()
    )
    private val rotations = intArrayOf(
        1, 3, 6, 10, 15, 21, 28, 36, 45, 55, 2, 14, 27, 41, 56, 8, 25, 43, 62, 18, 39, 61, 20, 44
    )
    private val lanes = intArrayOf(
        10, 7, 11, 17, 18, 3, 5, 16, 8, 21, 24, 4, 15, 23, 19, 13, 12, 2, 20, 14, 22, 9, 6, 1
    )

    fun hash(input: ByteArray): ByteArray {
        val state = LongArray(25)
        val padded = input.copyOf(((input.size / RATE) + 1) * RATE)
        padded[input.size] = (padded[input.size].toInt() xor 0x01).toByte()
        padded[padded.size - 1] = (padded[padded.size - 1].toInt() xor 0x80).toByte()

        var offset = 0
        while (offset < padded.size) {
            for (i in 0 until RATE / 8) {
                var lane = 0L
                for (b in 7 downTo 0) lane = (lane shl 8) or (padded[offset + i * 8 + b].toLong() and 0xFF)
                state[i] = state[i] xor lane
            }
            permute(state)
            offset += RATE
        }

        val out = ByteArray(32)
        for (i in out.indices) out[i] = (state[i / 8] ushr (8 * (i % 8))).toByte()
        return out
    }

    private fun permute(st: LongArray) {
        val bc = LongArray(5)
        for (round in 0 until 24) {
            for (i in 0 until 5) bc[i] = st[i] xor st[i + 5] xor st[i + 10] xor st[i + 15] xor st[i + 20]
            for (i in 0 until 5) {
                val t = bc[(i + 4) % 5] xor java.lang.Long.rotateLeft(bc[(i + 1) % 5], 1)
                for (j in 0 until 25 step 5) st[j + i] = st[j + i] xor t
            }
            var t = st[1]
            for (i in 0 until 24) {
                val j = lanes[i]
                val next = st[j]
                st[j] = java.lang.Long.rotateLeft(t, rotations[i])
                t = next
            }
            for (j in 0 until 25 step 5) {
                for (i in 0 until 5) bc[i] = st[j + i]
                for (i in 0 until 5) st[j + i] = st[j + i] xor (bc[(i + 1) % 5].inv() and bc[(i + 2) % 5])
            }
            st[0] = st[0] xor roundConstants[round]
        }
    }
}
