package hashing;

public final class HashFunctionV02 implements HashFunction {

    private static final int[] INITIAL_STATE = { 0x13579BDF, 0x2468ACE0, 0xFDB97531, 0xECA86420, 0x0F1E2D3C, 0x4B5A6978,
            0x89ABCDEF, 0x10203040 };

    // Odd multipliers preserve information modulo 2^32; these reuse the original state constants.
    private static final int BYTE_MULTIPLIER = 0x13579BDF;
    private static final int FINAL_MULTIPLIER = 0xFDB97531;
    private static final int FINAL_ROUNDS = 5;
    private static final String HEX_DIGITS = "0123456789abcdef";

    @Override
    public String hash(byte[] input) {
        int[] state = INITIAL_STATE.clone();

        for (int i = 0; i < input.length; i++) {
            int unsignedByte = input[i] & 0xFF;
            int currentIndex = i % state.length;
            int nextIndex = (currentIndex + 1) % state.length;

            state[currentIndex] ^= unsignedByte + i;

            int rotation = (unsignedByte % 13) + 5;
            state[currentIndex] = Integer.rotateLeft(state[currentIndex], rotation);

            state[currentIndex] += state[nextIndex];

            // Fold high bits down before multiplication, then spread each byte beyond its own element.
            state[currentIndex] ^= state[currentIndex] >>> 16;
            state[currentIndex] *= BYTE_MULTIPLIER;
            state[nextIndex] ^= Integer.rotateLeft(state[currentIndex], 11);
            int oppositeIndex = (currentIndex + state.length / 2) % state.length;
            state[oppositeIndex] += Integer.rotateLeft(state[currentIndex], 19);
        }

        state[0] ^= input.length;
        state[state.length - 1] += Integer.rotateLeft(input.length, 16);

        for (int j = 0; j < state.length; j++) {
            state[j] ^= Integer.rotateLeft(input.length * BYTE_MULTIPLIER, j * 3 + 1);
        }

        for (int i = 0; i < FINAL_ROUNDS; i++) {
            for (int j = 0; j < state.length; j++) {
                int nextIndex = (j + 1) % state.length;

                state[j] ^= state[nextIndex];
                state[j] = Integer.rotateLeft(state[j], 7);
                state[nextIndex] += state[j];

                // Distinguish rounds and positions, and carry differences across the state ring.
                state[j] += INITIAL_STATE[(j + i) % state.length] ^ (i + 1);
                state[j] ^= state[j] >>> 15;
                state[j] *= FINAL_MULTIPLIER;
                state[j] ^= state[j] >>> 13;
                int oppositeIndex = (j + state.length / 2) % state.length;
                state[oppositeIndex] ^= Integer.rotateLeft(state[j], 13 + i * 2);
            }
        }

        return toHex(state);
    }

    private static String toHex(int[] state) {
        char[] result = new char[state.length * 8];
        int position = 0;

        for (int value : state) {
            for (int shift = 28; shift >= 0; shift -= 4) {
                result[position++] = HEX_DIGITS.charAt((value >>> shift) & 0xF);
            }
        }

        return new String(result);
    }
}
