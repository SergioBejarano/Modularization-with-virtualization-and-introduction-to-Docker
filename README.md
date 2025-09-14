# Modularization with Virtualization and Introduction to Docker

This project consists of building a modularized web application and deploying it on AWS using EC2 and Docker. For the implementation, a custom micro-framework was developed in Java (**SPRING IS NOT USED**), which allows:

- Serving static content (HTML, images, CSS, JS).
- Exposing REST services through custom annotations and reflection.
- Managing Inversion of Control (IoC) in a simple way for web applications.
- Handling multiple clients concurrently using a thread pool.
- Performing a graceful server shutdown to avoid losing in-progress requests.

The goal of this assignment is to demonstrate modularization, virtualization, and automated deployment of Java applications in the cloud, using modern tools such as Docker and AWS EC2, but without relying on external frameworks like Spring.

The custom framework implements:

- Automatic discovery of controllers and routes using annotations (`@RestController`, `@GetMapping`, `@RequestParam`).
- Concurrent connection handling using `ExecutorService`.
- Graceful shutdown with Java's shutdown hook.
- Deployment and execution in Docker containers, facilitating portability and scalability.

The included example application shows how to expose a POJO as a REST service and how to serve static files from the server.

# 📷 Videos

There are two videos in `/src/main/resources/webroot` inside videos.zip.

`despliegue TALLER EN CLASE.mp4`: evidence of the deployment of the application developed in class, it only has the /greeting service.

`deployment TAREA.mp4`: evidence of the functioning of the deployment of the application developed as homework, it has several services. This was developed WITHOUT SPRING.

## Verifying Java and Maven Versions

Before compiling or running this project, make sure that both **Java** and **Maven** are properly installed and configured on the system.

### Check Java version

Run the following command in the terminal:

```bash
java -version
```

Check Maven version:

```bash
mvn -version
```

## 🚀 Build the project

To compile and package the project, run:

```bash
mvn clean install
```

▶️ Running the application

```bash
java -cp target/classes co.edu.escuelaing.microspringboot.MicroSpringBoot
```

⚙️ How loadServices() works

The `loadServices()` method in the `HttpServer` class is responsible for scanning and registering available controllers:

Looks for compiled classes under co.edu.escuelaing.microspringboot.examples.

Loads each class via reflection.

Detects classes annotated with @RestController.

Iterates through their methods and finds those annotated with @GetMapping.

Registers each mapping in the services map, using the annotation value as the endpoint.

This way, the application can automatically discover REST services without requiring extra parameters at startup.

## General Explanation: How HttpServer Works

`HttpServer` is a lightweight HTTP server developed in Java, designed to serve static files and expose REST services using custom annotations. Its architecture allows for:

- **Dynamic service loading:** Upon startup, the server scans classes annotated with `@RestController` and registers methods marked with `@GetMapping`, enabling automatic route and service management.
- **Request handling:** Listens on port 35000 and accepts client connections. It analyzes the received HTTP request, determines whether it corresponds to a REST service (`/app/...`) or a static file, and responds accordingly.
- **REST services:** For routes beginning with `/app`, it invokes the corresponding method using reflection, passing the parameters received in the URL.
- **Static files:** For other routes, it searches for and serves files from the `webroot` directory, assigning the appropriate MIME type.
- **HTTP responses:** It uses the `HttpResponse` class to construct responses with status codes, headers, and body, ensuring compatibility with browsers and HTTP clients.
- **Error handling:** If the requested path does not exist, respond with a standard `404 Not Found` message.

This modular structure facilitates server extension and maintenance, allowing new REST services to be added and functionality to be improved easily.

### Note:

Implementation of the @GetMapping annotation to mark the methods that will manage REST services and the @RequestParam annotation to extract query parameters from HTTP requests are in the `co.edu.escuelaing.microspringboot.annotations` package.



# Concurrency

Explanation:
The server uses a fixed thread pool (ExecutorService) to handle multiple client connections at the same time. Each incoming connection is processed in a separate thread, allowing concurrent request handling.

# Graceful Shutdown

Explanation:
A shutdown hook is added so that when the server is stopped (e.g., with Ctrl+C), it closes the server socket and waits for all threads to finish, ensuring a clean shutdown.

## Code section (HttpServer class):

```java
public static void runServer(int port) throws IOException, URISyntaxException {
      loadServices();

      final ServerSocket serverSocket;
      try {
          serverSocket = new ServerSocket(port);
      } catch (IOException e) {
          System.err.println("Could not listen on port: " + port);
          return;
      }
      ExecutorService threadPool = Executors.newFixedThreadPool(10);
      Runtime.getRuntime().addShutdownHook(new Thread(() -> {
          System.out.println("\nShutdown hook triggered. Stopping server...");
          running = false;
          try {
              serverSocket.close();
          } catch (IOException e) {
              // Ignore
          }
          threadPool.shutdown();
          try {
              if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                  threadPool.shutdownNow();
              }
          } catch (InterruptedException e) {
              threadPool.shutdownNow();
          }
          System.out.println("Server stopped gracefully.");
      }));
      System.out.println("Server started on port " + port + ". Press Ctrl+C to stop.");
      while (running) {
          try {
              final Socket clientSocket = serverSocket.accept();
              threadPool.submit(() -> handleClient(clientSocket));
          } catch (IOException e) {
              if (running) {
                  System.err.println("Accept failed: " + e.getMessage());
              }
          }
      }
      threadPool.shutdown();
      try {
          if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
              threadPool.shutdownNow();
          }
      } catch (InterruptedException e) {
          threadPool.shutdownNow();
      }
}
```

# Unit tests

<img width="2816" height="698" alt="image" src="https://github.com/user-attachments/assets/394527da-0a04-473a-bffe-b93edfc8a4c5" />

# Prototype architecture

![Sin título (4)](https://github.com/user-attachments/assets/ffb7e227-499d-4546-bb2f-b987d5c995e5)


🏗️ Architecture Overview

The project follows a minimal micro-framework web architecture, built in Java, that mimics how larger frameworks like Spring Boot work, but in a simplified way for learning purposes.

🔹 1. Client

The client (browser, Postman, or curl) sends an HTTP request to the server.

It then receives the HTTP response generated by the application.


🔹 2. REST Services

Controllers (POJOs with annotations) expose the application’s endpoints:

GreetingController → Handles endpoints that return greetings.

MathController → Provides mathematical operations as services.

They contain the business logic of the application.


🔹 3. Server / Microframework Web

This is the core infrastructure that makes everything work.

a) httpserver package

HttpServer → Main entry point; listens for requests, delegates them to the right controller, and returns responses.

HttpRequest → Represents the incoming client request (method, path, parameters, headers).

HttpResponse → Represents the server’s reply (status code, headers, body).

b) microspringboot/annotations package

Implements a lightweight IoC container using Java reflection:

@RestController → Marks a class as a REST service.

@GetMapping → Maps a method to an HTTP GET request and a specific path.

@RequestParam → Maps query parameters from the request to method parameters.

Thanks to the loadServices() reflection mechanism, the framework can dynamically discover controllers and methods at runtime (like Spring Boot).


🔹 4. Docker Containerization

To ensure portability and reproducibility:

The Java application is packaged into a Docker image using a Dockerfile.

This image contains the compiled app and its runtime dependencies (JDK).

The image is pushed to DockerHub → central repository for easy distribution.


🔹 5. Cloud Deployment on AWS

Finally, the application is deployed to the cloud:

A VM (EC2 instance) is provisioned on AWS.

Docker is installed on the VM.

The container is pulled from DockerHub.

The app is now accessible via the EC2 public DNS on port 42000.

This allows the lightweight microframework app to behave like a real-world cloud-deployed microservice.


# Class Diagrams

<img width="2016" height="1208" alt="image" src="https://github.com/user-attachments/assets/b6327e1e-ad85-47bd-89bc-30b91067e26b" />

<img width="1030" height="517" alt="image" src="https://github.com/user-attachments/assets/9edb675f-1d21-4448-a9aa-4a6c9fd12267" />

<img width="908" height="512" alt="image" src="https://github.com/user-attachments/assets/fe56ec43-7669-4b14-98ff-65c91d3e5326" />

<img width="1071" height="521" alt="image" src="https://github.com/user-attachments/assets/bf5330c5-face-4d63-b998-bd86284e0bf2" />


# Modularization workshop with virtualization and introduction to Docker

### Concurrency and Graceful Shutdown in the Framework

The framework implements a concurrent HTTP server using a thread pool (`ExecutorService`). Each time a new connection arrives, it is handled by a thread from the pool, allowing multiple clients to be served simultaneously and improving performance under load.

For graceful termination, a Java shutdown hook (`Runtime.getRuntime().addShutdownHook`) is used. This hook is automatically triggered when the process receives a termination signal (for example, when Ctrl+C is pressed). The hook stops the main server loop, closes the `ServerSocket`, and waits for all threads to finish servicing active connections before terminating the process. This ensures that no requests in progress are lost and that resources.

### Task development

In the root of the project, create a file named Dockerfile with the following content:

```
FROM openjdk:17

WORKDIR /usrapp/bin

ENV PORT=6000

COPY /target/classes /usrapp/bin/classes
COPY /target/dependency /usrapp/bin/dependency

CMD ["java","-cp","./classes:./dependency/*","co.edu.escuelaing.microspringboot.MicroSpringBoot"]
```

Using the Docker command line tool, build the image:
<img width="1600" height="705" alt="image" src="https://github.com/user-attachments/assets/1cb1a42f-fea9-4abb-925d-048419dc683e" />

From the image created, create three instances of a docker container independent of the console (option “-d”) and with port 6000 linked to a physical port on the machine (option -p):

<img width="1600" height="295" alt="image" src="https://github.com/user-attachments/assets/ab02329a-01a4-4a39-be6a-3b3d8488bb71" />

Make sure the container is running
<img width="1600" height="208" alt="image" src="https://github.com/user-attachments/assets/44089a11-326e-4247-a563-dc6ac87c8242" />

Access via browser:
<img width="1600" height="895" alt="image" src="https://github.com/user-attachments/assets/a934f9b2-9b05-4367-982e-6132ef84d083" />

Use docker-compose to automatically generate a docker configuration:

```
services:
  web:
    build:
        context: .
        dockerfile: Dockerfile
    container_name: web
    ports:
        - "8087:6000"
  db:
    image: mongo:8-noble
    container_name: db
    volumes:
      - mongodb:/data/db
      - mongodb_config:/data/configdb
    ports:
        - 27017:27017
    command: mongod

volumes:
  mongodb:
  mongodb_config:
```

Run the docker compose:
<img width="1600" height="737" alt="image" src="https://github.com/user-attachments/assets/fa444f65-5497-4405-aa62-ac399c578f38" />

Verify that the services were created:
<img width="1600" height="267" alt="image" src="https://github.com/user-attachments/assets/c41959d5-c27d-4a87-805f-8079f512bad9" />

in the containers section from the Docker Desktop dashboard:
<img width="1600" height="952" alt="image" src="https://github.com/user-attachments/assets/2aba9554-37d9-4bf3-a8d2-99bd7db0b9bf" />

Create the repository:
<img width="1600" height="848" alt="image" src="https://github.com/user-attachments/assets/6fee37d1-4dc4-4616-8635-574d30f7cd9e" />

In the local Docker engine, create a reference to the image with the name of the repository where it will be uploaded:
<img width="1600" height="430" alt="image" src="https://github.com/user-attachments/assets/47723634-bc0c-4eaf-ad0b-800efbe8360c" />

Login:

<img width="1600" height="268" alt="image" src="https://github.com/user-attachments/assets/a2ee9304-772f-41ca-9e69-12e48495ca71" />

Push the image to the repository on DockerHub

```
docker push sergiobejarano/sergiotarealab04
```

In the Tags tab of the repository on Dockerhub:
<img width="1600" height="850" alt="image" src="https://github.com/user-attachments/assets/43def2d6-4cb5-4fbc-9ead-08e543c131e0" />

The EC2 instance is now created in AWS.
<img width="2879" height="1461" alt="Captura de pantalla 2025-09-08 223652" src="https://github.com/user-attachments/assets/d71c54e5-f5a0-4d44-9c1e-f116962d61b4" />

Access the virtual machine.

Install Docker:

```
sudo yum update -y
sudo yum install docker
```

Start the Docker service:

```
sudo service docker start
```

Configure the user in the Docker group:

```
sudo usermod -a -G docker ec2-user
```

From the image created in Dockerhub, create an instance of a docker container independent of the console (option “-d”) and with port 6000 linked to a physical port on your machine (option -p):

<img width="1600" height="676" alt="image" src="https://github.com/user-attachments/assets/4f803122-3024-4803-a96c-8b80c8e9dafe" />

Accessing the web application deployed in a Docker container on AWS:

<img width="2879" height="1700" alt="image" src="https://github.com/user-attachments/assets/5a2d01e9-8482-4f74-b695-c6a6aabc89ed" />

## Tests

<img width="1455" height="465" alt="image" src="https://github.com/user-attachments/assets/dcc2679e-23c8-41aa-a63b-c6f99f58340c" />

<img width="1858" height="434" alt="image" src="https://github.com/user-attachments/assets/471f98a3-44ec-4090-9bd7-902364fece57" />

<img width="1572" height="421" alt="image" src="https://github.com/user-attachments/assets/7c2a24ad-c1af-4a34-a180-1453f09d7329" />

## Author

Sergio Bejarano
