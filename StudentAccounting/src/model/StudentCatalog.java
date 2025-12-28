package model;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StudentCatalog {
    private List<Student> students;
    private static final String DATA_FILE = "students.dat";
    private int lastId;

    public StudentCatalog() {
        students = new ArrayList<>();
        loadFromFile();
        lastId = students.stream().mapToInt(Student::getId).max().orElse(0);
    }

    // Основные бизнес-операции
    public synchronized boolean addStudent(Student student) {
        student.setId(++lastId);
        boolean added = students.add(student);
        if (added) {
            saveToFile();
        }
        return added;
    }

    public synchronized List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public synchronized List<Student> findStudentsByLastName(String lastName) {
        List<Student> result = new ArrayList<>();
        for (Student s : students) {
            if (s.getLastName().toLowerCase().contains(lastName.toLowerCase())) {
                result.add(s);
            }
        }
        return result;
    }

    public synchronized boolean deleteStudent(int id) {
        boolean removed = students.removeIf(s -> s.getId() == id);
        if (removed) {
            saveToFile();
        }
        return removed;
    }

    // НОВЫЙ МЕТОД: поиск студента по ID
    public synchronized Student getStudentById(int id) {
        for (Student s : students) {
            if (s.getId() == id) {
                return s;
            }
        }
        return null;
    }

    // Приватные методы для работы с файлом
    private void saveToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(students);
        } catch (IOException e) {
            System.err.println("Ошибка сохранения данных: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                students = (List<Student>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Ошибка загрузки данных: " + e.getMessage());
                students = new ArrayList<>();
            }
        }
    }
}