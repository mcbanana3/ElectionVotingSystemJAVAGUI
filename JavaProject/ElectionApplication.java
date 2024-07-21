import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

public class ElectionApplication extends JFrame {

    private ArrayList<String> areaCodes;
    private ArrayList<String[]> voters;
    private ArrayList<String[]> candidates;
    private JTabbedPane tabbedPane;
    int prevSelectedTabIndex = -1;

    private void loadAreaCodes() {
        areaCodes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("aCodes.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                areaCodes.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadVoters() {
        voters = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("voters.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                voters.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateVoters() {
        try (FileWriter writer = new FileWriter("voters.csv")) {
            for (String[] voter : voters) {
                writer.write(String.join(",", voter) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadCandidates() {
        candidates = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("candidates.csv"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                candidates.add(parts);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCandidates() {
        try (FileWriter writer = new FileWriter("candidates.csv")) {
            for (String[] candidate : candidates) {
                writer.write(String.join(",", candidate) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleAadhaarKeyEvent(KeyEvent evt, JTextField txtAadhaar) {

        if (txtAadhaar.getText().length() >= 12
                && !(evt.getKeyChar() == KeyEvent.VK_DELETE || evt.getKeyChar() == KeyEvent.VK_BACK_SPACE)) {
            getToolkit().beep();
            evt.consume();
        } else {
            char c = evt.getKeyChar();
            if (((c < '0') || (c > '9')) && (c != KeyEvent.VK_BACK_SPACE) && (c != KeyEvent.VK_DELETE)) {
                evt.consume(); // ignore event
            }
        }
    }

    private void handleNameKeyEvent(KeyEvent evt) {
        char c = evt.getKeyChar();

        if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c == ' ') || (c == KeyEvent.VK_BACK_SPACE)
                || (c == KeyEvent.VK_DELETE))
            ;
        else
            evt.consume(); // ignore event
    }

    private int getNumCandidates(String constituency) {

        int count = 0;

        for (int i = 0; i < candidates.size(); i++) {
            String[] candidate = candidates.get(i);

            if (candidate[1].equals(constituency))
                count++;
        }

        return count;
    }

    public ElectionApplication() {

        // Set up the frame
        setTitle("Election Application");
        setSize(640, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame
        setLayout(new BorderLayout());

        // Load area codes, voters, and candidates
        loadAreaCodes();
        loadVoters();
        loadCandidates();

        // Create tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Add Voter", createAddVoterPanel());
        tabbedPane.addTab("Add Candidate", createAddCandidatePanel());
        tabbedPane.addTab("Vote", createVotePanel());
        tabbedPane.addTab("View Results", createViewResultsPanel());

        add(tabbedPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JPanel createAddVoterPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));

        JLabel lblAadhaar = new JLabel("Aadhaar:");
        lblAadhaar.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtAadhaar = new JTextField();

        txtAadhaar.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                handleAadhaarKeyEvent(evt, txtAadhaar);
            }
        });

        JLabel lblConstituency = new JLabel("Constituency:");
        lblConstituency.setHorizontalAlignment(SwingConstants.CENTER);

        JComboBox<String> cmbConstituency = new JComboBox<>(areaCodes.toArray(new String[0]));

        JLabel lblName = new JLabel("Name:");
        lblName.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtName = new JTextField();

        txtName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                handleNameKeyEvent(evt);
            }
        });

        JLabel lblAge = new JLabel("Age:");
        lblAge.setHorizontalAlignment(SwingConstants.CENTER);

        JSlider sliderAge = new JSlider(18, 118);
        sliderAge.setMajorTickSpacing(10);
        sliderAge.setMinorTickSpacing(2);
        sliderAge.setPaintTicks(true);
        sliderAge.setPaintLabels(true);

        JLabel lblGender = new JLabel("Gender:");
        lblGender.setHorizontalAlignment(SwingConstants.CENTER);

        String[] genderOptions = { "Male", "Female" };
        JComboBox<String> cmbGender = new JComboBox<>(genderOptions);

        JButton btnClear = new JButton("Clear");
        JButton btnSubmit = new JButton("Submit");

        btnClear.addActionListener(e -> {
            txtAadhaar.setText("");
            cmbConstituency.setSelectedIndex(0);
            txtName.setText("");
            sliderAge.setValue(18);
            cmbGender.setSelectedIndex(0);
        });

        btnSubmit.addActionListener(e -> {
            String aadhaar = txtAadhaar.getText();
            String constituency = (String) cmbConstituency.getSelectedItem();
            String name = txtName.getText();
            int age = sliderAge.getValue();
            String gender = (String) cmbGender.getSelectedItem();

            // Validate inputs
            if (aadhaar.isEmpty() || aadhaar.length() != 12 || aadhaar.startsWith("0")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Aadhaar. Please enter a 12 digit integer not starting with zero.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (String[] voter : voters) {
                if (voter[0].equals(aadhaar)) {
                    JOptionPane.showMessageDialog(this, "Voter with the same Aadhaar already exists.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (constituency == null || constituency.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a constituency.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (name.isEmpty() || !name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(this, "Invalid name. Please enter letters and spaces only.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (gender == null || gender.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a gender.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // All checks passed, add the voter
            String[] newVoter = { aadhaar, constituency, name, String.valueOf(age), gender };
            voters.add(newVoter);
            updateVoters();

            JOptionPane.showMessageDialog(this, "Voter added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            btnClear.doClick(); // Clear the form after submission
        });

        panel.add(lblAadhaar);
        panel.add(txtAadhaar);
        panel.add(lblConstituency);
        panel.add(cmbConstituency);
        panel.add(lblName);
        panel.add(txtName);
        panel.add(lblAge);
        panel.add(sliderAge);
        panel.add(lblGender);
        panel.add(cmbGender);
        panel.add(btnClear);
        panel.add(btnSubmit);

        return panel;
    }

    private JPanel createAddCandidatePanel() {
        JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));

        JLabel lblAadhaar = new JLabel("Aadhaar:");
        lblAadhaar.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtAadhaar = new JTextField();

        txtAadhaar.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                handleAadhaarKeyEvent(evt, txtAadhaar);
            }
        });

        JLabel lblConstituency = new JLabel("Constituency:");
        lblConstituency.setHorizontalAlignment(SwingConstants.CENTER);

        JComboBox<String> cmbConstituency = new JComboBox<>(areaCodes.toArray(new String[0]));

        JLabel lblParty = new JLabel("Party:");
        lblParty.setHorizontalAlignment(SwingConstants.CENTER);

        String[] partyOptions = { "TDP", "YSRCP", "INC", "BJP", "TRS", "JSP" };
        JComboBox<String> cmbParty = new JComboBox<>(partyOptions);

        JLabel lblName = new JLabel("Name:");
        lblName.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtName = new JTextField();

        txtName.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                handleNameKeyEvent(evt);
            }
        });

        JLabel lblAge = new JLabel("Age:");
        lblAge.setHorizontalAlignment(SwingConstants.CENTER);

        JSlider sliderAge = new JSlider(18, 118);
        sliderAge.setMajorTickSpacing(10);
        sliderAge.setMinorTickSpacing(2);
        sliderAge.setPaintTicks(true);
        sliderAge.setPaintLabels(true);

        JLabel lblGender = new JLabel("Gender:");
        lblGender.setHorizontalAlignment(SwingConstants.CENTER);

        String[] genderOptions = { "Male", "Female" };
        JComboBox<String> cmbGender = new JComboBox<>(genderOptions);

        JButton btnClear = new JButton("Clear");
        JButton btnSubmit = new JButton("Submit");

        btnClear.addActionListener(e -> {
            txtAadhaar.setText("");
            cmbConstituency.setSelectedIndex(0);
            cmbParty.setSelectedIndex(0);
            txtName.setText("");
            sliderAge.setValue(18);
            cmbGender.setSelectedIndex(0);
        });

        btnSubmit.addActionListener(e -> {
            String aadhaar = txtAadhaar.getText();
            String constituency = (String) cmbConstituency.getSelectedItem();
            String party = (String) cmbParty.getSelectedItem();
            String name = txtName.getText();
            int age = sliderAge.getValue();
            String gender = (String) cmbGender.getSelectedItem();

            // Validate inputs
            if (aadhaar.isEmpty() || aadhaar.length() != 12 || aadhaar.startsWith("0")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Aadhaar. Please enter a 12 digit integer not starting with zero.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            for (String[] candidate : candidates) {
                if (candidate[0].equals(aadhaar)) {
                    JOptionPane.showMessageDialog(this, "Candidate with the same Aadhaar already exists.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            if (constituency == null || constituency.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a constituency.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (party == null || party.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a party.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (name.isEmpty() || !name.matches("[a-zA-Z ]+")) {
                JOptionPane.showMessageDialog(this, "Invalid name. Please enter letters and spaces only.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (gender == null || gender.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select a gender.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // All checks passed, add the candidate
            String[] newCandidate = { aadhaar, constituency, party, name, String.valueOf(age), gender, "0" }; // Vote
                                                                                                              // count
                                                                                                              // initialized
                                                                                                              // to 0
            candidates.add(newCandidate);
            updateCandidates();

            JOptionPane.showMessageDialog(this, "Candidate added successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            btnClear.doClick(); // Clear the form after submission
        });

        panel.add(lblAadhaar);
        panel.add(txtAadhaar);
        panel.add(lblConstituency);
        panel.add(cmbConstituency);
        panel.add(lblParty);
        panel.add(cmbParty);
        panel.add(lblName);
        panel.add(txtName);
        panel.add(lblAge);
        panel.add(sliderAge);
        panel.add(lblGender);
        panel.add(cmbGender);
        panel.add(btnClear);
        panel.add(btnSubmit);

        return panel;
    }

    private JPanel createVotePanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        JLabel lblAadhaar = new JLabel("Aadhaar:");
        lblAadhaar.setHorizontalAlignment(SwingConstants.CENTER);

        JTextField txtAadhaar = new JTextField();

        txtAadhaar.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent evt) {
                handleAadhaarKeyEvent(evt, txtAadhaar);
            }
        });

        JButton btnClear = new JButton("Clear");
        JButton btnSubmit = new JButton("Submit");

        btnClear.addActionListener(e -> {
            txtAadhaar.setText("");
        });

        btnSubmit.addActionListener(e -> {
            String aadhaar = txtAadhaar.getText();

            // Validate Aadhaar
            if (aadhaar.isEmpty() || aadhaar.length() != 12 || aadhaar.startsWith("0")) {
                JOptionPane.showMessageDialog(this,
                        "Invalid Aadhaar. Please enter a 12 digit integer not starting with zero.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean found = false;
            String constituency = "";

            for (String[] voter : voters) {
                if (voter[0].equals(aadhaar)) {
                    found = true;
                    constituency = voter[1];
                    break;
                }
            }

            if (!found) {
                JOptionPane.showMessageDialog(this, "Aadhaar not found in the list of voters.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            panel.removeAll();

            tabbedPane.insertTab("Vote", null, createVoteScreen(constituency), "Vote", 2);
            tabbedPane.removeTabAt(3);
            tabbedPane.setSelectedIndex(2);
        });

        panel.add(lblAadhaar);
        panel.add(txtAadhaar);
        panel.add(btnClear);
        panel.add(btnSubmit);

        return panel;
    }

    private JPanel createVoteScreen(String constituency) {

        int numCandidates = getNumCandidates(constituency);

        JPanel panel = new JPanel(new GridLayout(numCandidates + 2, 1, 10, 10));

        JLabel lblSelectCandidate = new JLabel("Select One of the Candidates Below:");
        lblSelectCandidate.setHorizontalAlignment(SwingConstants.CENTER);

        // Create radio buttons for each candidate
        ButtonGroup candidateGroup = new ButtonGroup();
        JRadioButton[] radioButtons = new JRadioButton[numCandidates];
        int[] cIndex = new int[numCandidates];

        for (int i = 0, j = 0; i < candidates.size(); i++) {
            String[] candidate = candidates.get(i);

            if (candidate[1].equals(constituency)) {
                String candidateInfo = candidate[3] + " (" + candidate[2] + ")";

                radioButtons[j] = new JRadioButton(candidateInfo);
                radioButtons[j].setHorizontalAlignment(SwingConstants.CENTER);

                candidateGroup.add(radioButtons[j]);

                cIndex[j] = i;
                j++;
            }
        }

        JButton btnVote = new JButton("Vote");

        btnVote.addActionListener(e -> {
            String selectedCandidate = null;
            for (int i = 0; i < radioButtons.length; i++) {
                if (radioButtons[i].isSelected()) {
                    selectedCandidate = candidates.get(cIndex[i])[0]; // Aadhaar of the selected candidate
                    break;
                }
            }

            if (selectedCandidate == null) {
                JOptionPane.showMessageDialog(this, "Please select a candidate to vote.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Increment vote count for the selected candidate
            for (String[] candidate : candidates) {
                if (candidate[0].equals(selectedCandidate)) {
                    int voteCount = Integer.parseInt(candidate[6]);
                    voteCount++;
                    candidate[6] = String.valueOf(voteCount); // Update vote count
                    updateCandidates(); // Update candidates.csv
                    break;
                }
            }

            panel.removeAll();

            tabbedPane.insertTab("Vote", null, createVotePanel(), "Vote", 2);
            tabbedPane.removeTabAt(3);

            tabbedPane.setSelectedIndex(3);
        });

        panel.add(lblSelectCandidate);
        for (JRadioButton radioButton : radioButtons) {
            panel.add(radioButton);
        }
        panel.add(btnVote);

        return panel;
    }

    private JPanel createViewResultsPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));

        JLabel lblConstituency = new JLabel("Constituency:");
        lblConstituency.setHorizontalAlignment(SwingConstants.CENTER);

        JComboBox<String> cmbConstituency = new JComboBox<>(areaCodes.toArray(new String[0]));

        JButton btnClear = new JButton("Clear");
        JButton btnSubmit = new JButton("Submit");

        btnClear.addActionListener(e -> {
            cmbConstituency.setSelectedIndex(0);
        });

        btnSubmit.addActionListener(e -> {
            String selectedConstituency = (String) cmbConstituency.getSelectedItem();

            if (selectedConstituency == null) {
                JOptionPane.showMessageDialog(this, "Please select a constituency.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Replace the current panel with the results screen for the selected
            // constituency
            panel.removeAll();

            tabbedPane.insertTab("Check Results", null, createResultsScreen(selectedConstituency), "Check results", 3);
            tabbedPane.removeTabAt(4);

            tabbedPane.setSelectedIndex(3);
        });

        panel.add(lblConstituency);
        panel.add(cmbConstituency);
        panel.add(btnClear);
        panel.add(btnSubmit);

        return panel;
    }

    private JPanel createResultsScreen(String constituency) {

        int numCandidates = getNumCandidates(constituency);

        JPanel panel = new JPanel(new GridLayout(numCandidates + 3, 1));

        JLabel lblResults = new JLabel("Election Results for Constituency: " + constituency);
        lblResults.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblResults);

        String maxVotesCandidate = null;
        int maxVotes = 0;

        for (int i = 0; i < candidates.size(); i++) {
            String[] candidate = candidates.get(i);

            if (candidate[1].equals(constituency)) {
                JLabel lblCandidate = new JLabel(candidate[3] + " (" + candidate[2] + "): " + candidate[6] + " votes");
                lblCandidate.setHorizontalAlignment(SwingConstants.CENTER);

                panel.add(lblCandidate);

                int votes = Integer.parseInt(candidate[6]); // Vote count is stored at index 6
                if (votes > maxVotes) {
                    maxVotes = votes;
                    maxVotesCandidate = candidate[3] + " (" + candidate[2] + ")"; // Name (Party)
                }
            }
        }

        JLabel lblMaxVotes = new JLabel("Winner: " + maxVotesCandidate);
        lblMaxVotes.setForeground(Color.RED);
        lblMaxVotes.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(lblMaxVotes);

        JButton btnBack = new JButton("Back");

        btnBack.addActionListener(e -> {
            panel.removeAll();

            tabbedPane.insertTab("Check Results", null, createViewResultsPanel(), "Check results", 3);
            tabbedPane.removeTabAt(4);

            tabbedPane.setSelectedIndex(3);
        });

        panel.add(btnBack);

        return panel;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ElectionApplication::new);
    }
}
