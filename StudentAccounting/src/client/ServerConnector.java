package client;

import model.Student;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerConnector {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Подключение к серверу установлено");
            return true;
        } catch (IOException e) {
            System.err.println("Не удалось подключиться к серверу: " + e.getMessage());
            return false;
        }
    }

    public List<Student> getAllStudents() throws IOException, ClassNotFoundException {
        out.writeObject("GET_ALL");
        out.flush();
        return (List<Student>) in.readObject();
    }

    public boolean addStudent(Student student) throws IOException, ClassNotFoundException {
        out.writeObject("ADD");
        out.writeObject(student);
        out.flush();
        String response = (String) in.readObject();
        return "SUCCESS".equals(response);
    }

    public List<Student> findStudents(String lastName) throws IOException, ClassNotFoundException {
        out.writeObject("FIND");
        out.writeObject(lastName);
        out.flush();
        return (List<Student>) in.readObject();
    }

    public boolean deleteStudent(int id) throws IOException, ClassNotFoundException {
        out.writeObject("DELETE");
        out.writeObject(id);
        out.flush();
        String response = (String) in.readObject();
        return "SUCCESS".equals(response);
    }

    public void disconnect() {
        try {
            if (out != null) {
                out.writeObject("EXIT");
                out.flush();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
                System.out.println("Соединение с сервером закрыто");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}