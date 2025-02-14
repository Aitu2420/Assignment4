/*
continue working with your subject area using all the capabilities of the Java, such as language features for object-oriented programming, the SOLID principles, design patterns, and component-oriented principles.
- create simple REST API web-service and do request/response in JSON format using data from your DB.
*/
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.Gson; // Для работы с JSON
class Animal {
    private String name;
    private String kind;
    private String diet;
    private int age;

    public Animal(String name, String kind, String diet, int age) {
        this.name = name;
        this.kind = kind;
        this.diet = diet;
        this.age = age;
    }

    // Геттеры
    public String getName() {
        return name;
    }

    public String getKind() {
        return kind;
    }

    public String getDiet() {
        return diet;
    }

    public int getAge() {
        return age;
    }
}
class AnimalHandler implements HttpHandler { //used for working with Animals from ZOO database
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();


        if (method.equalsIgnoreCase("GET")) { //getting the request
            if (path.equals("/api/animals")) {
                response = getAllAnimalsFromDB(); //getting all animals
            }
        } else if (method.equalsIgnoreCase("POST")) {
            if (path.equals("/api/animals")) {
                Animal newAnimal = new Json().fromJson(new InputStreamReader(exchange.getRequestBody()), Animal.class); //adding the new animal
                addAnimalToDB(newAnimal);
                response = "Animal added successfully!";
            }
        } else if (method.equalsIgnoreCase("PUT")) {
            if (path.matches("/api/animals/\\d+")) {
                int id = Integer.parseInt(path.split("/")[3]); //updating the animal by ID
                Animal updatedAnimal = new Gson().fromJson(new InputStreamReader(exchange.getRequestBody()), Animal.class);
                updateAnimalInDB(id, updatedAnimal);
                response = "Animal updated successfully!";
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            if (path.matches("/api/animals/\\d+")) {
                int id = Integer.parseInt(path.split("/")[3]); //deleting the animal by ID
                deleteAnimalFromDB(id);
                response = "Animal deleted successfully!";
            }
        }
        exchange.sendResponseHeaders(statusCode, response.getBytes().length);//response to customer
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
    private String getAllAnimalsFromDB() {
        StringBuilder sb = new StringBuilder();
        String query = "SELECT * FROM animals";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ZOO", "postgres", "michigun228");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String name = rs.getString("name");
                String kind = rs.getString("kind");
                String diet = rs.getString("diet");
                int age = rs.getInt("age");
                Animal animal = new Animal(name, kind, diet, age);
                sb.append(new Gson().toJson(animal)).append("\n"); // конвертируем в JSON
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // Добавление животного в базу данных
    private void addAnimalToDB(Animal animal) {
        String query = "INSERT INTO animals (name, kind, diet, age) VALUES (?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ZOO", "postgres", "michigun228");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, animal.getName());
            stmt.setString(2, animal.getKind());
            stmt.setString(3, animal.getDiet());
            stmt.setInt(4, animal.getAge());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Обновление животного в базе данных
    private void updateAnimalInDB(int id, Animal animal) {
        String query = "UPDATE animals SET name = ?, kind = ?, diet = ?, age = ? WHERE id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ZOO", "postgres", "michigun228");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, animal.getName());
            stmt.setString(2, animal.getKind());
            stmt.setString(3, animal.getDiet());
            stmt.setInt(4, animal.getAge());
            stmt.setInt(5, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Удаление животного из базы данных
    private void deleteAnimalFromDB(int id) {
        String query = "DELETE FROM animals WHERE id = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:postgresql://localhost:5432/ZOO", "postgres", "michigun228");
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

public class Assignment4 {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/api/animals", new AnimalHandler()); // Все запросы на /api/animals обрабатываются AnimalHandler
        server.setExecutor(null); // создаем сервер с автоэксплуатацией
        server.start();
        System.out.println("Server started on http://localhost:8080");
    }
}