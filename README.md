# Library Management System (Java Swing GUI)

A comprehensive desktop library management application built in Java using Swing. Easily manage books, users, borrowing, returns, history, and more in an interactive graphical interface.

## Features

- **Add/View/Edit Books**
  - Store title, author, and genre/category
  - Track book availability (available/borrowed)
- **User Management**
  - Register users and remove users
  - View user's current loans and full borrowing history
- **Loan Management**
  - Borrow and return books with due dates  
  - Automatic overdue detection and labeling
- **Flexible Search & Filtering**
  - Search by book title, author, or genre
  - Filter by status (available/borrowed) or keyword
  - Sort books by title
- **Data Export & Import**
  - Export all book records as a CSV file (compatible with Excel)
  - Import book records from a CSV file
- **Persistent Storage**
  - Save and load all data (books and users) between sessions using file serialization
- **Simple, User-Friendly GUI**
  - Point-and-click menu for all operations
  - Info window for feedback and alerts
  - Table displays for books and results

## How to Run

1. **Clone or Download the Project**
2. **Compile:**
    ```
    javac LibraryManagementSystem.java
    ```
3. **Run:**
    ```
    java LibraryManagementSystem
    ```
4. **Usage:**
   - Use the menu items and buttons to add books, register users, manage loans, search, and export.
   - Data is saved to `library.dat` on exit and can be manually saved/loaded.
   - Exported CSV files will be saved as `books_export.csv` in the working directory.

## CSV Format

Exported books CSV headers:


## Dependencies

- Java SE 8 or newer
- No external libraries required (pure Java + Swing)

## Author

Tanveer Singh

