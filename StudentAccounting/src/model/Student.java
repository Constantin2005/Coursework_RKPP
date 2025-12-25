package model;

import java.io.Serializable;
import java.util.Objects;

public class Student implements Serializable {
    private int id;
    private String lastName;
    private String firstName;
    private String group;
    private int yearOfAdmission;

    public Student(int id, String lastName, String firstName, String group, int yearOfAdmission) {
        this.id = id;
        this.lastName = lastName;
        this.firstName = firstName;
        this.group = group;
        this.yearOfAdmission = yearOfAdmission;
    }

    // Геттеры и сеттеры
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getGroup() { return group; }
    public void setGroup(String group) { this.group = group; }

    public int getYearOfAdmission() { return yearOfAdmission; }
    public void setYearOfAdmission(int yearOfAdmission) { this.yearOfAdmission = yearOfAdmission; }

    @Override
    public String toString() {
        return String.format("%d: %s %s, Группа: %s, Год поступления: %d",
                id, lastName, firstName, group, yearOfAdmission);
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