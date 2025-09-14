import java.io.*;
import java.util.*;


class Book implements Serializable {
    private String title;
    private String author;
    private boolean isAvailable;

    public Book(String title, String author) {
        this.title = title;
        this.author = author;
        this.isAvailable = true;
    }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public boolean isAvailable() { return isAvailable; }

    public void borrow() { isAvailable = false; }
    public void returnBook() { isAvailable = true; }

    @Override
    public String toString() {
        return title + " by " + author + (isAvailable ? " (Available)" : " (Borrowed)");
    }
}


class User implements Serializable {
    private String name;
    private List<Book> borrowedBooks = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public List<Book> getBorrowedBooks() { return borrowedBooks; }

    public void borrowBook(Book book) {
        borrowedBooks.add(book);
        book.borrow();
    }

    public void returnBook(Book book) {
        borrowedBooks.remove(book);
        book.returnBook();
    }

    @Override
    public String toString() {
        return "User: " + name + " | Borrowed Books: " + borrowedBooks.size();
    }
}


class Library {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    
    public void addBook(Book book) {
        books.add(book);
    }

    
    public void addUser(User user) {
        users.add(user);
    }

    
    public void displayBooks() {
        if (books.isEmpty()) {
            System.out.println("No books available.");
            return;
        }
        for (Book b : books) {
            System.out.println(b);
        }
    }

    
    public void borrowBook(String userName, String bookTitle) {
        User user = findUser(userName);
        Book book = findBook(bookTitle);
        if (user != null && book != null && book.isAvailable()) {
            user.borrowBook(book);
            System.out.println(userName + " borrowed " + bookTitle);
        } else {
            System.out.println("Book not available or user not found.");
        }
    }

    
    public void returnBook(String userName, String bookTitle) {
        User user = findUser(userName);
        if (user != null) {
            for (Book b : user.getBorrowedBooks()) {
                if (b.getTitle().equalsIgnoreCase(bookTitle)) {
                    user.returnBook(b);
                    System.out.println(userName + " returned " + bookTitle);
                    return;
                }
            }
        }
        System.out.println("Return failed. User or book not found.");
    }

    // Helper methods
    private Book findBook(String title) {
        for (Book b : books) {
            if (b.getTitle().equalsIgnoreCase(title)) {
                return b;
            }
        }
        return null;
    }

    private User findUser(String name) {
        for (User u : users) {
            if (u.getName().equalsIgnoreCase(name)) {
                return u;
            }
        }
        return null;
    }

    // Save data to file
    public void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("library.dat"))) {
            out.writeObject(books);
            out.writeObject(users);
            System.out.println("Data saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving data.");
        }
    }

    // Load data from file
    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("library.dat"))) {
            books = (List<Book>) in.readObject();
            users = (List<User>) in.readObject();
            System.out.println("Data loaded successfully.");
        } catch (Exception e) {
            System.out.println("No previous data found.");
        }
    }
}

// Main Class
public class LibraryManagementSystem {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        Library library = new Library();
        library.loadData();

        while (true) {
            System.out.println("\n=== Library Menu ===");
            System.out.println("1. Add Book");
            System.out.println("2. Register User");
            System.out.println("3. Display Books");
            System.out.println("4. Borrow Book");
            System.out.println("5. Return Book");
            System.out.println("6. Save & Exit");
            System.out.print("Choose an option: ");
            int choice = sc.nextInt();
            sc.nextLine(); // consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter book title: ");
                    String title = sc.nextLine();
                    System.out.print("Enter author: ");
                    String author = sc.nextLine();
                    library.addBook(new Book(title, author));
                    break;

                case 2:
                    System.out.print("Enter user name: ");
                    String name = sc.nextLine();
                    library.addUser(new User(name));
                    break;

                case 3:
                    library.displayBooks();
                    break;

                case 4:
                    System.out.print("Enter user name: ");
                    String uName = sc.nextLine();
                    System.out.print("Enter book title: ");
                    String bTitle = sc.nextLine();
                    library.borrowBook(uName, bTitle);
                    break;

                case 5:
                    System.out.print("Enter user name: ");
                    String uReturn = sc.nextLine();
                    System.out.print("Enter book title: ");
                    String bReturn = sc.nextLine();
                    library.returnBook(uReturn, bReturn);
                    break;

                case 6:
                    library.saveData();
                    System.out.println("Exiting...");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice!");
            }
        }
    }
}
