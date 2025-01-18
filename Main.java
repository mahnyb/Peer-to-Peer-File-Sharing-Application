import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Main {
    public static void main(String[] args) {
        try {
            String nodeID = System.getenv("NODE_ID") != null ? System.getenv("NODE_ID") : "PeerNode";
            int port = 9000;  // Static virtual port inside container

            OverlayNetwork overlayNetwork = new OverlayNetwork(port, nodeID, 3);

            overlayNetwork.setSharedFolder(new File("/SharedFiles"));
            overlayNetwork.setDownloadFolder(new File("/DownloadedFiles"));

            System.out.println("Starting CLI peer " + nodeID + " on port " + port);
            overlayNetwork.startCLI();

            DatagramSocket socket = new DatagramSocket(port);
            byte[] buffer = new byte[1024];

            while (true) {

                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String received = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received request: " + received);

                String response = "Hello from " + nodeID + "! Received your request.";
                DatagramPacket responsePacket = new DatagramPacket(
                        response.getBytes(),
                        response.length(),
                        packet.getAddress(),
                        packet.getPort()
                );
                socket.send(responsePacket);
                System.out.println("Sent response to: " + packet.getAddress() + ":" + packet.getPort());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}