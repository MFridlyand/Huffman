javac -d bin *.java
cd bin
jar cfe Huffman.jar Main Main.class *
cd ..