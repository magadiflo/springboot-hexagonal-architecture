# [Implementación de arquitecturas hexagonales](https://www.youtube.com/watch?v=CycNkSXfXy8&t=1021s)

Tutorial tomado del canal de **youtube NullSafe Architect**

---

## Visión General de la Arquitectura Hexagonal

![01.overview.png](./assets/01.overview.png)

## Dependencias

````xml
<!--Spring Boot 3.2.4-->
<!--Java 21-->
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
````

## Configuraciones del application.yml

En esta aplicación no hacemos uso de `jpa/hibernate`, símplemente hacemos uso de `jdbc`.

````yml
server:
  port: 8080
  error:
    include-message: always

spring:
  application:
    name: springboot-hexagonal-architecture

  datasource:
    url: jdbc:postgresql://localhost:5432/db_hexagonal_architecture
    username: postgres
    password: magadiflo

  sql:
    init:
      mode: always

logging:
  level:
    org.springframework.test.context.jdbc: DEBUG
    org.springframework.jdbc.datasource.init: DEBUG
````

## Schema.sql

El script `schema.sql` será creada en el directorio `/resoruces`. En este script definimos las tablas de base de
datos que trabajaremos en este proyecto:

````sql
CREATE TABLE IF NOT EXISTS customers(
    id TEXT NOT NULL PRIMARY KEY,
    name TEXT NOT NULL,
    country TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS orders(
    id TEXT NOT NULL PRIMARY KEY,
    customer_id TEXT NOT NULL,
    total NUMERIC(10,2) NOT NULL
);
````

## Docker Compose

Vamos a trabajar con la base de datos `postgres` que estará siendo ejecutada en un contendor de `docker`. Para eso,
vamos a trabajar con `docker compose`, que nos permitirá ejecutar de manera sencilla el servicio de nuestro contendor
de base de datos:

````yml
services:
  postgres:
    image: postgres:15.2-alpine
    container_name: postgres
    environment:
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: magadiflo
      POSTGRES_DB: db_hexagonal_architecture
    ports:
      - 5432:5432
    restart: unless-stopped
````

Recordar que para levantar el contendor con `docker compose` tenemos que ejecutar el siguiente comando en la raíz de
este proyecto, dado que el archivo `compose.yml` se encuentra en ese lugar:

````bash
M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\24.nullsafe_architect\springboot-hexagonal-architecture (main -> origin)
$ docker compose up -d
[+] Building 0.0s (0/0)                                                                                                                                                                                                        docker:default [+] Running 2/2
 ✔ Network springboot-hexagonal-architecture_default  Created                                                                                                                                                                            0.1s  ✔ Container postgres                                 Started                                                                                                                                                                            0.1s

M:\PROGRAMACION\DESARROLLO_JAVA_SPRING\02.youtube\24.nullsafe_architect\springboot-hexagonal-architecture (main -> origin)
$ docker container ls -a
CONTAINER ID   IMAGE                  COMMAND                  CREATED         STATUS         PORTS                    NAMES
6c624a898553   postgres:15.2-alpine   "docker-entrypoint.s…"   7 seconds ago   Up 5 seconds   0.0.0.0:5432->5432/tcp   postgres
````

