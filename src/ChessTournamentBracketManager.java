import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChessTournamentBracketManager extends JFrame {

    private final List<String> players = new ArrayList<>();
    private final List<JRadioButton> winnerRadioButtons = new ArrayList<>();
    private final ButtonGroup winnerButtonGroup = new ButtonGroup();
    private Connection connection;
    private final JPanel panel;
    private final List<String> nextRoundWinners = new ArrayList<>();
    private JMenu roundWinnersMenu;

    public ChessTournamentBracketManager() {
        setTitle("Chess Tournament Bracket Manager");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel contentPane = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.WHITE);
                g.fillRect(0, 0, 200,200);
            }
        };
        contentPane.setLayout(null);
        setContentPane(contentPane);


        JMenuBar menuBar = createMenuBar();
        setJMenuBar(menuBar);

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/tournament", "root", "");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to connect to database.");
            System.exit(1);
        }

        panel = new JPanel();
        panel.setLayout(null);

        panel.setBounds(0, 0, 600, 600);
        getContentPane().add(panel);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupRoundWinnersData();
            }
        });

        JButton nextRoundButton = new JButton("Next Round");
        nextRoundButton.setBounds(400, 500, 150, 30);
        nextRoundButton.addActionListener(e -> advanceToNextRound());
        panel.add(nextRoundButton);
        setVisible(true);
    }

    void cleanupRoundWinnersData() {
        String deleteQuery = "DELETE FROM round_winners";
        try (PreparedStatement preparedStatement = connection.prepareStatement(deleteQuery)) {
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to delete round winners data.");
        }
    }

    void advanceToNextRound() {

        removeRound2Matches();

        setupNextRoundMatches(2);
    }

    private void removeRound2Matches() {
        Component[] components = panel.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel label) {
                String labelText = label.getText();
                if (labelText.startsWith("Round 1 Match") || labelText.startsWith("Round 2 Match")) {
                    panel.remove(component);
                }
            } else if (component instanceof JButton || component instanceof JRadioButton) {
                panel.remove(component);
            }
        }
        winnerRadioButtons.clear();
        winnerButtonGroup.clearSelection();
        panel.revalidate();
        panel.repaint();
    }

    private void setupNextRoundMatches(int roundNumber) {
        List<String> previousRoundWinners = getRoundWinnersFromDatabase(roundNumber - 1);

        int yOffset = 50;

        for (int i = 0; i < previousRoundWinners.size() / 2; i++) {
            String player1 = previousRoundWinners.get(i * 2);
            String player2 = previousRoundWinners.get(i * 2 + 1);

            JLabel matchLabel = new JLabel("Round " + roundNumber + " Match " + (i + 1) + ": " + player1 + " vs " + player2);
            matchLabel.setBounds(10, yOffset, 300, 25);
            panel.add(matchLabel);

            JRadioButton player1RadioButton = new JRadioButton(player1);
            JRadioButton player2RadioButton = new JRadioButton(player2);
            player1RadioButton.setBounds(350, yOffset, 120, 25);
            player2RadioButton.setBounds(500, yOffset, 120, 25);
            winnerButtonGroup.add(player1RadioButton);
            winnerButtonGroup.add(player2RadioButton);
            panel.add(player1RadioButton);
            panel.add(player2RadioButton);
            winnerRadioButtons.add(player1RadioButton);
            winnerRadioButtons.add(player2RadioButton);

            JButton selectButton = new JButton("Select");
            selectButton.setBounds(230, yOffset, 80, 25);
            int finalI = i;
            selectButton.addActionListener(e -> {
                String winner = winnerRadioButtons.get(finalI * 2).isSelected() ? player1 : player2;
                storeWinnerInDatabase(winner, roundNumber);
                panel.remove(matchLabel);
                panel.remove(player1RadioButton);
                panel.remove(player2RadioButton);
                panel.remove(selectButton);
                panel.revalidate();
                panel.repaint();
            });
            panel.add(selectButton);

            yOffset += 30;
        }
        JButton nextRoundButton = new JButton("Start Round 3");
        nextRoundButton.setBounds(400, yOffset, 150, 30);
        nextRoundButton.addActionListener(e -> startRound3());
        panel.add(nextRoundButton);
        panel.revalidate();
        panel.repaint();
    }

    private void startRound3() {
        List<String> round2Winners = getRoundWinnersFromDatabase(2);

        panel.removeAll();

        int yOffset = 50;

        for (int i = 0; i < round2Winners.size() / 2; i++) {
            String player1 = round2Winners.get(i * 2);
            String player2 = round2Winners.get(i * 2 + 1);

            JLabel matchLabel = new JLabel("Round 3 Match " + (i + 1) + ": " + player1 + " vs " + player2);
            matchLabel.setBounds(10, yOffset, 300, 25);
            panel.add(matchLabel);

            JRadioButton player1RadioButton = new JRadioButton(player1);
            JRadioButton player2RadioButton = new JRadioButton(player2);
            player1RadioButton.setBounds(350, yOffset, 120, 25);
            player2RadioButton.setBounds(500, yOffset, 120, 25);
            winnerButtonGroup.add(player1RadioButton);
            winnerButtonGroup.add(player2RadioButton);
            panel.add(player1RadioButton);
            panel.add(player2RadioButton);
            winnerRadioButtons.add(player1RadioButton);
            winnerRadioButtons.add(player2RadioButton);

            JButton selectButton = new JButton("Select");
            selectButton.setBounds(230, yOffset, 80, 25);
            int finalI = i;
            selectButton.addActionListener(e -> {
                String winner = winnerRadioButtons.get(finalI * 2).isSelected() ? player1 : player2;
                storeWinnerInDatabase(winner, 3);
                panel.remove(matchLabel);
                panel.remove(player1RadioButton);
                panel.remove(player2RadioButton);
                panel.remove(selectButton);
                panel.revalidate();
                panel.repaint();
                if (round2Winners.size() / 2 == 1) {
                    JOptionPane.showMessageDialog(null, "Congratulations! The winner of the tournament is: " + winner);
                }
            });
            panel.add(selectButton);

            yOffset += 30;
            panel.revalidate();
            panel.repaint();
        }
        setVisible(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu playerMenu = new JMenu("Players");
        JMenuItem addPlayerItem = new JMenuItem("Add Player");
        JMenuItem showPlayersItem = new JMenuItem("Show Players");
        addPlayerItem.addActionListener(e -> addPlayer());
        showPlayersItem.addActionListener(e -> showPlayersDialog());
        playerMenu.add(addPlayerItem);
        playerMenu.add(showPlayersItem);

        JMenu matchMenu = new JMenu("Set Match");

        JMenuItem automaticMatchItem = new JMenuItem("Team Matchup");

        automaticMatchItem.addActionListener(e -> automaticMatchSetup(new ArrayList<>(players)));

        matchMenu.add(automaticMatchItem);
        roundWinnersMenu = new JMenu("Round Winners");
        JMenuItem round1WinnerItem = new JMenuItem("Round 1");
        round1WinnerItem.addActionListener(e -> displayRoundWinners(1));
        roundWinnersMenu.add(round1WinnerItem);
        JMenuItem round2WinnerItem = new JMenuItem("Round 2");
        round2WinnerItem.addActionListener(e -> displayRoundWinners(2));
        roundWinnersMenu.add(round2WinnerItem);
        JMenuItem round3WinnerItem = new JMenuItem("Round 3");
        round3WinnerItem.addActionListener(e -> displayRoundWinners(3));
        roundWinnersMenu.add(round3WinnerItem);

        menuBar.add(playerMenu);
        menuBar.add(matchMenu);
        menuBar.add(roundWinnersMenu);
        return menuBar;
    }

    void addPlayer() {
        for (int i = 1; i <= 8; i++) {
            String playerName = JOptionPane.showInputDialog("Enter Player " + i + " Name:");
            if (playerName != null && !playerName.trim().isEmpty()) {
                players.add(playerName.trim());
                addPlayerToDatabase(playerName.trim());
            } else {
                break;
            }
        }
        JOptionPane.showMessageDialog(null, "Players added successfully!");
    }

    private void addPlayerToDatabase(String playerName) {
        String insertQuery = "INSERT INTO players (name) VALUES (?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, playerName);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to add player to the database.");
        }
    }

    void showPlayersDialog() {
        if (players.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No players added yet!");
        } else {
            StringBuilder playersString = new StringBuilder();
            for (String player : players) {
                playersString.append(player).append("\n");
            }
            JOptionPane.showMessageDialog(null, playersString.toString(), "Players List", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void automaticMatchSetup(List<String> winners) {
        if (winners.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No players added yet!");
            return;
        }

        int round = 1;
        int yOffset = 50;
        List<String> roundWinners = new ArrayList<>();

        while (winners.size() > 1) {
            final List<String> currentWinners = winners;
            int numWinners = currentWinners.size();
            int numMatches = numWinners / 2;
            nextRoundWinners.clear();

            for (int i = 0; i < numMatches; i++) {
                if (i * 2 >= currentWinners.size() || i * 2 + 1 >= currentWinners.size()) {
                    break;
                }

                String player1 = currentWinners.get(i * 2);
                String player2 = currentWinners.get(i * 2 + 1);

                JLabel matchLabel = new JLabel("Round " + round + " Match " + (i + 1) + ": " + player1 + " vs " + player2);
                matchLabel.setBounds(10, yOffset, 300, 25);
                panel.add(matchLabel);

                JRadioButton player1RadioButton = new JRadioButton(player1);
                JRadioButton player2RadioButton = new JRadioButton(player2);
                player1RadioButton.setBounds(350, yOffset, 120, 25);
                player2RadioButton.setBounds(500, yOffset, 120, 25);
                winnerButtonGroup.add(player1RadioButton);
                winnerButtonGroup.add(player2RadioButton);
                panel.add(player1RadioButton);
                panel.add(player2RadioButton);
                winnerRadioButtons.add(player1RadioButton);
                winnerRadioButtons.add(player2RadioButton);

                JButton selectButton = new JButton("Select");
                selectButton.setBounds(230, yOffset, 80, 25);
                int finalI = i;
                int finalRound = round;
                List<String> finalWinners = winners;
                selectButton.addActionListener(e -> {
                    if (winnerRadioButtons.get(finalI * 2).isSelected()) {
                        player1RadioButton.setSelected(true);
                        roundWinners.add(player1);
                        // Disable selected radio button
                        player1RadioButton.setEnabled(false);
                        player2RadioButton.setEnabled(false);
                        // Store winner in database
                        storeWinnerInDatabase(player1, finalRound);
                    } else if (winnerRadioButtons.get(finalI * 2 + 1).isSelected()) {
                        player2RadioButton.setSelected(true);
                        roundWinners.add(player2);
                        // Disable selected radio button
                        player1RadioButton.setEnabled(false);
                        player2RadioButton.setEnabled(false);
                        // Store winner in database
                        storeWinnerInDatabase(player2, finalRound);
                    } else {
                        JOptionPane.showMessageDialog(null, "Please select a winner first.");
                    }

                    if (roundWinners.size() == numMatches) {
                        nextRoundWinners.clear();
                        nextRoundWinners.addAll(roundWinners);
                        roundWinners.clear();

                        // Add round winners to the menu item
                        JMenuItem roundWinnerItem = new JMenuItem("Round " + finalRound + " Winner: " + finalWinners.getFirst());
                        roundWinnersMenu.add(roundWinnerItem);
                    }
                });
                panel.add(selectButton);

                yOffset += 30;
            }

            winners = nextRoundWinners;
            round++;
        }

        if (!winners.isEmpty()) {
            JOptionPane.showMessageDialog(null, "The winner of the tournament is: " + winners.getFirst());
        }
        panel.revalidate();
        panel.repaint();
    }



    void displayRoundWinners(int roundNumber) {
        List<String> roundWinners = getRoundWinnersFromDatabase(roundNumber);
        if (roundWinners.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No winners for round " + roundNumber);
        } else {
            StringBuilder winnersString = new StringBuilder();
            for (String winner : roundWinners) {
                winnersString.append(winner).append("\n");
            }
            JOptionPane.showMessageDialog(null, "Round " + roundNumber + " Winners:\n" + winnersString, "Round Winners", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    void storeWinnerInDatabase(String winner, int roundNumber) {
        String insertQuery = "INSERT INTO round_winners (round_number, winner) VALUES (?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setInt(1, roundNumber);
            preparedStatement.setString(2, winner);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to store winner in the database.");
        }
    }

    List<String> getRoundWinnersFromDatabase(int roundNumber) {
        List<String> roundWinners = new ArrayList<>();
        String selectQuery = "SELECT winner FROM round_winners WHERE round_number = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(selectQuery)) {
            preparedStatement.setInt(1, roundNumber);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                roundWinners.add(resultSet.getString("winner"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to retrieve round winners from the database.");
        }
        return roundWinners;
    }

    public static void main(String[] args) {
        new ChessTournamentBracketManager();
    }
}