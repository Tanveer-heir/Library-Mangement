import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

// Book now has a genre!
class Book implements Serializable {
    private String title;
    private String author;
    private String genre;
    private boolean isAvailable;

    public Book(String title, String author, String genre) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.isAvailable = true;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public String getGenre() {
        return genre;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void borrow() {
        isAvailable = false;
    }

    public void returnBook() {
        isAvailable = true;
    }

    @Override
    public String toString() {
        return title + " by " + author + " [" + genre + "]" + (isAvailable ? " (Available)" : " (Borrowed)");
    }
}

// Loans track due/return date and tie to Book
class BorrowedBook implements Serializable {
    Book book;
    Date dueDate;
    Date returnDate;

    public BorrowedBook(Book book, Date dueDate) {
        this.book = book;
        this.dueDate = dueDate;
        this.returnDate = null;
    }

    public boolean isOverdue() {
        return (returnDate == null && dueDate.before(new Date()));
    }

    public String getStatus() {
        if (returnDate != null)
            return "Returned";
        if (isOverdue())
            return "Overdue";
        return "Borrowed";
    }
}

// User now tracks current loans and full history
class User implements Serializable {
    private String name;
    private List<BorrowedBook> currentLoans = new ArrayList<>();
    private List<BorrowedBook> history = new ArrayList<>();

    public User(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<BorrowedBook> getCurrentLoans() {
        return currentLoans;
    }

    public List<BorrowedBook> getHistory() {
        return history;
    }

    public void borrowBook(Book book, Date dueDate) {
        currentLoans.add(new BorrowedBook(book, dueDate));
        book.borrow();
    }

    public boolean returnBook(String title) {
        Iterator<BorrowedBook> it = currentLoans.iterator();
        while (it.hasNext()) {
            BorrowedBook bb = it.next();
            if (bb.book.getTitle().equalsIgnoreCase(title)) {
                bb.returnDate = new Date();
                history.add(bb);
                bb.book.returnBook();
                it.remove();
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "User: " + name + " | Borrowed Books: " + currentLoans.size();
    }
}

class Library implements Serializable {
    private List<Book> books = new ArrayList<>();
    private List<User> users = new ArrayList<>();

    public void addBook(Book book) {
        books.add(book);
    }

    public void addUser(User user) {
        users.add(user);
    }

    public Book findBook(String title) {
        for (Book b : books)
            if (b.getTitle().equalsIgnoreCase(title))
                return b;
        return null;
    }

    public User findUser(String name) {
        for (User u : users)
            if (u.getName().equalsIgnoreCase(name))
                return u;
        return null;
    }

    public List<Book> getBooks() {
        return books;
    }

    public List<User> getUsers() {
        return users;
    }

    // Remove user if no active loans
    public boolean removeUser(String userName) {
        Iterator<User> it = users.iterator();
        while (it.hasNext()) {
            User u = it.next();
            if (u.getName().equalsIgnoreCase(userName) && u.getCurrentLoans().isEmpty()) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    // CSV export/import
    public void exportBooksCSV(String filename) throws IOException {
        FileWriter fw = new FileWriter(filename);
        fw.write("Title,Author,Genre,Status\n");
        for (Book b : books)
            fw.write(String.format("%s,%s,%s,%s\n", b.getTitle(), b.getAuthor(), b.getGenre(),
                    b.isAvailable() ? "Available" : "Borrowed"));
        fw.close();
    }

    public void importBooksCSV(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        br.readLine(); // skip header!
        while ((line = br.readLine()) != null) {
            String[] arr = line.split(",");
            books.add(new Book(arr[0], arr[1], arr[2]));
        }
        br.close();
    }

    // Sorting/filtering
    public List<Book> getSortedBooksByTitle() {
        ArrayList<Book> sorted = new ArrayList<>(books);
        sorted.sort(Comparator.comparing(Book::getTitle));
        return sorted;
    }

    public List<Book> filterBooksByAvailability(boolean available) {
        ArrayList<Book> filtered = new ArrayList<>();
        for (Book b : books)
            if (b.isAvailable() == available)
                filtered.add(b);
        return filtered;
    }

    public List<Book> filterBooksByGenre(String genre) {
        ArrayList<Book> res = new ArrayList<>();
        for (Book b : books)
            if (b.getGenre().equalsIgnoreCase(genre))
                res.add(b);
        return res;
    }

    // Persistence
    public void saveData() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("library.dat"))) {
            out.writeObject(books);
            out.writeObject(users);
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    public void loadData() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("library.dat"))) {
            books = (List<Book>) in.readObject();
            users = (List<User>) in.readObject();
        } catch (Exception ignored) {
        }
    }
}

public class LibraryGUI extends JFrame {
    private Library library = new Library();

    private JTable booksTable;
    private DefaultTableModel booksModel;
    private JTextArea infoArea;
    private JComboBox<String> genreBox, filterStatusBox;
    private JTextField filterTextField;

    public LibraryGUI() {
        super("Library Management System");
        library.loadData();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);

        // MENU
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem save = new JMenuItem("Save Data");
        JMenuItem load = new JMenuItem("Load Data");
        JMenuItem export = new JMenuItem("Export Books CSV");
        JMenuItem importCsv = new JMenuItem("Import Books CSV");
        JMenuItem exit = new JMenuItem("Exit");
        fileMenu.add(save);
        fileMenu.add(load);
        fileMenu.add(export);
        fileMenu.add(importCsv);
        fileMenu.add(exit);
        menuBar.add(fileMenu);

        JMenu userMenu = new JMenu("Users");
        JMenuItem removeUser = new JMenuItem("Remove User");
        JMenuItem showUserHistory = new JMenuItem("Show Borrowing History");
        userMenu.add(removeUser);
        userMenu.add(showUserHistory);
        menuBar.add(userMenu);

        setJMenuBar(menuBar);

        // PANEL
        JPanel topPanel = new JPanel(new GridLayout(2, 4, 7, 7));
        JButton addBookBtn = new JButton("Add Book");
        JButton addUserBtn = new JButton("Register User");
        JButton borrowBtn = new JButton("Borrow Book");
        JButton returnBtn = new JButton("Return Book");
        JButton searchTitleBtn = new JButton("Search by Title");
        JButton searchAuthorBtn = new JButton("Search by Author");
        JButton sortBtn = new JButton("Sort by Title");
        genreBox = new JComboBox<>();
        genreBox.addItem("All Genres");
        genreBox.addItem("Fiction");
        genreBox.addItem("Nonfiction");
        genreBox.addItem("Poetry");
        genreBox.addItem("Comics");
        genreBox.addItem("Academic");
        filterStatusBox = new JComboBox<>(new String[] { "All", "Available", "Borrowed" });
        filterTextField = new JTextField();
        topPanel.add(addBookBtn);
        topPanel.add(addUserBtn);
        topPanel.add(borrowBtn);
        topPanel.add(returnBtn);
        topPanel.add(searchTitleBtn);
        topPanel.add(searchAuthorBtn);
        topPanel.add(sortBtn);
        topPanel.add(genreBox);
        add(topPanel, BorderLayout.NORTH);

        // BOOK TABLE
        booksModel = new DefaultTableModel(new String[] { "Title", "Author", "Genre", "Status" }, 0);
        booksTable = new JTable(booksModel);
        refreshBooksTable(library.getBooks());
        add(new JScrollPane(booksTable), BorderLayout.CENTER);

        // INFO
        JPanel bottomPanel = new JPanel(new BorderLayout());
        infoArea = new JTextArea(6, 50);
        infoArea.setEditable(false);
        bottomPanel.add(new JScrollPane(infoArea), BorderLayout.CENTER);

        // Filter row
        JPanel filterPanel = new JPanel();
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterTextField);
        filterPanel.add(filterStatusBox);
        bottomPanel.add(filterPanel, BorderLayout.NORTH);
        add(bottomPanel, BorderLayout.SOUTH);

        // BUTTON ACTIONS
        addBookBtn.addActionListener(e -> showAddBookDialog());
        addUserBtn.addActionListener(e -> showAddUserDialog());
        borrowBtn.addActionListener(e -> showBorrowDialog());
        returnBtn.addActionListener(e -> showReturnDialog());
        searchTitleBtn.addActionListener(e -> showSearchDialog(true));
        searchAuthorBtn.addActionListener(e -> showSearchDialog(false));
        sortBtn.addActionListener(e -> refreshBooksTable(library.getSortedBooksByTitle()));

        genreBox.addActionListener(e -> handleGenreFilter());
        filterStatusBox.addActionListener(e -> handleStatusFilter());
        filterTextField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                handleTextFilter();
            }
        });

        save.addActionListener(e -> {
            library.saveData();
            infoArea.append("Data saved.\n");
        });
        load.addActionListener(e -> {
            library.loadData();
            refreshBooksTable(library.getBooks());
            infoArea.append("Data loaded.\n");
        });
        exit.addActionListener(e -> {
            library.saveData();
            System.exit(0);
        });

        export.addActionListener(e -> {
            try {
                library.exportBooksCSV("books_export.csv");
                infoArea.append("Exported to books_export.csv\n");
            } catch (Exception ex) {
                infoArea.append("Export failed.\n");
            }
        });
        importCsv.addActionListener(e -> {
            try {
                library.importBooksCSV("books_export.csv");
                refreshBooksTable(library.getBooks());
                infoArea.append("Imported from books_export.csv\n");
            } catch (Exception ex) {
                infoArea.append("Import failed.\n");
            }
        });

        removeUser.addActionListener(e -> showRemoveUserDialog());
        showUserHistory.addActionListener(e -> showBorrowedHistoryDialog());

        // WINDOW CLOSE - SAVE
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                library.saveData();
            }
        });
    }

    private void handleGenreFilter() {
        String selected = (String) genreBox.getSelectedItem();
        if (selected.equals("All Genres"))
            refreshBooksTable(library.getBooks());
        else
            refreshBooksTable(library.filterBooksByGenre(selected));
    }

    private void handleStatusFilter() {
        int idx = filterStatusBox.getSelectedIndex();
        if (idx == 0)
            refreshBooksTable(library.getBooks());
        else
            refreshBooksTable(library.filterBooksByAvailability(idx == 1));
    }

    private void handleTextFilter() {
        String keyword = filterTextField.getText().toLowerCase();
        List<Book> res = new ArrayList<>();
        for (Book b : library.getBooks())
            if (b.getTitle().toLowerCase().contains(keyword) || b.getAuthor().toLowerCase().contains(keyword))
                res.add(b);
        refreshBooksTable(res);
    }

    private void showAddBookDialog() {
        JTextField title = new JTextField(), author = new JTextField(), genre = new JTextField();
        Object[] msg = { "Book Title:", title, "Author:", author, "Genre:", genre };
        int op = JOptionPane.showConfirmDialog(this, msg, "Add Book", JOptionPane.OK_CANCEL_OPTION);
        if (op == JOptionPane.OK_OPTION && !title.getText().isEmpty() && !author.getText().isEmpty()
                && !genre.getText().isEmpty()) {
            library.addBook(new Book(title.getText(), author.getText(), genre.getText()));
            refreshBooksTable(library.getBooks());
            infoArea.append("Book added: " + title.getText() + "\n");
            genreBox.addItem(genre.getText());
        }
    }

    private void showAddUserDialog() {
        String name = JOptionPane.showInputDialog(this, "Enter user name:");
        if (name != null && !name.trim().isEmpty()) {
            library.addUser(new User(name.trim()));
            infoArea.append("User added: " + name + "\n");
        }
    }

    private void showBorrowDialog() {
        String uName = JOptionPane.showInputDialog(this, "User name:");
        String bTitle = JOptionPane.showInputDialog(this, "Book title:");
        String due = JOptionPane.showInputDialog(this, "Due date (yyyy-MM-dd):");
        if (uName != null && bTitle != null && due != null) {
            try {
                User user = library.findUser(uName);
                Book book = library.findBook(bTitle);
                Date dueDate = new SimpleDateFormat("yyyy-MM-dd").parse(due);
                if (user != null && book != null && book.isAvailable()) {
                    user.borrowBook(book, dueDate);
                    refreshBooksTable(library.getBooks());
                    infoArea.append(uName + " borrowed " + bTitle + "\n");
                } else {
                    infoArea.append("Borrow failed (book/user not found or not available).\n");
                }
            } catch (Exception e) {
                infoArea.append("Invalid date.\n");
            }
        }
    }

    private void showReturnDialog() {
        String uName = JOptionPane.showInputDialog(this, "User name:");
        String bTitle = JOptionPane.showInputDialog(this, "Book title:");
        if (uName != null && bTitle != null) {
            User user = library.findUser(uName);
            if (user != null) {
                boolean success = user.returnBook(bTitle);
                refreshBooksTable(library.getBooks());
                if (success)
                    infoArea.append(uName + " returned " + bTitle + "\n");
                else
                    infoArea.append("Book was not borrowed by user.\n");
            } else
                infoArea.append("User not found.\n");
        }
    }

    private void showSearchDialog(boolean byTitle) {
        String prompt = byTitle ? "Enter title to search:" : "Enter author to search:";
        String input = JOptionPane.showInputDialog(this, prompt);
        if (input != null) {
            List<Book> result = new ArrayList<>();
            for (Book b : library.getBooks()) {
                if (byTitle && b.getTitle().toLowerCase().contains(input.toLowerCase())
                        || !byTitle && b.getAuthor().toLowerCase().contains(input.toLowerCase()))
                    result.add(b);
            }
            refreshBooksTable(result);
            infoArea.append(
                    "Search for " + (byTitle ? "title: " : "author: ") + input + ", found: " + result.size() + "\n");
        }
    }

    private void showRemoveUserDialog() {
        String user = JOptionPane.showInputDialog(this, "Enter user name to remove:");
        if (user != null) {
            boolean ok = library.removeUser(user);
            if (ok)
                infoArea.append("User removed: " + user + "\n");
            else
                infoArea.append("User not found or still has borrowed books.\n");
        }
    }

    private void showBorrowedHistoryDialog() {
        String user = JOptionPane.showInputDialog(this, "Enter user name:");
        if (user != null) {
            User u = library.findUser(user);
            if (u == null) {
                infoArea.append("No such user.\n");
                return;
            }
            StringBuilder sb = new StringBuilder("Borrow history for " + user + ":\n");
            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd");
            for (BorrowedBook bb : u.getHistory()) {
                sb.append(bb.book).append(" | Due: ").append(fmt.format(bb.dueDate));
                if (bb.returnDate != null)
                    sb.append(" | Returned: ").append(fmt.format(bb.returnDate));
                sb.append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString().isEmpty() ? "No history." : sb.toString());
        }
    }

    private void refreshBooksTable(List<Book> list) {
        booksModel.setRowCount(0);
        for (Book b : list)
            booksModel.addRow(new Object[] { b.getTitle(), b.getAuthor(), b.getGenre(),
                    b.isAvailable() ? "Available" : "Borrowed" });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryGUI().setVisible(true));
    }
}
