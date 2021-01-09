package thn.crypto.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteUtils {
    private static final Logger log = LoggerFactory.getLogger(ByteUtils.class);

    /**
     * Swap the high byte order with the low byte order
     *
     * @param data The integer value to be processed
     * @return the resulted integer value
     */
    public static int swap(final int data) {
        return ((data & 0x0F) << 4 | (data & 0xF0) >> 4);
    }

    /**
     * Swap the high byte order with the low byte order
     *
     * @param data The byte value to be processed
     * @return the resulted byte value
     */
    public static int swap(final byte data) {
        return swap(Byte.toUnsignedInt(data));
    }

    public static byte[] swap(final byte[] input, final int numberOfBytes) {
        byte[] output = new byte[numberOfBytes];

        for (int index = 0; index < numberOfBytes; index++) {
            output[index] = (byte) swap(input[index]);
        }

        return output;
    }

    public static byte[] forward(final byte[] input, final int numberOfBytes, final int position) {
        byte[] output = new byte[numberOfBytes];

        for (int index = 0; index < numberOfBytes; index++) {
            output[index] = (byte) forward(input[index], position);
        }

        return output;
    }

    public static byte[] backward(final byte[] input, final int numberOfBytes, final int position) {
        byte[] output = new byte[numberOfBytes];

        for (int index = 0; index < numberOfBytes; index++) {
            output[index] = (byte) backward(input[index], position);
        }
        return output;
    }

    public static int forward(final byte data, final int position) {
        return rotate(byteToInt(data), position);
    }

    public static int backward(final byte data, final int position) {
        return rotate(byteToInt(data), (-1 * position));
    }

    public static int forward(final int data, final int position) {
        return rotate(data, position);
    }

    public static int backward(final int data, final int position) {
        return rotate(data, (-1 * position));
    }

    public static int rotate(final byte data, final int position) {
        return rotate(byteToInt(data), position);
    }

    public static int rotate(final int data, final int position) {
        return ((data + position) % 256);
    }

    // equivalent to Byte.toUnsignedInt(...) in Java SE 8
    public static int byteToInt(final byte data) {
        return (data & 0xFF);
    }

    public static byte[] encode(final byte[] input, final int start, final int end, final int[] keys) {
        final int dataSize = end - start;
        byte[] output = new byte[dataSize];
        int position = 0;

        for (int index = start; index < dataSize; index++) {
            output[index] = (byte) swap(forward(swap(input[index]), keys[position++]));

            if (position > 0 && (position % keys.length == 0)) {
                position = 0;
            }
        }
        return output;
    }

    public static byte[] decode(final byte[] input, final int start, final int end, final int[] keys) {
        final int dataSize = end - start;
        byte[] output = new byte[dataSize];
        int position = 0;

        for (int index = 0; index < dataSize; index++) {
            output[index] = (byte) swap(backward(swap(input[index]), keys[position++]));

            if (position > 0 && (position % keys.length == 0)) {
                position = 0;
            }
        }
        return output;
    }

    public static File getFileFromResource(final String fileName) throws IOException {
        final InputStream is = ByteUtils.class.getClassLoader().getResourceAsStream(fileName);
        final File imageFile = new File("target/dummy.bin");
        FileUtils.copyInputStreamToFile(is, imageFile);
        return imageFile;
    }

    public static File getFileFromPath(final String fileName) {
        return Paths.get(fileName).toFile();
    }

    public static byte[] toByteArrayFromResource(final String fileName) throws IOException {
        return FileUtils.readFileToByteArray(getFileFromResource(fileName));
    }


    public static byte[] getByteArray(final File input, final int size) throws IOException {
        return getByteArray(input, 0, size);
    }

    /**
     * Note: ONLY work with file size that is up to (Integer.MAX - 1)
     *
     * @param input
     * @param start
     * @param end
     * @return the file content as a byte array
     * @throws IOException
     *             thrown when unable to process the input file
     */
    public static byte[] getByteArray(final File input, final int start, final int end) throws IOException {
        byte[] data = FileUtils.readFileToByteArray(input);
        if ((end - start) > 0) {
            data = ArrayUtils.subarray(data, start, end);
        }
        return data;
    }

    /**
     * http://www.javacodegeeks.com/2010/11/java-best-practices-char-to-byte-and
     * .html
     *
     * @param value
     *            The string value whose character is converted to a hex ASCII
     *            representation
     * @return a byte array in ASCII
     */
    public static byte[] stringToBytesASCII(String value) {
        byte[] ascii = new byte[value.length()];
        for (int index = 0; index < ascii.length; index++) {
            ascii[index] = (byte) value.charAt(index);
        }
        return ascii;
    }

    public static void display(final byte[] data, final int blockSize, final long numberOfBytes) {
        for (int index = 0; index < data.length; index++) {
            if (index > 0 && (index % numberOfBytes == 0)) {
                break;
            }

            if (index > 0 && index % 4 == 0) {
                System.out.print(" ");
            }

            if (index % blockSize == 0) {
                System.out.print(String.format("\n%06X:%06X -", index, (index + blockSize - 1)));
            }

            System.out.print(String.format(" %02X", data[index]));
        }
        System.out.println("\n");
    }

    public static int[] getByteInfo(final String info) {
        final String[] extracted = info.split(":");
        if (extracted.length > 0) {
            int[] arrayInfo = new int[extracted.length];
            for (int index = 0; index < extracted.length; index++) {
                final int extractedInt = Integer.parseInt(extracted[index], 16);
                arrayInfo[index] = extractedInt;
            }
            return arrayInfo;
        }
        return null;
    }

    public static int find(final byte[] data, final byte first, final byte second, final int start) {
        System.out.println(String.format("s[%d]: %02X - %X", start, first, second));

        int index = ArrayUtils.indexOf(data, first, start);
        if (index > -1) {
            if (Byte.toUnsignedInt(data[index+1]) == Byte.toUnsignedInt(second)) {
                return index;
            } else {
                find(data, first, second, index+1);
            }
        }
        return -1;
    }
}