package thn.crypto.process;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import thn.crypto.utils.ByteUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoFile {
	private static final Logger log = LoggerFactory.getLogger(CryptoFile.class);

    private static final String ENCRYPT_FILE = "encrypt.file";
	private static final String DECRYPT_FILE = "decrypt.file";
	private static final String FIND_INFO = "find.bytes";
    private static final String FIND_INFO_1 = "find.bytes.1";
	private static final String EXTRACT_INFO = "extract.bytes";
	private static final String DISPLAY_BIN = "display.bin";

	public static void main(String[] args) {
	    final CryptoFile cryptoFile = new CryptoFile();
	    cryptoFile.run(args);
	}

	public void run(final String[] args) {
    	if (args.length >= 2) {
    	    final String command = args[0];

    		switch(command) {
    			case ENCRYPT_FILE: {
    				final File inputFile = Paths.get(args[1]).toFile();
    				final File outputFile = Paths.get(args[2]).toFile();
    				final String keys = args[3];
                    encryptFile(inputFile, outputFile, keys);
    				break;
    			}

    			case DECRYPT_FILE: {
    				final File inputFile = Paths.get(args[1]).toFile();
    				final File outputFile = Paths.get(args[2]).toFile();
    				final String keys = args[3];
    				decryptFile(inputFile, outputFile, keys);
    				break;
    			}

    			case FIND_INFO: {
    				final File inputFile = Paths.get(args[1]).toFile();
    				final String values = args[2];
    				findInfo(inputFile, values, false);
    				break;
    			}

                case FIND_INFO_1: {
                    final File inputFile = Paths.get(args[1]).toFile();
                    final String values = args[2];
                    findInfo(inputFile, values, true);
                    break;
                }

    			case EXTRACT_INFO: {
    				final File inputFile = Paths.get(args[1]).toFile();
    				final String offsets = args[2];
    				extractInfo(inputFile, offsets);
                    break;
    			}

    			case DISPLAY_BIN: {
    			    final File inputFile = Paths.get(args[1]).toFile();
    			    final int blockSize = Integer.parseInt(args[2]);
    			    final int numberOfBytes = Integer.parseInt(args[3]);
    			    displayBinaryFile(inputFile, blockSize, numberOfBytes);
                    break;
    			}

    			default: {
    			    log.info("Invalid command: " + command);
    				break;
    			}
    		}

    	} else {
    		displayUsage();
    	}
	}

	public void encryptFile(final File input, final File output, final String keys) {
		encryptFile(input, output, keys, 0, -1);
	}

	public void encryptFile(final File inputFile, final File outputFile, final String keys, final int start, final int end) {
	    try {
			final int[] intKeys = getKeys(keys);
			final byte[] inputData = ByteUtils.getByteArray(inputFile, start, end);
			final byte[] encodedData = ByteUtils.encode(inputData, start, inputData.length, intKeys);
			if (encodedData != null) {
				FileUtils.writeByteArrayToFile(outputFile, encodedData);
			}
		} catch (IOException e) {
			log.info("unable to encrypt: " + inputFile.toString(), e);
		}

	}

	public void decryptFile(final File inputFile, final File outputFile, final String keys) {
		decryptFile(inputFile, outputFile, keys, 0, -1);
	}

	public void decryptFile(final File inputFile, final File outputFile, final String keys, final int start, final int end) {
	    try {
			final int[] intKeys = getKeys(keys);
			final byte[] inputData = ByteUtils.getByteArray(inputFile, start, end);
			final byte[] decodedData = ByteUtils.decode(inputData, start, inputData.length, intKeys);
			if (decodedData != null) {
				FileUtils.writeByteArrayToFile(outputFile, decodedData);
			}
		} catch (final IOException e) {
			log.info("unable to decrypt: " + inputFile.toString(), e);
		}
	}

	// offset - position where a byte is extracted
	public void extractInfo(final File inputFile, final String offsetString) {
	    try {
			final int[] offsets = ByteUtils.getByteInfo(offsetString);
            final byte[][] extracted = new byte[offsets.length][2];

			final byte[] inputData = ByteUtils.getByteArray(inputFile, -1);
			for (int index = 0; index < offsets.length; index++) {
			    extracted[index][0] = (byte)offsets[index];
			    extracted[index][1] = inputData[offsets[index]];
			}
			displayExtractInfo(offsetString, extracted);
		} catch (final IOException e) {
			log.info("unable to decrypt: " + inputFile.toString(), e);
		}
	}

    private void displayExtractInfo(final String offsetString, final byte[][] info) {
        log.info("offsets: " + offsetString);

        // one holds the final string
        final StringBuilder one = new StringBuilder(32);
        for (int index = 0;  index < info.length; index++) {
            log.info(String.format("%06X -> %s [%02X]", info[index][0], (char)info[index][1], info[index][1]));
            one.append(String.format("%s", (char)info[index][1]));
        }
        log.info("--> " + one.toString());
    }

	// string values as ABCDE or aBcD or abcd
	public void findInfo(final File inputFile, final String values, final boolean exactOne) {
	    try {

	    	final byte[] byteValues = ByteUtils.stringToBytesASCII(values);
			final byte[] inputData = ByteUtils.getByteArray(inputFile, -1);

			if (exactOne) {
			    final StringBuilder one = new StringBuilder(32);
			    for (int index = 0; index < byteValues.length; index++) {
			        for (int indexInput = 0; indexInput < inputData.length; indexInput++) {
			            if (inputData[indexInput] == byteValues[index]) {
			                one.append(String.format("%06X", indexInput));
			                break;
			            }
			        }
			        if (index < (byteValues.length - 1)) {
			            one.append(":");
			        }
			    }
		        log.info("search: " + values);
			    log.info("--> " + one.toString());
			} else {
	            final Multimap<Byte, Integer> info = HashMultimap.create();
	            for (int index = 0; index < inputData.length; index++) {
	                if (ArrayUtils.contains(byteValues, inputData[index])) {
	                    info.put(inputData[index], index);
	                }
	            }
	            displayFindInfo(values, byteValues, info);
			}
		}
	    catch (final IOException e) {
			log.info("unable to decrypt: " + inputFile.toString(), e);
		}
	}

    private void displayFindInfo(final String values, final byte[] byteValues, final Multimap<Byte, Integer> info) {
        log.info("search: " + values);
        int index = 1;
        for (final byte key : byteValues) {
            index = 1;
            final StringBuilder results = new StringBuilder(32);
            results.append(String.format("%s [%02X] -> [", (char)ByteUtils.byteToInt(key), key));

            final Collection<Integer> keyValues = info.get(key);
            for (final Integer keyValue : keyValues) {
                if (index++ % 6 == 0) {
                    break;
                }

                if (results.toString().endsWith("[")) {
                    results.append(String.format("%06X", keyValue));
                } else {
                    results.append(String.format(", %06X", keyValue));
                }
            }
            results.append("]");
            log.info(results.toString());
        }
    }

	private void displayFindInfo(final String values, final Multimap<Byte, Integer> info) {
		log.info("search: " + values);

		for (final Byte key : info.keySet()) {
			final StringBuilder results = new StringBuilder(32);
			results.append(String.format("%s [%02X] -> [", (char)ByteUtils.byteToInt(key), key));

			final Collection<Integer> keyValues = info.get(key);
			int index = 1;

			for (final Integer keyValue : keyValues) {
				if (index++ % 5 == 0) {
					break;
				}

				if (results.toString().endsWith("[")) {
					results.append(String.format("%06X", keyValue));
				} else {
					results.append(String.format(", %06X", keyValue));
				}
			}
			results.append("]");
			log.info(results.toString());
		}
	}

	private void displayBinaryFile(final File inputFile, final int blockSize, final int numberOfBytes) {
	    final int displayBlockSize = (blockSize > 0 ? blockSize : 16);
	    final int displayNumberOfBytes = (numberOfBytes > 0 ? numberOfBytes : -1);
        try {
            final byte[] inputData = ByteUtils.getByteArray(inputFile, -1);
            ByteUtils.display(inputData, displayBlockSize, displayNumberOfBytes);
        }
        catch (final IOException e) {
            log.info("unable to display: " + inputFile.toString(), e);
        }

	}

	private int[] getKeys(final String keys) {
		final String[] extracted = keys.split(":");
		if (extracted.length > 0) {
			int[] intKeys = new int[extracted.length];
			for (int index = 0; index < extracted.length; index++) {
				intKeys[index] = Integer.parseInt(extracted[index]);
			}
			return intKeys;
		}
		return null;
	}

	public void displayUsage() {
	    final StringBuilder usage = new StringBuilder(32);
	    usage.append("\nUsage: CryptoFile [options]")
	        .append("\nwhere options are")
	        .append("\n- ").append(ENCRYPT_FILE).append(" <input file> <output file> <key>")
            .append("\n- ").append(DECRYPT_FILE).append(" <input file> <output file> <key>")
            .append("\n- ").append(FIND_INFO).append(" <input file> <search info>")
            .append("\n- ").append(FIND_INFO_1).append(" <input file> <search info>")
            .append("\n- ").append(EXTRACT_INFO).append(" <input file> <offset1[:offset2[:offset3:offset(n)]]>")
	        .append("\n- ").append(DISPLAY_BIN).append(" <input file> <block size> <number of bytes>")
	        .append("\n");

	    System.out.println(usage.toString());
	}
}
