import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CompoundSecurity extends JFrame {
    private CardManager manager;
    private JTextField cardIDField;
    private JComboBox<String> levelBox;
    private JTextArea displayArea;

    public CompoundSecurity() {
        manager = new CardManager();
        manager.loadFromFile();

        setTitle("Access Card Management System");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        JLabel cardLabel = new JLabel("Card ID:");
        cardIDField = new JTextField(10);

        JLabel levelLabel = new JLabel("Access Level:");
        String[] levels = {"Low", "Medium", "High"};
        levelBox = new JComboBox<>(levels);

        JButton addButton = new JButton("Add Card");
        JButton modifyButton = new JButton("Modify Card");
        JButton revokeButton = new JButton("Revoke Card");
        JButton showButton = new JButton("Show All Cards");

        displayArea = new JTextArea(10, 30);
        displayArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(displayArea);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                String level = (String) levelBox.getSelectedItem();

                AccessCard newCard = null;
                if (level.equalsIgnoreCase("Low")) {
                    newCard = new LowAccessCard(cardID);
                } else if (level.equalsIgnoreCase("Medium")) {
                    newCard = new MediumAccessCard(cardID);
                } else if (level.equalsIgnoreCase("High")) {
                    newCard = new HighAccessCard(cardID);
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid access level!");
                    return;
                }

                manager.addCard(newCard);
                JOptionPane.showMessageDialog(null, "Card Added Successfully!");
                cardIDField.setText("");
            }
        });

        modifyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                String newLevel = (String) levelBox.getSelectedItem();
                manager.modifyCard(cardID, newLevel);
                JOptionPane.showMessageDialog(null, "Card Modified Successfully!");
            }
        });

        revokeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String cardID = cardIDField.getText();
                manager.revokeCard(cardID);
                JOptionPane.showMessageDialog(null, "Card Revoked Successfully!");
            }
        });

        showButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayArea.setText(manager.getAllCards());
            }
        });

        add(cardLabel);
        add(cardIDField);
        add(levelLabel);
        add(levelBox);
        add(addButton);
        add(modifyButton);
        add(revokeButton);
        add(showButton);
        add(scrollPane);

        setVisible(true);
    }

    public String getAllCards() {
        StringBuilder result = new StringBuilder();
        for (AccessCard card : manager.getCardList()) {
            result.append(card.toString()).append("\n");
        }
        return result.toString();
    }


    public static void main(String[] args) {
        new CompoundSecurity();
    }
}
