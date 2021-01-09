package thn.crypto.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteUtilsIT {
    private static final Logger log = LoggerFactory.getLogger(ByteUtilsIT.class);

    private final int[] KEYS = { 1, 2, 27 };
    private final int BLOCK_SIZE = 16;
    private final int CHUNK = 64;

    @Test
    public void test() throws IOException {
        final String JPG_FILE = "data/image/red-256.jpg";
        final byte[] inputData = ByteUtils.toByteArrayFromResource(JPG_FILE);

        ByteUtils.display(inputData, BLOCK_SIZE, CHUNK);

        log.info("");

        byte[] encodedData = ByteUtils.encode(inputData, 0, CHUNK, KEYS);
        ByteUtils.display(encodedData, BLOCK_SIZE, CHUNK);

        log.info("");

        byte[] decodedData = ByteUtils.decode(encodedData, 0, CHUNK, KEYS);
        ByteUtils.display(decodedData, BLOCK_SIZE, CHUNK);

        final String OUTPUT_JPG_FILE = "target/data/image/red-256_o.jpg";
        final File outputFile = Paths.get(OUTPUT_JPG_FILE).toFile();
        FileUtils.writeByteArrayToFile(outputFile, decodedData);
    }

    @Test
    public void testFind() throws IOException {
        final String JPG_FILE = "data/image/red-256.jpg";
        final byte[] inputData = ByteUtils.toByteArrayFromResource(JPG_FILE);

        final int soi = ByteUtils.find(inputData, (byte)0x6f, (byte)0x74, 0);
        final int eoi = ByteUtils.find(inputData, (byte)0xff, (byte)0xd9, 0);
        log.info(String.format("[%d, %d]", soi, eoi));
    }

    @Test
    public void testEncode() throws IOException {
        final int[] keys = { 1, 5, 200 };

        final String JPG_FILE = "data/image/red-256.jpg";
        final File imageFile = ByteUtils.getFileFromResource(JPG_FILE);

        final byte[] inputData = ByteUtils.getByteArray(imageFile, -1);
        ByteUtils.display(inputData, BLOCK_SIZE, 32);

        log.info("");

        final byte[] encoded = ByteUtils.encode(inputData, 0, inputData.length, keys);
        ByteUtils.display(encoded, BLOCK_SIZE, 32);
    }

    @Test
    public void testDecode() throws IOException {
        final int[] keys = { 1, 5, 200 };

        final String JPG_FILE = "data/image/red-256.jpg";
        final File imageFile = ByteUtils.getFileFromResource(JPG_FILE);

        final byte[] inputData = ByteUtils.getByteArray(imageFile, -1);
        ByteUtils.display(inputData, BLOCK_SIZE, 32);

        log.info("");

        final byte[] encoded = ByteUtils.encode(inputData, 0, inputData.length, keys);
        ByteUtils.display(encoded, BLOCK_SIZE, 32);

        log.info("");

        final byte[] decoded = ByteUtils.decode(encoded, 0, inputData.length, keys);
        ByteUtils.display(decoded, BLOCK_SIZE, 32);
    }

    public void testSwap() {
        byte data = 0x01;
        log.info(String.format("%d: %02X -> %02X", data, data, ByteUtils.swap(data)));

        data = (byte) 0xFF;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data, ByteUtils.swap(data)));

        data = (byte) 0xF0;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data, ByteUtils.swap(data)));

        int iData = 1;
        log.info(String.format("%d: %02X -> %02X", iData, iData, ByteUtils.swap(iData)));

        iData = 255;
        log.info(String.format("%d: %02X -> %02X", iData, iData, ByteUtils.swap(iData)));
    }

    public void testBackward() {
        int position = 1;
        int iData = 1;
        byte data = 0x01;

        iData = 0;
        log.info(String.format("[%d - %d]: %02X -> %02X", iData, position, iData,
                (byte) ByteUtils.backward(iData, position)));

        iData = 1;
        log.info(String.format("[%d - %d]: %02X -> %02X", iData, position, iData,
                (byte) ByteUtils.backward(iData, position)));

        iData = 255;
        log.info(String.format("[%d - %d]: %02X -> %02X", iData, position, iData,
                (byte) ByteUtils.backward(iData, position)));

        iData = 3;
        position = 4;
        log.info(String.format("[%d - %d]: %02X -> %02X", iData, position, iData,
                (byte) ByteUtils.backward(iData, position)));

        iData = 5;
        position = 15;
        log.info(String.format("[%d - %d]: %02X -> %02X", iData, position, iData,
                (byte) ByteUtils.backward(iData, position)));

        position = 1;

        data = (byte) 0xFF;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data,
                (byte) ByteUtils.backward(data, position)));

        data = (byte) 0x00;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data,
                (byte) ByteUtils.backward(data, position)));

        data = (byte) 0x00;
        position = 4;
        log.info(String.format("[%d - %d]: %02X -> %02X", ByteUtils.byteToInt(data), position, data, (byte) ByteUtils.backward(data, position)));
    }

    public void testRotate() {
        int position = 1;
        int iData = 1;

        byte data = 0x01;
        log.info(String.format("%d: %02X -> %02X", data, data, ByteUtils.rotate(data, position)));

        data = (byte) 0xFF;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data, ByteUtils.rotate(data, position)));

        data = (byte) 0xF0;
        log.info(
                String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data, ByteUtils.rotate(data, position)));

        data = (byte) 0x0F;
        log.info(String.format("%d: %02X -> %02X", ByteUtils.byteToInt(data), data, ByteUtils.rotate(data, position)));

        iData = 1;
        log.info(String.format("%d: %02X -> %02X", iData, iData, ByteUtils.rotate(iData, position)));

        iData = 15;
        log.info(String.format("%d: %02X -> %02X", iData, iData, ByteUtils.rotate(iData, position)));

        iData = 255;
        log.info(String.format("%d: %02X -> %02X", iData, iData, ByteUtils.rotate(iData, position)));
    }

    public void testTmp() {
        log.info(String.format("%d -> %02X", 56, 56));

        int hexToInt = Integer.parseInt("00033807", 16);
        log.info(String.format("%s ->%,d, %02X", "00033807", hexToInt, hexToInt));

        final String OFFSETS_Tri = "00033807:00061002:00007003";
        int[] data = ByteUtils.getByteInfo(OFFSETS_Tri);

        for (int index = 0; index < data.length; index++) {
            log.info("{}", data[index]);
        }
    }

    public void testToASCII() {
        final String ALPHABET_LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
        display(ALPHABET_LOWERCASE, ByteUtils.stringToBytesASCII(ALPHABET_LOWERCASE));

        log.info("");

        display(ALPHABET_LOWERCASE.toUpperCase(), ByteUtils.stringToBytesASCII(ALPHABET_LOWERCASE.toUpperCase()));
    }

    private void display(final String value, final byte[] convertedToASCII) {
        for (int index = 0; index < convertedToASCII.length; index++) {
            log.info(String.format("%s -> %02X", value.charAt(index), convertedToASCII[index]));
        }

    }
}
