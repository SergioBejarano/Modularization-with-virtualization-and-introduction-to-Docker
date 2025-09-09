# MicroSpringBoot

### Minimal Web Server & IoC Framework in Java

A lightweight prototype of a **Java-based web server** (similar to Apache) that can:

- Serve static content (HTML and PNG images).  
- Provide a simple **IoC (Inversion of Control) framework** to build web applications from POJOs using annotations.  
- Demonstrate **Java reflection capabilities** by dynamically discovering and loading beans.  
- Handle multiple (non-concurrent) requests.  

As part of the prototype, an example web application is included to show how a POJO can be exposed as a REST service.



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
Running on a specific commit

In earlier versions, it was necessary to explicitly specify the controller when starting the application:

```bash
java -cp target/classes co.edu.escuelaing.microspringboot.MicroSpringBoot co.edu.escuelaing.microspringboot.examples.GreetingController Starting MicroSpringBoot
```

Running on the latest commit

With the current implementation, specifying the controller is no longer required.
The framework automatically scans and registers all classes annotated with `@RestController` in the `examples` package.

```bash
java -cp target/classes co.edu.escuelaing.microspringboot.MicroSpringBoot 
```



⚙️ How loadServices() works

The `loadServices()` method in the `HttpServer` class  is responsible for scanning and registering available controllers:

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


# Unit tests

<img width="2816" height="698" alt="image" src="https://github.com/user-attachments/assets/394527da-0a04-473a-bffe-b93edfc8a4c5" />


# Testing...

<img width="2879" height="1316" alt="image" src="https://github.com/user-attachments/assets/8944adc7-9687-46ed-8be4-5c181a0ecacd" />

Go to `localhost:35000`:

Console:

<img width="2879" height="1682" alt="image" src="https://github.com/user-attachments/assets/12cb9f40-7661-4ffd-ba72-e63c4aa8064b" />


Request: /app/greeting

<img width="2879" height="1620" alt="image" src="https://github.com/user-attachments/assets/0e40b7f9-2c9b-4dd2-ac75-2033595a3793" />

Request: /app/greeting?name=Sergio

<img width="2879" height="1634" alt="image" src="https://github.com/user-attachments/assets/08510387-dd69-4f4b-8f4d-c3972ef67ac6" />

<img width="934" height="213" alt="image" src="https://github.com/user-attachments/assets/cfa69e70-377f-427b-b6fe-3403125185a5" />

Request: /app/factors?number=90

<img width="2879" height="1624" alt="image" src="https://github.com/user-attachments/assets/ccaa39c0-3632-407c-a875-f285f5ce8eb3" />

<img width="974" height="280" alt="image" src="https://github.com/user-attachments/assets/01739b54-7056-4e58-823c-0425d1adc930" />


Request: /app/square?number=12

<img width="2879" height="1640" alt="image" src="https://github.com/user-attachments/assets/79dade56-5661-4951-abe2-f354ccc1acbf" />

<img width="896" height="214" alt="image" src="https://github.com/user-attachments/assets/f61cd30e-5d2e-4181-9e05-8b43071c10db" />



# Prototype architecture
![Sin título (3)](https://github.com/user-attachments/assets/8f32e7bd-c195-438c-88ed-69fe0bdd2204)

🏗️ Architecture Overview

The project follows a minimal micro-framework web architecture, built in Java, that mimics how larger frameworks like Spring Boot work, but in a simplified way for learning purposes.

🔹 1. Client

The client (e.g., browser, Postman, or curl) sends an HTTP request to the server.

It then receives the HTTP response generated by the application.

🔹 2. REST Services

These are the application’s controllers (POJOs with annotations) that expose endpoints:

GreetingController → Handles endpoints that return greetings.

MathController → Provides mathematical operations as services.

They define the business logic of the application.

🔹 3. Server / Microframework Web

This is the core infrastructure that makes everything work. It has two main parts:

a) httpserver package

HttpServer: Main entry point; listens for requests, delegates them to the right controller, and returns responses.

HttpRequest: Represents the incoming client request (method, path, parameters, headers).

HttpResponse: Represents the server’s reply (status code, headers, body).

b) microspringboot/annotations package

Implements a very lightweight IoC container using Java reflection:

@RestController → Marks a class as a REST service.

@GetMapping → Maps a method to an HTTP GET request and a specific path.

@RequestParam → Maps query parameters from the request to method parameters.

This allows the framework to dynamically discover controllers and methods at runtime (like Spring Boot), thanks to the loadServices() reflection mechanism.

🔹 4. Request–Response Flow

Client sends an HTTP request.

HttpServer receives it and builds an HttpRequest object.

The server uses reflection + annotations to route the request to the appropriate controller method (e.g., GreetingController.hello()).

The method executes business logic and produces an output.

The server wraps the result in an HttpResponse.

The Client receives the response.

# Modularization workshop with virtualization and introduction to Docker


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

Access via your browser
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

<img width="2879" height="1461" alt="Captura de pantalla 2025-09-08 223652" src="https://github.com/user-attachments/assets/d71c54e5-f5a0-4d44-9c1e-f116962d61b4" />


<img width="1600" height="676" alt="image" src="https://github.com/user-attachments/assets/4f803122-3024-4803-a96c-8b80c8e9dafe" />




## Author

Sergio Bejarano

