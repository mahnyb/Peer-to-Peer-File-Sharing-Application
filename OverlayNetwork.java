import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class OverlayNetwork {

    private final int udpPort;
    private final MulticastSocket multicastSocket;  // Multicast for discovery
    private final Set<Peer> peers;
    private final int maxHops;  // Limited scope flooding
    private final String nodeID;
    private boolean running;
    private File sharedFolder;
    private File downloadFolder;

    private final String multicastGroupAddress = "224.0.0.1";  // Global multicast address

    public OverlayNetwork(int udpPort, String nodeID, int maxHops) throws IOException {
        this.udpPort = udpPort;
        this.peers = ConcurrentHashMap.newKeySet();
        this.nodeID = nodeID;
        this.maxHops = maxHops;
        this.running = true;

        this.multicastSocket = new MulticastSocket(udpPort);
        this.multicastSocket.setReuseAddress(true);
        this.multicastSocket.setTimeToLive(128);

        NetworkInterface networkInterface = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
        System.out.println("Joining multicast group over interface: " + networkInterface);
        this.multicastSocket.joinGroup(new InetSocketAddress(InetAddress.getByName(multicastGroupAddress), udpPort), networkInterface);
    }

    public void startDiscovery() {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.execute(this::listenForMessages);  // Listener for incoming messages
        executor.execute(this::sendDiscoveryMessages);  // Sends discovery messages every now and then
    }

    private void listenForMessages() {
        try {
            while (running) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println("Received message: " + receivedMessage + " from " + packet.getAddress());

                handleDiscoveryMessage(receivedMessage, packet.getAddress(), packet.getPort());
            }
        }
        catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    // No worky :/
    private void sendDiscoveryMessages() {
        try {
            while (running) {
                String message = "DISCOVERY:" + nodeID + ":" + maxHops;
                byte[] data = message.getBytes();
                DatagramPacket packet = new DatagramPacket(
                        data,
                        data.length,
                        InetAddress.getByName(multicastGroupAddress),
                        udpPort
                );
                multicastSocket.send(packet);
                System.out.println("Sent discovery message to " + multicastGroupAddress);
                Thread.sleep(3000);  // Send discovery messages over multicast every 3 sesc
            }
        }
        catch (Exception e) {
            if (running) {
                e.printStackTrace();
            }
        }
    }

    private void handleDiscoveryMessage(String message, InetAddress senderAddress, int senderPort) {
        System.out.println("Received message: " + message + " from " + senderAddress);

        try {
            String[] parts = message.split(":");
            if (parts.length < 3) {
                return;
            }

            String messageType = parts[0];
            String senderID = parts[1];
            int timeToLive = Integer.parseInt(parts[2]);

            if ("DISCOVERY".equals(messageType) && timeToLive > 0 && !nodeID.equals(senderID)) {
                peers.add(new Peer(senderID, senderAddress.getHostAddress(), senderPort));
                System.out.println("Discovered peer: " + senderID);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopDiscovery() {
        running = false;

        if (multicastSocket != null && !multicastSocket.isClosed()) {
            try {
                multicastSocket.leaveGroup(new InetSocketAddress(InetAddress.getByName(multicastGroupAddress), udpPort), NetworkInterface.getByInetAddress(InetAddress.getLocalHost()));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            multicastSocket.close();
        }

        System.out.println("Disconnected from multicast network.");
    }

    public void setSharedFolder(File folder) {
        this.sharedFolder = folder;
        System.out.println("Shared folder set to: " + sharedFolder.getAbsolutePath());
    }

    public void setDownloadFolder(File folder) {
        this.downloadFolder = folder;
        System.out.println("Download folder set to: " + downloadFolder.getAbsolutePath());
    }

    public void requestFile(String fileName) throws IOException {
        if (downloadFolder == null) {
            System.err.println("Download folder not set!");
            return;
        }

        System.out.println("Requesting file: " + fileName);
        byte[] requestData = ("REQUEST_FILE:" + fileName + ":" + nodeID).getBytes();

        DatagramPacket packet = new DatagramPacket(requestData, requestData.length, InetAddress.getByName(multicastGroupAddress), udpPort);
        multicastSocket.send(packet);
    }

    private static class Peer {
        String id;
        String ipAddress;
        int port;

        Peer(String id, String ipAddress, int port) {
            this.id = id;
            this.ipAddress = ipAddress;
            this.port = port;
        }

        @Override
        public String toString() {
            return "Peer{id='" + id + "', ipAddress='" + ipAddress + "', port=" + port + '}';
        }
    }

    // Dockers initialized in CLI
    public void startCLI() {
        System.out.println("Peer running in CLI mode...");
        startDiscovery();
    }
}