package hashing;

public final class HashFunctionV01 implements HashFunction {

    private static final int[] INITIAL_STATE = {
            0x13579BDF, 0x2468ACE0, 0xFDB97531, 0xECA86420,
            0x0F1E2D3C, 0x4B5A6978, 0x89ABCDEF, 0x10203040
    };

    @Override
    public String hash(byte[] input) {
        int[] state = INITIAL_STATE.clone();

        for (int i = 0; i < input.length; i++) {
            int unsignedByte = input[i] & 0xFF;
            int currentIndex = i % state.length;
            int nextIndex = (currentIndex + 1) % state.length;

            state[currentIndex] ^= unsignedByte + i;

            int rotation = (unsignedByte % 13) + 5;
            state[currentIndex] =
                    Integer.rotateLeft(state[currentIndex], rotation);

            state[currentIndex] += state[nextIndex];
        }

        throw new UnsupportedOperationException("not implemented yet");
    }