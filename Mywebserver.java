import java.io.*;
import java.net.*;

public class Mywebserver {
    public static void main(String[] args) {
        // Start the server in a separate thread
        new Thread(() -> startServer()).start();
        System.out.println("Server thread started."); // Confirm the server thread is initiated

        // Add a small delay to ensure the server starts before the client connects
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(5635, 0, InetAddress.getLocalHost())) {
            System.out.println("Server is listening on port 5635...");
            System.out.println("IP Address: " + InetAddress.getLocalHost().getHostAddress() + ":5635/mypage.htm");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                        OutputStream out = clientSocket.getOutputStream()) {

                    String inputLine;
                    StringBuilder requestBuilder = new StringBuilder();
                    String requestedFile = null;

                    while ((inputLine = in.readLine()) != null) {
                        System.out.println("Received line: " + inputLine);
                        requestBuilder.append(inputLine).append("\n");
                        if (inputLine.startsWith("GET")) {
                            requestedFile = inputLine.split(" ")[1].substring(1);
                            System.out.println("Requested file: " + requestedFile);
                        }
                        if (inputLine.isEmpty()) {
                            break; // End of headers
                        }
                    }
                    System.out.println("Complete request:\n" + requestBuilder.toString());

                    // If no file was requested, set a default
                    if (requestedFile == null || requestedFile.isEmpty()) {
                        requestedFile = "index.html"; // Default to index.html
                    }

                    // Read the requested file
                    File file = new File(requestedFile);
                    System.out.println("Looking for file at: " + file.getAbsolutePath());
                    if (file.exists() && !file.isDirectory()) {
                        System.out.println("File exists: " + requestedFile);
                        String contentType = getContentType(requestedFile);
                        String httpResponseHeader = "HTTP/1.0 200 OK\r\n" +
                                "Content-Type: " + contentType + "\r\n" +
                                "Content-Length: " + file.length() + "\r\n" +
                                "\r\n";

                        out.write(httpResponseHeader.getBytes());
                        out.flush(); // Ensure headers are sent
                        System.out.println("Response headers sent.");

                        // Read and send the file content
                        try (FileInputStream fileInputStream = new FileInputStream(file)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                                out.write(buffer, 0, bytesRead);
                            }
                            out.flush(); // Ensure all file data is sent
                            System.out.println("File content sent.");
                        }
                    } else {
                        System.out.println("File not found: " + requestedFile);
                        String httpResponse = "HTTP/1.0 404 Not Found\r\n" +
                                "Content-Type: text/html\r\n" +
                                "\r\n" +
                                "<html><body><h1>404 Not Found</h1></body></html>";
                        out.write(httpResponse.getBytes());
                        out.flush(); // Ensure response is sent
                        System.out.println("404 response sent.");
                    }
                } catch (IOException e) {
                    System.out.println("IOException while handling client: " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    clientSocket.close(); // Always close the client socket
                }
            }
        } catch (IOException e) {
            System.out.println("IOException in server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getContentType(String filePath) {
        if (filePath.endsWith(".htm") || filePath.endsWith(".html")) {
            return "text/html";
        } else if (filePath.endsWith(".gif")) {
            return "image/gif";
        } else if (filePath.endsWith(".png")) {
            return "image/png";
        } else if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filePath.endsWith(".css")) {
            return "text/css";
        } else if (filePath.endsWith(".js")) {
            return "application/javascript";
        } else {
            return "application/octet-stream"; // Default type
        }
    }
}