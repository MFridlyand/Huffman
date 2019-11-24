package compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Huffman {

	public static byte[] encode(byte[] input) throws IOException {
		int[] dictionary = buildDictionary(input);
		Node root = buildHuffmanTree(dictionary);
		String[] codeTable = buildCodeTable(root);
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		writeHeader(dictionary, input.length, stream);
		encode(input, codeTable, stream);
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

	private static void encode(byte[] input, String[] codeTable, ByteArrayOutputStream stream) throws IOException {
		BitWriter w = new BitWriter(stream);
		for (byte b : input)
			encodeByte(b, codeTable, w);
		w.close();
	}

	private static void encodeByte(byte b, String[] codeTable, BitWriter w) throws IOException {
		int index = byte2index(b);
		String code = codeTable[index];
		for (char c : code.toCharArray()) {
			boolean bit = c == '1';
			w.writeBit(bit);
		}
	}

	private static int byte2index(byte b) {
		int index = b + 128; // < 0 ? b + 256 : b;
		return index;
	}

	private static byte index2byte(int index) {
		assert index < 256 && index >= 0;
		byte b = (byte) (index - 128); // < 0 ? b + 256 : b;
		return b;
	}

	private static int[] buildDictionary(byte[] input) {
		int[] dictionary = new int[256];
		for (byte b : input) {
			int index = byte2index(b);
			dictionary[index]++;
		}
		return dictionary;
	}

	private static void decode(ByteArrayInputStream stream, Node root, int dataSize, ByteArrayOutputStream out)
			throws IOException {
		BitReader reader = new BitReader(stream);
		for (int i = 0; i < dataSize; i++) {
			decodeByte(root, reader, out);
		}
	}

	private static void decodeByte(Node root, BitReader reader, ByteArrayOutputStream out) throws IOException {
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

	private static class Node {
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

	private static class Header {
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
		for (int item : dictionary) {
			if (item != 0)
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
		final PriorityQueue<Node> nodes = buildHeap(dictionary);
		while (nodes.size() > 1) {
			final Node n0 = nodes.poll();
			final Node n1 = nodes.poll();

			final Node newNode = new Node((byte) 0, n0.freq + n1.freq);
			newNode.left = n0;
			newNode.right = n1;
			nodes.offer(newNode);
		}
		// printTree(nodes.get(0), "");
		return nodes.peek();
	}

	private static PriorityQueue<Node> buildHeap(int[] dictionary) {
		PriorityQueue<Node> nodes = new PriorityQueue<>(dictionary.length, new Comparator<Node>() {
			@Override
			public int compare(Node arg0, Node arg1) {
				return arg0.freq - arg1.freq;
			}
		});
		for (int i = 0; i < dictionary.length; i++) {
			if (dictionary[i] != 0)
				nodes.offer(new Node(index2byte(i), dictionary[i]));
		}
		return nodes;
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

	@SuppressWarnings("unused")
	private static void printTree(Node node, String margin) {
		if (node != null) {
			System.out.println(margin + "freq: " + node.freq + " val :" + (char) node.val);
			printTree(node.left, margin + "\t");
			printTree(node.right, margin + "\t");
		}
	}
}
