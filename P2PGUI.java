import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class P2PGUI {
    private OverlayNetwork overlayNetwork;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(P2PGUI::new);
    }

    public P2PGUI() {
        // Main frame
        JFrame frame = new JFrame("Mahny Barazandehtar - 20210702004");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 700);
        frame.setLayout(new BorderLayout());

        // Menu bar and menus
        JMenuBar menuBar = new JMenuBar();
        JMenu filesMenu = new JMenu("Files");
        JMenuItem connectMenuItem = new JMenuItem("Connect");
        JMenuItem disconnectMenuItem = new JMenuItem("Disconnect");
        JMenuItem exitMenuItem = new JMenuItem("Exit");

        filesMenu.add(connectMenuItem);
        filesMenu.add(disconnectMenuItem);
        filesMenu.addSeparator();
        filesMenu.add(exitMenuItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem aboutMenuItem = new JMenuItem("About");
        helpMenu.add(aboutMenuItem);

        menuBar.add(filesMenu);
        menuBar.add(helpMenu);
        frame.setJMenuBar(menuBar);

        // About menu item action
        aboutMenuItem.addActionListener(e -> JOptionPane.showMessageDialog(
                frame,
                "CSE 471 - Term Project\n" +
                        "Developed by: Mahny Barazandehtar\n" +
                        "Student ID: 20210702004\n" +
                        "Email: mahny.barazandehtar@std.yeditepe.edu.tr",
                "About",
                JOptionPane.INFORMATION_MESSAGE
        ));

        // Connect menu item action
        connectMenuItem.addActionListener(e -> {
            try {
                if (overlayNetwork == null) {
                    overlayNetwork = new OverlayNetwork(9000, "Node1", 3);
                    overlayNetwork.startDiscovery();
                    JOptionPane.showMessageDialog(frame, "Connected to P2P network! :D", "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    JOptionPane.showMessageDialog(frame, "Already connected! :3", "Info", JOptionPane.WARNING_MESSAGE);
                }
            }
            catch (IOException io_exception) {
                io_exception.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error connecting to P2P network: " + io_exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Disconnect menu item action
        disconnectMenuItem.addActionListener(e -> {
            if (overlayNetwork != null) {
                overlayNetwork.stopDiscovery();
                overlayNetwork = null;
                JOptionPane.showMessageDialog(frame, "Disconnected from P2P network. :P", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
            else {
                JOptionPane.showMessageDialog(frame, "Not connected to any network! :P", "Info", JOptionPane.WARNING_MESSAGE);
            }
        });

        // Exit menu item action
        exitMenuItem.addActionListener(e -> System.exit(0));

        // Main panel and layout
        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.BOTH;

        // Root folder setting
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weighty = 0;
        gbc.gridwidth = 3;
        mainPanel.add(new JLabel("Root of the P2P shared folder"), gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        JTextField rootFolderField = new JTextField("/Users/mahny/SharedFiles");
        mainPanel.add(rootFolderField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton rootFolderSetButton = new JButton("Set");
        mainPanel.add(rootFolderSetButton, gbc);

        rootFolderSetButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File sharedFolder = chooser.getSelectedFile();
                rootFolderField.setText(sharedFolder.getAbsolutePath());
                if (overlayNetwork != null) {
                    overlayNetwork.setSharedFolder(sharedFolder);
                }
            }
        });

        // Destination folder setting
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 3;
        mainPanel.add(new JLabel("Destination folder"), gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        JTextField destinationFolderField = new JTextField("/Users/mahny/DownloadedFiles");
        mainPanel.add(destinationFolderField, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0;
        JButton destinationFolderSetButton = new JButton("Set");
        mainPanel.add(destinationFolderSetButton, gbc);

        destinationFolderSetButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                File downloadFolder = chooser.getSelectedFile();
                destinationFolderField.setText(downloadFolder.getAbsolutePath());
                if (overlayNetwork != null) {
                    overlayNetwork.setDownloadFolder(downloadFolder);
                }
            }
        });

        // Settings panel
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 3;
        gbc.weighty = 0;
        JPanel settingsPanel = new JPanel(new GridLayout(1, 2, 5, 5));
        settingsPanel.setBorder(BorderFactory.createTitledBorder("Settings"));

        // Folder exclusion panel
        JPanel folderExclusionPanel = new JPanel(new BorderLayout());
        folderExclusionPanel.setBorder(BorderFactory.createTitledBorder("Folder exclusion"));

        JCheckBox checkNewFilesRoot = new JCheckBox("Check new files only in the root");
        folderExclusionPanel.add(checkNewFilesRoot, BorderLayout.NORTH);

        DefaultListModel<String> folderListModel = new DefaultListModel<>();
        JList<String> folderList = new JList<>(folderListModel);
        JScrollPane folderListScrollPane = new JScrollPane(folderList);
        folderExclusionPanel.add(folderListScrollPane, BorderLayout.CENTER);

        JPanel folderButtonsPanel = new JPanel(new GridLayout(1, 2));
        JButton addFolderButton = new JButton("Add");
        JButton deleteFolderButton = new JButton("Del");
        folderButtonsPanel.add(addFolderButton);
        folderButtonsPanel.add(deleteFolderButton);
        folderExclusionPanel.add(folderButtonsPanel, BorderLayout.SOUTH);

        addFolderButton.addActionListener(e -> {
            String folderToAdd = JOptionPane.showInputDialog("Enter the folder path to exclude:");
            if (folderToAdd != null && !folderToAdd.trim().isEmpty()) {
                folderListModel.addElement(folderToAdd);
            }
        });

        deleteFolderButton.addActionListener(e -> {
            int selectedIndex = folderList.getSelectedIndex();
            if (selectedIndex != -1) {
                folderListModel.remove(selectedIndex);
            }
        });

        // File mask panel
        JPanel fileMaskPanel = new JPanel(new BorderLayout());
        fileMaskPanel.setBorder(BorderFactory.createTitledBorder("Exclude files matching these masks"));

        JTextArea fileMaskArea = new JTextArea("*.exe\n download.dat");
        JScrollPane fileMaskScrollPane = new JScrollPane(fileMaskArea);
        fileMaskPanel.add(fileMaskScrollPane, BorderLayout.CENTER);

        JPanel fileMaskButtonsPanel = new JPanel(new GridLayout(1, 2));
        JButton addMaskButton = new JButton("Add");
        JButton deleteMaskButton = new JButton("Del");
        fileMaskButtonsPanel.add(addMaskButton);
        fileMaskButtonsPanel.add(deleteMaskButton);
        fileMaskPanel.add(fileMaskButtonsPanel, BorderLayout.SOUTH);

        settingsPanel.add(folderExclusionPanel);
        settingsPanel.add(fileMaskPanel);
        mainPanel.add(settingsPanel, gbc);

        // Downloading files panel
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 3;
        gbc.weighty = 1;
        JPanel downloadingFilesPanel = new JPanel(new BorderLayout());
        downloadingFilesPanel.setBorder(BorderFactory.createTitledBorder("Downloading files"));
        JTextArea downloadingFilesArea = new JTextArea();
        downloadingFilesPanel.add(new JScrollPane(downloadingFilesArea), BorderLayout.CENTER);
        mainPanel.add(downloadingFilesPanel, gbc);

        // Found files panel
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 3;
        gbc.weighty = 1;
        JPanel foundFilesPanel = new JPanel(new BorderLayout());
        foundFilesPanel.setBorder(BorderFactory.createTitledBorder("Found files"));
        JTextArea foundFilesArea = new JTextArea();
        foundFilesPanel.add(new JScrollPane(foundFilesArea), BorderLayout.CENTER);
        mainPanel.add(foundFilesPanel, gbc);

        // Search panel
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 3;
        gbc.weighty = 0;
        JPanel searchPanel = new JPanel(new BorderLayout());
        JButton searchButton = new JButton("Search");
        JTextField searchField = new JTextField();
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);
        mainPanel.add(searchPanel, gbc);

        // Search button action
        searchButton.addActionListener(e -> {
            String fileName = searchField.getText().trim();
            if (!fileName.isEmpty()) {
                try {
                    overlayNetwork.requestFile(fileName);
                    downloadingFilesArea.append("Requested file: " + fileName + "\n");
                }
                catch (IOException io_exception) {
                    io_exception.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Error requesting file: " + io_exception.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        frame.add(mainPanel, BorderLayout.CENTER);
        frame.setVisible(true);
    }
}