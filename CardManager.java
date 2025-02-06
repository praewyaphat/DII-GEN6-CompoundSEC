import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CardManager {
    private List<AccessCard> cards = new ArrayList<>();
    private final String FILE_NAME = "cards.txt";

    public void addCard(AccessCard card) {
        if (card.getCardID().equals("CARD004")) {
            System.out.println("Skipping Card 4");
            return;
        }
        cards.add(card);
        System.out.println("Added: " + card.getCardID() + " | Level: " + card.getAccessLevel());
        saveToFile();
    }

    public void modifyCard(String cardID, String newLevel) {
        for (AccessCard card : cards) {
            if (card.getCardID().equals(cardID)) {
                card.setAccessLevel(newLevel);
                System.out.println("Modified: " + cardID + " -> New Level: " + newLevel);
                saveToFile();
                return;
            }
        }
        System.out.println("Card not found!");
    }

    public void revokeCard(String cardID) {
        cards.removeIf(card -> card.getCardID().equals(cardID));
        System.out.println("Revoked: " + cardID);
        saveToFile();
    }

    public void showAllCards() {
        System.out.println("\n--- All Cards ---");
        for (AccessCard card : cards) {
            card.showCardInfo();
        }
    }

    public void saveToFile() {
        try (FileWriter writer = new FileWriter(FILE_NAME)) {
            for (AccessCard card : cards) {
                writer.write(card.getCardID() + "," + card.getAccessLevel() + "\n");
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

    public void loadFromFile() {
        cards.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                String cardID = data[0];
                String level = data[1];

                if (cardID.equals("CARD004")) {
                    System.out.println("Skipping Card 4 from file.");
                    continue;
                }

                switch (level) {
                    case "Low":
                        cards.add(new LowAccessCard(cardID));
                        break;
                    case "Medium":
                        cards.add(new MediumAccessCard(cardID));
                        break;
                    case "High":
                        cards.add(new HighAccessCard(cardID));
                        break;
                }
            }
            System.out.println("Loaded data from file.");
        } catch (IOException e) {
            System.out.println("No previous data found. Starting fresh.");
        }
    }
}
