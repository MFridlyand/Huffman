import java.io.IOException;
import java.io.OutputStream;

class BitWriter {
	public BitWriter(OutputStream out) {
		this.out = out;
	}

	public void writeBit(boolean val) throws IOException {
		if (val)
			value = value | (1 << bit);
		bit--;
		if (bit == -1) {
			out.write(value);
			bit = 7;
			value = 0;
		}
	}

	public void close() throws IOException {
		if (bit != 7)
			out.write(value);
		out.close();
	}

	int value = 0;
	int bit = 7;
	OutputStream out;
}
