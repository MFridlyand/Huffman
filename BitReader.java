package compression;

import java.io.IOException;
import java.io.InputStream;

class BitReader {
	public BitReader(InputStream in) {
		this.in = in;
	}

	public int readBit() throws IOException {
		if (nBit == -1) {
			value = in.read();
			if (value == -1)
				return -1;
			nBit = 7;
		}
		int bit = value & (1 << nBit);
		nBit--;
		return bit == 0 ? 0 : 1;
	}

	public void close() throws IOException {
		in.close();
	}

	InputStream in;
	int value = 0;
	int nBit = -1;
}
