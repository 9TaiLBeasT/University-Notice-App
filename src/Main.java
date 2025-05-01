import java.util.List;
import java.util.Scanner;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        NoticeDAO dao = new NoticeDAO();
        Scanner scanner = new Scanner(System.in);
        int choice;

        System.out.println("🎓 University Announcements System 🎓");

        do {
            System.out.println("\n========================");
            System.out.println("1. Add a New Notice");
            System.out.println("2. View All Notices");
            System.out.println("3. Delete a Notice");
            System.out.println("4. Update a Notice");
            System.out.println("5. Exit");
            System.out.print("Enter your choice: ");

            choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            try {
                switch (choice) {
                    case 1:
                        System.out.print("Enter Title: ");
                        String title = scanner.nextLine();

                        System.out.print("Enter Content: ");
                        String content = scanner.nextLine();

                        System.out.print("Enter Category (General/Faculty/Student): ");
                        String category = scanner.nextLine();

                        Notice newNotice = new Notice(title, content, category);
                        dao.addNotice(newNotice);

                        // ✅ Send push notification
                        try {
                            FCMSender.sendPushNotification(title, content);
                            System.out.println("📲 Push notification sent.");
                        } catch (Exception e) {
                            System.out.println("❌ Failed to send push notification: " + e.getMessage());
                        }

                        System.out.println("✅ Notice added successfully.");
                        break;

                    case 2:
                        List<Notice> notices = dao.getAllNotices();
                        if (notices.isEmpty()) {
                            System.out.println("📭 No notices available.");
                        } else {
                            System.out.println("\n📢 All Notices:");
                            for (Notice n : notices) {
                                System.out.println("\n------------------------");
                                System.out.println("ID: " + n.getId());
                                System.out.println("Title: " + n.getTitle());
                                System.out.println("Content: " + n.getContent());
                                System.out.println("Category: " + n.getCategory());
                                System.out.println("Posted On: " + n.getCreatedAt());
                            }
                        }
                        break;

                    case 3:
                        System.out.print("Enter Notice ID to delete: ");
                        int deleteId = scanner.nextInt();
                        dao.deleteNotice(deleteId);
                        System.out.println("🗑️ Notice deleted.");
                        break;

                    case 4:
                        List<Notice> noticesToUpdate = dao.getAllNotices();
                        if (noticesToUpdate.isEmpty()) {
                            System.out.println("📭 No notices available to update.");
                            break;
                        }

                        System.out.println("\n🔧 Notices Available to Update:");
                        for (Notice n : noticesToUpdate) {
                            System.out.println("ID: " + n.getId() + " | Title: " + n.getTitle());
                        }

                        System.out.print("Enter the ID of the notice you want to update: ");
                        int updateId = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        System.out.println("What do you want to update?");
                        System.out.println("1. Title");
                        System.out.println("2. Content");
                        System.out.println("3. Category");
                        System.out.println("4. All");
                        System.out.print("Enter your choice: ");
                        int updateChoice = scanner.nextInt();
                        scanner.nextLine(); // consume newline

                        String updatedTitle = null;
                        String updatedContent = null;
                        String updatedCategory = null;

                        switch (updateChoice) {
                            case 1:
                                System.out.print("Enter New Title: ");
                                updatedTitle = scanner.nextLine();
                                dao.updateNotice(updateId, updatedTitle, null, null);
                                break;
                            case 2:
                                System.out.print("Enter New Content: ");
                                updatedContent = scanner.nextLine();
                                dao.updateNotice(updateId, null, updatedContent, null);
                                break;
                            case 3:
                                System.out.print("Enter New Category: ");
                                updatedCategory = scanner.nextLine();
                                dao.updateNotice(updateId, null, null, updatedCategory);
                                break;
                            case 4:
                                System.out.print("Enter New Title: ");
                                updatedTitle = scanner.nextLine();
                                System.out.print("Enter New Content: ");
                                updatedContent = scanner.nextLine();
                                System.out.print("Enter New Category: ");
                                updatedCategory = scanner.nextLine();
                                dao.updateNotice(updateId, updatedTitle, updatedContent, updatedCategory);
                                break;
                            default:
                                System.out.println("❌ Invalid update option.");
                        }
                        System.out.println("✅ Notice updated.");
                        break;

                    case 5:
                        System.out.println("👋 Exiting... Goodbye!");
                        break;

                    default:
                        System.out.println("❗ Invalid choice. Try again.");
                }
            } catch (SQLException e) {
                System.out.println("❌ Database Error: " + e.getMessage());
            }

        } while (choice != 5);

        scanner.close();
    }
}
