package thn.crypto.process;

import java.io.File;
import java.nio.file.Paths;

import org.junit.Test;

public class CryptoFileIT {
	private final String IMAGE_FILE = "data/image/mouse.jpg";

	@Test
	public void testFindInfo() {
		final File input = Paths.get(IMAGE_FILE).toFile();

		final CryptoFile crypto = new CryptoFile();

        System.out.println("");
		crypto.findInfo(input, "TriNguyen", false);

		System.out.println("");
		crypto.findInfo(input, "KevinMitnick", false);

        System.out.println("");
		crypto.findInfo(input, "JamesKosta", true);
	}

	@Test
	public void testExtractInfo() {
		final File input = Paths.get(IMAGE_FILE).toFile();

		final CryptoFile crypto = new CryptoFile();

		final String OFFSET_Tri = "033807:061002:007003";

        System.out.println("");
		crypto.extractInfo(input, OFFSET_Tri);

        System.out.println("");
		final String OFFSET_Kevin = "077006:072007:02C002:058005:029002";
		crypto.extractInfo(input, OFFSET_Kevin);

        System.out.println("");
		final String OFFSET_James = "027001:032803:04D006:072007:008000";
		final String OFFSET_Kosta = "053007:064004:007005:02900F:047804";
		final String OFFSET_JamesKosta = OFFSET_James + ":" + OFFSET_Kosta;
		crypto.extractInfo(input, OFFSET_JamesKosta);
	}
}
