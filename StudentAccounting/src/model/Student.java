package model;

import java.io.Serializable;
import java.util.Objects;

public class Student implements Serializable {
    // ФИКСИРОВАННЫЙ serialVersionUID для совместимости версий
    private static final long serialVersionUID = 1L;

    private int id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String group;
    private int yearOfAdmission;

    // Конструктор без отчества
    public Student(int id, String lastName, String firstName, String group, int yearOfAdmission) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = "";
        this.group = group;
        this.yearOfAdmission = yearOfAdmission;
    }

    // Конструктор с отчеством
    public Student(int id, String lastName, String firstName, String middleName,
                   String group, int yearOfAdmission) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.group = group;
        this.yearOfAdmission = yearOfAdmission;
    }

    // Геттеры и сеттеры остаются без изменений
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() {
        return middleName != null ? middleName : "";
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public int getYearOfAdmission() { return yearOfAdmission; }
    public void setYearOfAdmission(int yearOfAdmission) { this.yearOfAdmission = yearOfAdmission; }

    public String getFullName() {
        StringBuilder fullName = new StringBuilder(lastName);
        if (!firstName.isEmpty()) {
            fullName.append(" ").append(firstName);
        }
        if (middleName != null && !middleName.isEmpty()) {
            fullName.append(" ").append(middleName);
        }
        return fullName.toString();
    }

    @Override
    public String toString() {
        return String.format("%d: %s %s %s, Группа: %s, Год поступления: %d",
                id, lastName, firstName,
                (middleName != null && !middleName.isEmpty() ? middleName : ""),
                group, yearOfAdmission);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}