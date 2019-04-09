package compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Huffman {

	public static byte[] encode(byte[] input) throws IOException {
		int[] dictionary = buildDictionary(input);
		Node root = buildHuffmanTree(dictionary);
		String[] codeTable = buildCodeTable(root);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writeHeader(dictionary, input.length, stream);
		encode(input, stream, codeTable);
		return stream.toByteArray();
	}

	public static byte[] decode(byte[] input) throws IOException {
		ByteArrayInputStream stream = new ByteArrayInputStream(input);
		Header header = readHeader(stream);
		Node root = buildHuffmanTree(header.dictionary);
		int dataSize = header.dataSize;
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		decode(stream, root, dataSize, out);
		return out.toByteArray();
	}

	private static void encode(byte[] input, ByteArrayOutputStream stream, String[] codeTable) throws IOException {
		BitWriter w = new BitWriter(stream);
		for (int i = 0; i < input.length; i++) {
			int index = byte2index(input[i]);
			String code = codeTable[index];
			for (int j = 0; j < code.length(); j++) {
				boolean bit = code.charAt(j) == '1' ? true : false;
				w.writeBit(bit);
			}
		}
		w.close();
	}

	private static int byte2index(byte b) {
		int index = b + 128; // < 0 ? b + 256 : b;
		assert index < 256 && index >= 0;
		return index;
	}

	private static byte index2byte(int index) {
		byte b = (byte) (index - 128); // < 0 ? b + 256 : b;
		return b;
	}

	private static int[] buildDictionary(byte[] input) {
		int[] dictionary = new int[256];
		for (int i = 0; i < input.length; i++) {
			int index = byte2index(input[i]);
			dictionary[index]++;
		}
		return dictionary;
	}

	private static void decode(ByteArrayInputStream stream, Node root, int dataSize, ByteArrayOutputStream out)
			throws IOException {
		BitReader reader = new BitReader(stream);
		for (int i = 0; i < dataSize; i++) {
			Node node = root;
			while (!node.isLeaf()) {
				int bit = reader.readBit();
				if (bit == 0)
					node = node.left;
				else
					node = node.right;
			}
			out.write(node.val);
		}
	}

	static class Node {
		public int freq;
		public byte val;
		public Node left, right;

		public Node(byte val, int freq) {
			this.freq = freq;
			this.val = val;
		}

		public boolean isLeaf() {
			return left == null && right == null;
		}
	}

	static class Header {
		public Header(int[] dictionary, int dataSize) {
			this.dictionary = dictionary;
			this.dataSize = dataSize;
		}

		int[] dictionary;
		int dataSize;
	}

	private static void writeHeader(int[] dictionary, int inputLength, OutputStream stream) throws IOException {
		DataOutputStream dataOutput = new DataOutputStream(stream);
		dataOutput.writeInt(inputLength);
		int tableItemsCount = 0;
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i] != 0)
				tableItemsCount++;
		}
		dataOutput.writeInt(tableItemsCount);
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i] != 0) {
				dataOutput.writeByte(index2byte(i));
				dataOutput.writeInt(dictionary[i]);
			}
		}
	}

	private static Header readHeader(InputStream stream) throws IOException {
		DataInputStream dataInput = new DataInputStream(stream);
		int dataSize = dataInput.readInt();
		int tableSize = dataInput.readInt();
		int[] dict = new int[256];
		for (int i = 0; i < tableSize; i++) {
			byte v = dataInput.readByte();
			int index = byte2index(v);
			int freq = dataInput.readInt();
			dict[index] = freq;
		}
		return new Header(dict, dataSize);
	}

	private static Node buildHuffmanTree(int[] dictionary) {
		ArrayList<Node> nodes = new ArrayList<>();
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i] != 0)
				nodes.add(new Node(index2byte(i), dictionary[i]));
		}
		while (nodes.size() > 1) {
			Node n0 = nodes.get(0);
			Node n1 = nodes.get(1);
			if (n0.freq > n1.freq) {
				Node t = n0;
				n0 = n1;
				n1 = t;
			}
			for (int i = 2; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				if (node.freq < n0.freq) {
					n1 = n0;
					n0 = node;
				} else if (node.freq < n1.freq)
					n1 = node;
			}

			Node newNode = new Node((byte) 0, n0.freq + n1.freq);
			newNode.left = n0;
			newNode.right = n1;

			ArrayList<Node> modifiedNodes = new ArrayList<>();
			for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				if (node != n0 && node != n1)
					modifiedNodes.add(node);
			}
			modifiedNodes.add(newNode);
			nodes = modifiedNodes;
		}
		// printTree(nodes.get(0), "");
		return nodes.get(0);
	}

	private static String[] buildCodeTable(Node node) {
		String[] codeTable = new String[256];
		traverse(node, "", codeTable);
		return codeTable;
	}

	private static void traverse(Node node, String code, String[] codeTable) {
		if (node.isLeaf()) {
			codeTable[byte2index(node.val)] = code;
		} else {
			traverse(node.left, code + "0", codeTable);
			traverse(node.right, code + "1", codeTable);
		}
	}

	private static void printTree(Node node, String margin) {
		if (node != null) {
			System.out.println(margin + "freq: " + node.freq + " val :" + (char) node.val);
			printTree(node.left, margin + "\t");
			printTree(node.right, margin + "\t");
		}
	}
}
