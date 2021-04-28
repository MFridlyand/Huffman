import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			usage();
			return;
		}
		String mode = args[0];
		if (mode.equals("e")) {
			String in = args[1];
			String out = args[2];
			Path inPath = Paths.get(in);
			byte[] input = Files.readAllBytes(inPath);
			byte[] output = Huffman.encode(input);
			Path outPath = Paths.get(out);
			Files.write(outPath, output);
		} else if (mode.equals("d")) {
			String in = args[2];
			String out = args[1];
			Path inPath = Paths.get(in);
			byte[] input = Files.readAllBytes(inPath);
			byte[] output = Huffman.decode(input);
			Path outPath = Paths.get(out);
			Files.write(outPath, output);
		} else {
			usage();
		}
	}
	private static void usage() {
		System.out.println("usage:");
		System.out.println("encode: e input_file_name output_file_name");
		System.out.println("decode: d output_file_name input_file_name");
	}
}
