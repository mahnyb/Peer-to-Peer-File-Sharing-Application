// I tried so hard and got so far
// but file transfer didn't even get a chance :(((
// Oh my poor child of mine...
public class FileChunk {
    private final int chunkID;
    private final byte[] data;
    private final int length;
    private final String fileHash;

    public FileChunk(int chunkID, byte[] data, int length, String fileHash) {
        this.chunkID = chunkID;
        this.data = data;
        this.length = length;
        this.fileHash = fileHash;
    }

    public int getChunkID() {
        return chunkID;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return length;
    }

    public String getFileHash() {
        return fileHash;
    }
}
