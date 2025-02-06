import java.util.Scanner;

public class CompoundSecurity {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        CardManager manager = new CardManager();
        manager.loadFromFile();

        while (true) {
            System.out.println("\n=== Access Card Management System ===");
            System.out.println("1. Add Card");
            System.out.println("2. Modify Card");
            System.out.println("3. Revoke Card");
            System.out.println("4. Show All Cards");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1: //add
                    System.out.print("Enter Card ID: ");
                    String cardID = scanner.nextLine();
                    System.out.print("Enter Access Level (Low, Medium, High): ");
                    String level = scanner.nextLine();

                    AccessCard newCard = null;
                    if (level.equalsIgnoreCase("Low")) {
                        newCard = new LowAccessCard(cardID);
                    } else if (level.equalsIgnoreCase("Medium")) {
                        newCard = new MediumAccessCard(cardID);
                    } else if (level.equalsIgnoreCase("High")) {
                        newCard = new HighAccessCard(cardID);
                    } else {
                        System.out.println("Invalid access level!");
                        break;
                    }
                    manager.addCard(newCard);
                    break;

                case 2: //modify
                    System.out.print("Enter Card ID to Modify: ");
                    String modifyID = scanner.nextLine();
                    System.out.print("Enter New Access Level (Low, Medium, High): ");
                    String newLevel = scanner.nextLine();
                    manager.modifyCard(modifyID, newLevel);
                    break;

                case 3: //revoke
                    System.out.print("Enter Card ID to Revoke: ");
                    String revokeID = scanner.nextLine();
                    manager.revokeCard(revokeID);
                    break;

                case 4: //show
                    manager.showAllCards();
                    break;

                case 5: //exit
                    System.out.println("Exiting system...");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice! Please select a valid option.");
            }
        }
    }
}
