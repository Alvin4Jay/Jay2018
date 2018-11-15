package JavaNetworkProgramming.chapter2;

import java.io.IOException;
import java.io.OutputStream;

/**
 * 字符生成器
 *
 * @author xuanjian.xuwj
 */
public class CharacterGenerator {

	/**
	 * 每次写出一个字节
	 * @param out
	 * @throws IOException
	 */
	public static void generateCharacters(OutputStream out) throws IOException {
		int firstPrintableCharacter = 33;
		int numberOfPrintableCharacters = 94;
		int numberOfCharactersPerLine = 72;

		int start = firstPrintableCharacter;
		while (true) {
			for (int i = start; i < (start + numberOfCharactersPerLine); i++) {
				out.write((i - firstPrintableCharacter) % numberOfPrintableCharacters + firstPrintableCharacter);
			}
			out.write('\r');
			out.write('\n');
			start = ((start + 1) - firstPrintableCharacter) % numberOfPrintableCharacters + firstPrintableCharacter;
		}

	}

	/**
	 * 每次写出一个字节数组
	 * @param out
	 * @throws IOException
	 */
	public static void generateCharacters2(OutputStream out) throws IOException {
		int firstPrintableCharacter = 33;
		int numberOfPrintableCharacters = 94;
		int numberOfCharactersPerLine = 72;
		int start = firstPrintableCharacter;
		byte[] line = new byte[numberOfCharactersPerLine + 2];

		while (true) {
			for (int i = start; i < (start + numberOfCharactersPerLine); i++) {
				line[start - i] = (byte) ((i - firstPrintableCharacter) % numberOfPrintableCharacters
						+ firstPrintableCharacter);
			}
			line[72] = '\r';
			line[73] = '\n';
			out.write(line);
			start = ((start + 1) - firstPrintableCharacter) % numberOfPrintableCharacters + firstPrintableCharacter;
		}

	}

}
