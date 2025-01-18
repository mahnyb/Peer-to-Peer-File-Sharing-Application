import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class FileRequestHandler {
    private final File sharedFolder;

    public FileRequestHandler(File sharedFolder) {
        this.sharedFolder = sharedFolder;
    }

    public void handleFileRequest(String requestedFileName, InetAddress requesterAddress, int requesterPort, DatagramSocket socket) throws IOException {
        File file = findMatchingFile(requestedFileName);
        if (file == null) {
            System.out.println("File not found: " + requestedFileName);
            String notFoundResponse = "File " + requestedFileName + " not found.";

            DatagramPacket packet = new DatagramPacket(notFoundResponse.getBytes(), notFoundResponse.length(), requesterAddress, requesterPort);
            socket.send(packet);

            return;
        }

        String fileHash = computeFileHash(file);
        List<FileChunk> chunks = splitFileIntoChunks(file, fileHash);

        for (FileChunk chunk : chunks) {
            DatagramPacket packet = new DatagramPacket(chunk.getData(), chunk.getLength(), requesterAddress, requesterPort);
            socket.send(packet);
            System.out.println("Sent chunk " + (chunk.getChunkID() + 1) + "/" + chunks.size());
        }

        // If file transfer complete: hooray!
        // If not, gonna kms :(
        String completeMessage = "File transfer complete for " + requestedFileName;
        DatagramPacket completePacket = new DatagramPacket(completeMessage.getBytes(), completeMessage.length(), requesterAddress, requesterPort);
        socket.send(completePacket);
    }


    private File findMatchingFile(String fileName) {
        File[] files = sharedFolder.listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isFile() && file.getName().equalsIgnoreCase(fileName)) {
                return file;
            }
        }
        return null;
    }

    private String computeFileHash(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[256 * 1024];

            int bytesRead;

            while ((bytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = md.digest();
            StringBuilder hashString = new StringBuilder();

            for (byte b : hashBytes) {
                hashString.append(String.format("%02x", b));
            }

            return hashString.toString();
        }
        catch (Exception e) {
            throw new IOException("Error computing file hash: " + e.getMessage(), e);
        }
    }

    private List<FileChunk> splitFileIntoChunks(File file, String fileHash) throws IOException {
        List<FileChunk> chunks = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(file)) {

            byte[] buffer = new byte[256 * 1024];
            int bytesRead;
            int chunkID = 0;

            while ((bytesRead = fis.read(buffer)) > 0) {
                byte[] chunkData = Arrays.copyOf(buffer, bytesRead);
                chunks.add(new FileChunk(chunkID++, chunkData, bytesRead, fileHash));
            }
        }
        return chunks;
    }
}
