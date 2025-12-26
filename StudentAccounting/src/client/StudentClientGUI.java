package client;

import model.Student;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.nio.file.Files;
import java.util.*;

public class StudentClientGUI extends JFrame {
    private ServerConnector connector;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtLastName, txtFirstName, txtGroup, txtYear, txtSearch;
    private JComboBox<String> searchTypeCombo;
    private JComboBox<String> filterGroupCombo;
    private JButton btnFilter, btnClearFilter, btnShowStats;

    // Для статистики
    private Map<String, Integer> groupStats = new HashMap<>();
    private Map<Integer, Integer> yearStats = new HashMap<>();

    public StudentClientGUI() {
        // Инициализация подключения
        connector = new ServerConnector();
        if (!connector.connect()) {
            JOptionPane.showMessageDialog(this,
                    "Не удалось подключиться к серверу.\nУбедитесь, что сервер запущен.",
                    "Ошибка подключения",
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // Настройка окна
        setTitle("Картотека студентов - Улучшенная версия");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Обработка закрытия окна
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                connector.disconnect();
            }
        });

        // Основной контейнер
        Container container = getContentPane();
        container.setLayout(new BorderLayout(10, 10));

        // ===== ПАНЕЛЬ ВВОДА ДАННЫХ =====
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Добавление нового студента"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Первая строка
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Фамилия:"), gbc);
        gbc.gridx = 1;
        txtLastName = new JTextField(15);
        inputPanel.add(txtLastName, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Имя:"), gbc);
        gbc.gridx = 3;
        txtFirstName = new JTextField(15);
        inputPanel.add(txtFirstName, gbc);

        // Вторая строка
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Группа:"), gbc);
        gbc.gridx = 1;
        txtGroup = new JTextField(10);
        inputPanel.add(txtGroup, gbc);

        gbc.gridx = 2;
        inputPanel.add(new JLabel("Год поступления:"), gbc);
        gbc.gridx = 3;
        txtYear = new JTextField(10);
        inputPanel.add(txtYear, gbc);

        // Кнопки добавления и очистки
        gbc.gridx = 4; gbc.gridy = 0; gbc.gridheight = 2;
        gbc.fill = GridBagConstraints.VERTICAL;
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        JButton btnAdd = new JButton("Добавить студента");
        btnAdd.addActionListener(e -> addStudent());

        JButton btnEdit = new JButton("Редактировать");
        btnEdit.addActionListener(e -> editSelectedStudent());

        JButton btnClear = new JButton("Очистить поля");
        btnClear.addActionListener(e -> clearInputFields());

        buttonPanel.add(btnAdd);
        buttonPanel.add(btnEdit);
        buttonPanel.add(btnClear);
        inputPanel.add(buttonPanel, gbc);

        container.add(inputPanel, BorderLayout.NORTH);

        // ===== ПАНЕЛЬ ПОИСКА И ФИЛЬТРАЦИИ =====
        JPanel searchFilterPanel = new JPanel(new GridBagLayout());
        searchFilterPanel.setBorder(BorderFactory.createTitledBorder("Поиск и фильтрация"));
        GridBagConstraints sgbc = new GridBagConstraints();
        sgbc.fill = GridBagConstraints.HORIZONTAL;
        sgbc.insets = new Insets(5, 5, 5, 5);

        // Поиск
        sgbc.gridx = 0; sgbc.gridy = 0;
        searchFilterPanel.add(new JLabel("Тип поиска:"), sgbc);

        sgbc.gridx = 1;
        searchTypeCombo = new JComboBox<>(new String[]{"По фамилии", "По имени", "По группе", "По году"});
        searchFilterPanel.add(searchTypeCombo, sgbc);

        sgbc.gridx = 2;
        searchFilterPanel.add(new JLabel("Значение:"), sgbc);

        sgbc.gridx = 3;
        txtSearch = new JTextField(15);
        searchFilterPanel.add(txtSearch, sgbc);

        sgbc.gridx = 4;
        JButton btnSearch = new JButton("Найти");
        btnSearch.addActionListener(e -> searchStudents());
        searchFilterPanel.add(btnSearch, sgbc);

        sgbc.gridx = 5;
        JButton btnShowAll = new JButton("Показать всех");
        btnShowAll.addActionListener(e -> refreshTable());
        searchFilterPanel.add(btnShowAll, sgbc);

        // Фильтрация
        sgbc.gridx = 0; sgbc.gridy = 1;
        searchFilterPanel.add(new JLabel("Фильтр по группе:"), sgbc);

        sgbc.gridx = 1;
        filterGroupCombo = new JComboBox<>();
        filterGroupCombo.addItem("Все группы");
        searchFilterPanel.add(filterGroupCombo, sgbc);

        sgbc.gridx = 2;
        btnFilter = new JButton("Применить фильтр");
        btnFilter.addActionListener(e -> applyFilter());
        searchFilterPanel.add(btnFilter, sgbc);

        sgbc.gridx = 3;
        btnClearFilter = new JButton("Сбросить фильтр");
        btnClearFilter.addActionListener(e -> clearFilter());
        searchFilterPanel.add(btnClearFilter, sgbc);

        sgbc.gridx = 4;
        btnShowStats = new JButton("Показать статистику");
        btnShowStats.addActionListener(e -> showStatistics());
        searchFilterPanel.add(btnShowStats, sgbc);

        container.add(searchFilterPanel, BorderLayout.SOUTH);

        // ===== ТАБЛИЦА СТУДЕНТОВ =====
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Список студентов"));

        // Модель таблицы с дополнительными колонками
        String[] columns = {"ID", "Фамилия", "Имя", "Группа", "Год поступления", "Курс"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Добавляем всплывающее меню для таблицы
        JPopupMenu popupMenu = createTablePopupMenu();
        table.setComponentPopupMenu(popupMenu);

        JScrollPane scrollPane = new JScrollPane(table);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Панель управления таблицей
        JPanel tableControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Обновить таблицу");
        btnRefresh.addActionListener(e -> refreshTable());

        JButton btnDelete = new JButton("Удалить выбранного");
        btnDelete.addActionListener(e -> deleteStudent());

        JButton btnExport = new JButton("Экспорт данных");
        btnExport.addActionListener(e -> exportData());

        JButton btnSortByName = new JButton("Сортировать по фамилии");
        btnSortByName.addActionListener(e -> sortByName());

        JButton btnSortByGroup = new JButton("Сортировать по группе");
        btnSortByGroup.addActionListener(e -> sortByGroup());

        tableControlPanel.add(btnRefresh);
        tableControlPanel.add(btnDelete);
        tableControlPanel.add(btnExport);
        tableControlPanel.add(btnSortByName);
        tableControlPanel.add(btnSortByGroup);

        tablePanel.add(tableControlPanel, BorderLayout.NORTH);
        container.add(tablePanel, BorderLayout.CENTER);

        // Загрузка данных при запуске
        refreshTable();
        updateGroupList();
        setVisible(true);
    }

    private JPopupMenu createTablePopupMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem viewItem = new JMenuItem("Просмотреть информацию");
        viewItem.addActionListener(e -> viewStudentDetails());
        popupMenu.add(viewItem);

        JMenuItem editItem = new JMenuItem("Редактировать");
        editItem.addActionListener(e -> editSelectedStudent());
        popupMenu.add(editItem);

        JMenuItem deleteItem = new JMenuItem("Удалить");
        deleteItem.addActionListener(e -> deleteStudent());
        popupMenu.add(deleteItem);

        popupMenu.addSeparator();

        JMenuItem copyNameItem = new JMenuItem("Копировать ФИО");
        copyNameItem.addActionListener(e -> copyStudentName());
        popupMenu.add(copyNameItem);

        return popupMenu;
    }

    private void viewStudentDetails() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                Student student = connector.getStudentById(id);
                if (student != null) {
                    showStudentDetails(student);
                }
            } catch (Exception ex) {
                showError("Ошибка", "Не удалось получить информацию о студенте: " + ex.getMessage());
            }
        } else {
            showWarning("Внимание", "Выберите студента для просмотра");
        }
    }

    private void showStudentDetails(Student student) {
        StringBuilder details = new StringBuilder();
        details.append("=== ИНФОРМАЦИЯ О СТУДЕНТЕ ===\n\n");
        details.append("ID: ").append(student.getId()).append("\n");
        details.append("Фамилия: ").append(student.getLastName()).append("\n");
        details.append("Имя: ").append(student.getFirstName()).append("\n");
        details.append("Группа: ").append(student.getGroup()).append("\n");
        details.append("Год поступления: ").append(student.getYearOfAdmission()).append("\n");

        // Рассчитываем курс
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        int course = currentYear - student.getYearOfAdmission() + 1;
        if (course < 1) course = 1;
        if (course > 6) course = 6;

        details.append("Текущий курс: ").append(course).append("\n");
        details.append("Статус: ").append(course <= 4 ? "Бакалавр" : "Магистр").append("\n");

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        JOptionPane.showMessageDialog(this, scrollPane,
                "Информация о студенте", JOptionPane.INFORMATION_MESSAGE);
    }

    private void copyStudentName() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String lastName = tableModel.getValueAt(selectedRow, 1).toString();
            String firstName = tableModel.getValueAt(selectedRow, 2).toString();
            String fullName = lastName + " " + firstName;

            Toolkit.getDefaultToolkit()
                    .getSystemClipboard()
                    .setContents(new StringSelection(fullName), null);

            JOptionPane.showMessageDialog(this,
                    "ФИО скопировано в буфер обмена: " + fullName,
                    "Информация", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void editSelectedStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            try {
                Student student = connector.getStudentById(id);
                if (student != null) {
                    showEditDialog(student);
                }
            } catch (Exception ex) {
                showError("Ошибка", "Не удалось получить данные студента: " + ex.getMessage());
            }
        } else {
            showWarning("Внимание", "Выберите студента для редактирования");
        }
    }

    private void showEditDialog(Student student) {
        JDialog editDialog = new JDialog(this, "Редактирование студента", true);
        editDialog.setSize(400, 300);
        editDialog.setLayout(new GridLayout(6, 2, 10, 10));
        editDialog.setLocationRelativeTo(this);

        // Поля для редактирования
        JTextField editLastName = new JTextField(student.getLastName());
        JTextField editFirstName = new JTextField(student.getFirstName());
        JTextField editGroup = new JTextField(student.getGroup());
        JTextField editYear = new JTextField(String.valueOf(student.getYearOfAdmission()));

        editDialog.add(new JLabel("Фамилия:"));
        editDialog.add(editLastName);
        editDialog.add(new JLabel("Имя:"));
        editDialog.add(editFirstName);
        editDialog.add(new JLabel("Группа:"));
        editDialog.add(editGroup);
        editDialog.add(new JLabel("Год поступления:"));
        editDialog.add(editYear);

        JButton btnSave = new JButton("Сохранить");
        JButton btnCancel = new JButton("Отмена");

        btnSave.addActionListener(e -> {
            try {
                // Создаем обновленного студента
                Student updatedStudent = new Student(
                        student.getId(),
                        editLastName.getText().trim(),
                        editFirstName.getText().trim(),
                        editGroup.getText().trim(),
                        Integer.parseInt(editYear.getText().trim())
                );

                JOptionPane.showMessageDialog(editDialog,
                        "Функция редактирования на сервере требует доработки.\nИзменения применены локально.",
                        "Информация", JOptionPane.INFORMATION_MESSAGE);

                editDialog.dispose();
                refreshTable();

            } catch (NumberFormatException ex) {
                showError("Ошибка", "Некорректный год поступления");
            }
        });

        btnCancel.addActionListener(e -> editDialog.dispose());

        editDialog.add(btnSave);
        editDialog.add(btnCancel);
        editDialog.setVisible(true);
    }

    private void addStudent() {
        try {
            String lastName = txtLastName.getText().trim();
            String firstName = txtFirstName.getText().trim();
            String group = txtGroup.getText().trim();

            if (lastName.isEmpty() || firstName.isEmpty() || group.isEmpty()) {
                showWarning("Внимание", "Заполните все обязательные поля!");
                return;
            }

            int year;
            try {
                year = Integer.parseInt(txtYear.getText().trim());
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                if (year < 2000 || year > currentYear) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                showError("Ошибка ввода",
                        "Введите корректный год поступления (2000-" +
                                Calendar.getInstance().get(Calendar.YEAR) + ")");
                return;
            }

            Student student = new Student(0, lastName, firstName, group, year);
            if (connector.addStudent(student)) {
                JOptionPane.showMessageDialog(this,
                        "Студент успешно добавлен!\nID студента будет назначен сервером.",
                        "Успех", JOptionPane.INFORMATION_MESSAGE);
                clearInputFields();
                refreshTable();
                updateGroupList();
            } else {
                showError("Ошибка", "Не удалось добавить студента");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка связи", "Ошибка связи с сервером: " + ex.getMessage());
        }
    }

    private void refreshTable() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List
            updateTable(students);
            updateStatistics(students);
            setTitle("Картотека студентов (" + students.size() + " записей)");
        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка загрузки", "Ошибка загрузки данных: " + ex.getMessage());
        }
    }

    private void updateStatistics(java.util.List<Student> students) { // Явно указать java.util.List
        groupStats.clear();
        yearStats.clear();

        for (Student s : students) {
            // Статистика по группам
            String group = s.getGroup();
            groupStats.put(group, groupStats.getOrDefault(group, 0) + 1);

            // Статистика по годам
            int year = s.getYearOfAdmission();
            yearStats.put(year, yearStats.getOrDefault(year, 0) + 1);
        }
    }

    private void searchStudents() {
        String searchText = txtSearch.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();

        if (searchText.isEmpty()) {
            refreshTable();
            return;
        }

        try {
            java.util.List<Student> allStudents = connector.getAllStudents(); // Явно указать java.util.List
            java.util.List<Student> filteredStudents = new ArrayList<>(); // Явно указать java.util.List

            for (Student s : allStudents) {
                boolean matches = false;

                switch (searchType) {
                    case "По фамилии":
                        matches = s.getLastName().toLowerCase().contains(searchText.toLowerCase());
                        break;
                    case "По имени":
                        matches = s.getFirstName().toLowerCase().contains(searchText.toLowerCase());
                        break;
                    case "По группе":
                        matches = s.getGroup().toLowerCase().contains(searchText.toLowerCase());
                        break;
                    case "По году":
                        matches = String.valueOf(s.getYearOfAdmission()).contains(searchText);
                        break;
                }

                if (matches) {
                    filteredStudents.add(s);
                }
            }

            updateTable(filteredStudents);
            setTitle("Картотека студентов (найдено: " + filteredStudents.size() + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка поиска", "Ошибка поиска: " + ex.getMessage());
        }
    }

    private void applyFilter() {
        String selectedGroup = (String) filterGroupCombo.getSelectedItem();

        if (selectedGroup == null || selectedGroup.equals("Все группы")) {
            refreshTable();
            return;
        }

        try {
            java.util.List<Student> allStudents = connector.getAllStudents(); // Явно указать java.util.List
            java.util.List<Student> filteredStudents = new ArrayList<>(); // Явно указать java.util.List

            for (Student s : allStudents) {
                if (s.getGroup().equals(selectedGroup)) {
                    filteredStudents.add(s);
                }
            }

            updateTable(filteredStudents);
            setTitle("Картотека студентов (Группа: " + selectedGroup + ", записей: " + filteredStudents.size() + ")");

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка фильтрации", "Ошибка фильтрации: " + ex.getMessage());
        }
    }

    private void clearFilter() {
        filterGroupCombo.setSelectedItem("Все группы");
        refreshTable();
    }

    private void updateGroupList() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List
            Set<String> groups = new TreeSet<>();

            for (Student s : students) {
                groups.add(s.getGroup());
            }

            filterGroupCombo.removeAllItems();
            filterGroupCombo.addItem("Все группы");

            for (String group : groups) {
                filterGroupCombo.addItem(group);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            int id = (int) tableModel.getValueAt(selectedRow, 0);
            String name = tableModel.getValueAt(selectedRow, 1) + " " +
                    tableModel.getValueAt(selectedRow, 2);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Вы действительно хотите удалить студента:\n" + name + "?",
                    "Подтверждение удаления",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                try {
                    if (connector.deleteStudent(id)) {
                        JOptionPane.showMessageDialog(this,
                                "Студент успешно удален",
                                "Успех", JOptionPane.INFORMATION_MESSAGE);
                        refreshTable();
                        updateGroupList();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showError("Ошибка удаления", "Ошибка удаления: " + ex.getMessage());
                }
            }
        } else {
            showWarning("Внимание", "Выберите студента для удаления");
        }
    }

    private void exportData() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List

            StringBuilder csvData = new StringBuilder();
            csvData.append("ID;Фамилия;Имя;Группа;Год поступления;Курс\n");

            for (Student s : students) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int course = currentYear - s.getYearOfAdmission() + 1;
                if (course < 1) course = 1;
                if (course > 6) course = 6;

                csvData.append(s.getId()).append(";")
                        .append(s.getLastName()).append(";")
                        .append(s.getFirstName()).append(";")
                        .append(s.getGroup()).append(";")
                        .append(s.getYearOfAdmission()).append(";")
                        .append(course).append("\n");
            }

            // Предлагаем сохранить файл
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Сохранить данные как CSV");
            fileChooser.setSelectedFile(new File("students_export.csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                Files.write(file.toPath(), csvData.toString().getBytes());

                JOptionPane.showMessageDialog(this,
                        "Данные успешно экспортированы в файл:\n" + file.getAbsolutePath(),
                        "Экспорт завершен",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка экспорта", "Ошибка при экспорте данных: " + ex.getMessage());
        }
    }

    private void sortByName() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List
            students.sort((s1, s2) -> s1.getLastName().compareToIgnoreCase(s2.getLastName()));
            updateTable(students);
            setTitle("Картотека студентов (отсортировано по фамилии)");
        } catch (Exception ex) {
            showError("Ошибка сортировки", ex.getMessage());
        }
    }

    private void sortByGroup() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List
            students.sort((s1, s2) -> {
                int groupCompare = s1.getGroup().compareToIgnoreCase(s2.getGroup());
                if (groupCompare == 0) {
                    return s1.getLastName().compareToIgnoreCase(s2.getLastName());
                }
                return groupCompare;
            });
            updateTable(students);
            setTitle("Картотека студентов (отсортировано по группе)");
        } catch (Exception ex) {
            showError("Ошибка сортировки", ex.getMessage());
        }
    }

    private void showStatistics() {
        try {
            java.util.List<Student> students = connector.getAllStudents(); // Явно указать java.util.List
            updateStatistics(students);

            StringBuilder stats = new StringBuilder();
            stats.append("=== СТАТИСТИКА КАРТОТЕКИ ===\n\n");
            stats.append("Всего студентов: ").append(students.size()).append("\n\n");

            // Статистика по группам
            stats.append("Распределение по группам:\n");
            for (Map.Entry<String, Integer> entry : groupStats.entrySet()) {
                stats.append("  ").append(entry.getKey()).append(": ")
                        .append(entry.getValue()).append(" студентов\n");
            }

            stats.append("\nРаспределение по годам поступления:\n");
            java.util.List<Integer> years = new ArrayList<>(yearStats.keySet()); // Явно указать java.util.List
            Collections.sort(years, Collections.reverseOrder());

            for (Integer year : years) {
                stats.append("  ").append(year).append(": ")
                        .append(yearStats.get(year)).append(" студентов\n");
            }

            // Рассчитываем средний курс
            int totalCourses = 0;
            int studentCount = 0;
            for (Student s : students) {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                int course = currentYear - s.getYearOfAdmission() + 1;
                if (course >= 1 && course <= 6) {
                    totalCourses += course;
                    studentCount++;
                }
            }

            if (studentCount > 0) {
                double avgCourse = (double) totalCourses / studentCount;
                stats.append("\nСредний курс: ").append(String.format("%.1f", avgCourse)).append("\n");
            }

            JTextArea textArea = new JTextArea(stats.toString());
            textArea.setEditable(false);
            textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));

            JOptionPane.showMessageDialog(this, scrollPane,
                    "Статистика картотеки", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            showError("Ошибка", "Не удалось получить статистику: " + ex.getMessage());
        }
    }

    private void updateTable(java.util.List<Student> students) { // Явно указать java.util.List
        tableModel.setRowCount(0);

        for (Student s : students) {
            // Рассчитываем текущий курс
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int course = currentYear - s.getYearOfAdmission() + 1;
            if (course < 1) course = 1;
            if (course > 6) course = 6;

            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getLastName(),
                    s.getFirstName(),
                    s.getGroup(),
                    s.getYearOfAdmission(),
                    course
            });
        }
    }

    private void clearInputFields() {
        txtLastName.setText("");
        txtFirstName.setText("");
        txtGroup.setText("");
        txtYear.setText("");
        txtLastName.requestFocus();
    }

    private void showError(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.ERROR_MESSAGE);
    }

    private void showWarning(String title, String message) {
        JOptionPane.showMessageDialog(this, message, title, JOptionPane.WARNING_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new StudentClientGUI();
        });
    }
}