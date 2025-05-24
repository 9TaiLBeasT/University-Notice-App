import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.sql.SQLException;

public class Main {
    // 🔁 Replace only the main method part with this change:
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

            switch (choice) {
                case 1:
                    try {
                        System.out.print("Enter Title: ");
                        String title = scanner.nextLine();

                        System.out.print("Enter Content: ");
                        String content = scanner.nextLine();

                        System.out.print("Enter Category (General/Faculty/Student): ");
                        String category = scanner.nextLine();

                        System.out.print("Is this an event notice? (yes/no): ");
                        boolean isEvent = scanner.nextLine().equalsIgnoreCase("yes");

                        Timestamp eventTimestamp = null;
                        if (isEvent) {
                            System.out.print("Enter Event DateTime (e.g., 2025-05-02T14:00): ");
                            String eventDateTimeStr = scanner.nextLine();
                            try {
                                LocalDateTime eventDateTime = LocalDateTime.parse(eventDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                eventTimestamp = Timestamp.valueOf(eventDateTime);
                            } catch (Exception e) {
                                System.out.println("❌ Invalid format. Use: yyyy-MM-ddTHH:mm (e.g. 2025-05-02T14:00)");
                                break;
                            }
                        }

                        Notice newNotice = new Notice(title, content, category);
                        newNotice.setEvent(isEvent);
                        newNotice.setEventTime(eventTimestamp);

                        dao.addNotice(newNotice);

                        try {
                            FCMSender.sendPushNotification(title, content);
                            System.out.println("📲 Push notification sent.");
                        } catch (Exception e) {
                            System.out.println("❌ Failed to send push notification: " + e.getMessage());
                        }

                        System.out.println("✅ Notice added successfully.");
                    } catch (Exception e) {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                    break;

                case 2:
                    try {
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
                                if (n.isEvent()) {
                                    System.out.println("📅 Event DateTime: " + n.getEventTime());
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                    break;

                case 3:
                    try {
                        System.out.print("Enter Notice ID to delete: ");
                        int deleteId = scanner.nextInt();
                        dao.deleteNotice(deleteId);
                        System.out.println("🗑️ Notice deleted.");
                    } catch (Exception e) {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                    break;

                case 4:
                    try {
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
                        scanner.nextLine();

                        System.out.println("What do you want to update?");
                        System.out.println("1. Title");
                        System.out.println("2. Content");
                        System.out.println("3. Category");
                        System.out.println("4. All");
                        System.out.print("Enter your choice: ");
                        int updateChoice = scanner.nextInt();
                        scanner.nextLine();

                        String updatedTitle = null;
                        String updatedContent = null;
                        String updatedCategory = null;

                        // Prompt for event info
                        System.out.print("Is this still an event notice? (yes/no): ");
                        boolean isEvent = scanner.nextLine().equalsIgnoreCase("yes");

                        Timestamp eventTime = null;
                        if (isEvent) {
                            System.out.print("Enter new Event DateTime (e.g., 2025-05-02T14:00): ");
                            String eventDateTimeStr = scanner.nextLine();
                            try {
                                LocalDateTime eventDateTime = LocalDateTime.parse(eventDateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                                eventTime = Timestamp.valueOf(eventDateTime);
                            } catch (Exception e) {
                                System.out.println("❌ Invalid format. Use: yyyy-MM-ddTHH:mm (e.g. 2025-05-02T14:00)");
                                break;
                            }
                        }

                        switch (updateChoice) {
                            case 1:
                                System.out.print("Enter New Title: ");
                                updatedTitle = scanner.nextLine();
                                break;
                            case 2:
                                System.out.print("Enter New Content: ");
                                updatedContent = scanner.nextLine();
                                break;
                            case 3:
                                System.out.print("Enter New Category: ");
                                updatedCategory = scanner.nextLine();
                                break;
                            case 4:
                                System.out.print("Enter New Title: ");
                                updatedTitle = scanner.nextLine();
                                System.out.print("Enter New Content: ");
                                updatedContent = scanner.nextLine();
                                System.out.print("Enter New Category: ");
                                updatedCategory = scanner.nextLine();
                                break;
                            default:
                                System.out.println("❌ Invalid update option.");
                                break;
                        }

                        dao.updateNotice(updateId, updatedTitle, updatedContent, updatedCategory, isEvent, eventTime);
                        System.out.println("✅ Notice updated.");

                    } catch (Exception e) {
                        System.out.println("❌ Error: " + e.getMessage());
                    }
                    break;

                case 5:
                    System.out.println("👋 Exiting... Goodbye!");
                    break;

                default:
                    System.out.println("❗ Invalid choice. Try again.");
            }
        } while (choice != 5);

        scanner.close();
    }

}
