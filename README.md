# [Implementación de arquitecturas hexagonales](https://www.youtube.com/watch?v=CycNkSXfXy8&t=1021s)

Tutorial tomado del canal de **youtube NullSafe Architect**

---

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

---

# DOMAIN

---

Para este ejemplo crearemos dos entidades de dominio `Customer` y `Order`:

````java

@Getter
@Setter
@Builder
public class Customer {
    private String id;
    private String name;
    private String country;
}
````

````java

@Getter
@Setter
@Builder
public class Order {
    private String id;
    private String customerId;
    private BigDecimal total;
}
````

Ahora crearemos algunas interfaces que nos ayudarán con la implementación de esta arquitectura:

````java
public interface EntityRepository<T, ID> {
    Iterable<T> findAll();

    Optional<T> findById(ID primaryKey);

    T save(T t);

    void deleteById(ID primaryKey);
}
````

````java
public interface OrderInputPort {
    Order createOrder(String customerId, BigDecimal total);
}
````

````java
public interface CustomerInputPort {
    List<Customer> getAllCustomers();

    Customer getCustomerById(String customerId);

    Customer createCustomer(String name, String country);

    void deleteCustomerById(String customerId);
}
````

---

# APPLICATION

---

En esta capa implementaremos los casos de uso del negocio, básicamente los relacionados al `customer` y al `order`:

````java

@RequiredArgsConstructor
@Service
public class CustomerUserCase implements CustomerInputPort {

    private final EntityRepository<Customer, String> customerRepository;

    @Override
    public List<Customer> getAllCustomers() {
        return (List<Customer>) this.customerRepository.findAll();
    }

    @Override
    public Customer getCustomerById(String customerId) {
        return this.customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer no encontrado"));
    }

    @Override
    public Customer createCustomer(String name, String country) {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .country(country)
                .build();
        return this.customerRepository.save(customer);
    }

    @Override
    public void deleteCustomerById(String customerId) {
        this.customerRepository.deleteById(customerId);
    }
}
````

````java

@RequiredArgsConstructor
@Service
public class OrderUseCase implements OrderInputPort {

    private final EntityRepository<Order, String> orderRepository;

    @Override
    public Order createOrder(String customerId, BigDecimal total) {
        Order order = Order.builder()
                .id(UUID.randomUUID().toString())
                .customerId(customerId)
                .total(total)
                .build();
        return this.orderRepository.save(order);
    }
}
````

---

# INFRASTRUCTURE

---

## Output - Adapter

La interfaz (puerto) creado anteriormente necesita su adaptador. En este apartado crearemos dos adaptadores, uno para el
modelo de dominio `Customer` y otro para el `Order`:

````java

@RequiredArgsConstructor
@Repository
public class CustomerRepository implements EntityRepository<Customer, String> {

    private final JdbcClient jdbcClient;

    @Override
    public Iterable<Customer> findAll() {
        return this.jdbcClient.sql("SELECT id, name, country FROM customers")
                .query(Customer.class)
                .list();
    }

    @Override
    public Optional<Customer> findById(String primaryKey) {
        return this.jdbcClient.sql("""
                        SELECT c.id, c.name, c.country
                        FROM customers AS c
                        WHERE c.id = :id
                        """)
                .param("id", primaryKey)
                .query(Customer.class)
                .optional();
    }

    @Override
    public Customer save(Customer customer) {
        this.jdbcClient.sql("""
                        INSERT INTO customers(id, name, country)
                        VALUES(:id, :name, :country)
                        """)
                .param("id", customer.getId(), Types.VARCHAR)
                .param("name", customer.getName(), Types.VARCHAR)
                .param("country", customer.getCountry(), Types.VARCHAR)
                .update();
        return customer;
    }

    @Override
    public void deleteById(String primaryKey) {
        this.jdbcClient.sql("DELETE FROM customers WHERE id = :id")
                .param("id", primaryKey)
                .update();
    }
}
````

````java

@RequiredArgsConstructor
@Repository
public class OrderRepository implements EntityRepository<Order, String> {

    private final JdbcClient jdbcClient;

    @Override
    public Iterable<Order> findAll() {
        return this.jdbcClient.sql("SELECT id, customer_id, total FROM orders")
                .query(new OrderRowMapper())
                .list();
    }

    @Override
    public Optional<Order> findById(String primaryKey) {
        return this.jdbcClient.sql("""
                        SELECT o.id, o.customer_id, o.total
                        FROM orders AS o
                        WHERE o.id = :id
                        """)
                .param("id", primaryKey)
                .query(new OrderRowMapper())
                .optional();
    }

    @Override
    public Order save(Order order) {
        this.jdbcClient.sql("""
                        INSERT INTO orders(id, customer_id, total)
                        VALUES(:id, :customerId, :total)
                        """)
                .param("id", order.getId())
                .param("customerId", order.getCustomerId())
                .param("total", order.getTotal())
                .update();
        return order;
    }

    @Override
    public void deleteById(String primaryKey) {
        this.jdbcClient.sql("DELETE FROM orders WHERE id = :id")
                .param("id", primaryKey)
                .update();
    }

    private static class OrderRowMapper implements RowMapper<Order> {

        @Override
        public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Order.builder()
                    .id(rs.getString("id"))
                    .customerId(rs.getString("customer_id"))
                    .total(rs.getBigDecimal("total"))
                    .build();
        }
    }
}
````

**NOTA**

> En el tutorial original se crea un único adaptador, de manera genérica, pero en mi caso crearé un adaptador para
> cada modelo de dominio.

## Input - Adapter

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/orders")
public class OrderAPI {

    private final OrderInputPort orderInputPort;

    @PostMapping
    public ResponseEntity<Order> createOrder(@RequestParam String customerId, @RequestParam BigDecimal total) {
        Order orderDB = this.orderInputPort.createOrder(customerId, total);
        URI uri = URI.create("/api/v1/orders/" + orderDB.getId());
        return ResponseEntity.created(uri).body(orderDB);
    }
}
````

````java

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/v1/customers")
public class CustomerAPI {

    private final CustomerInputPort customerInputPort;

    @GetMapping
    public ResponseEntity<List<Customer>> findAllCustomers() {
        return ResponseEntity.ok(this.customerInputPort.getAllCustomers());
    }

    @GetMapping(path = "/{customerId}")
    public ResponseEntity<Customer> getCustomer(@PathVariable String customerId) {
        return ResponseEntity.ok(this.customerInputPort.getCustomerById(customerId));
    }

    @PostMapping
    public ResponseEntity<Customer> saveCustomer(@RequestParam String name, @RequestParam String country) {
        Customer customerDB = this.customerInputPort.createCustomer(name, country);
        URI uri = URI.create("/api/v1/customers/" + customerDB.getId());
        return ResponseEntity.created(uri).body(customerDB);
    }

    @DeleteMapping(path = "/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String customerId) {
        this.customerInputPort.deleteCustomerById(customerId);
        return ResponseEntity.noContent().build();
    }
}
````

## Ejecutando aplicación

````bash
$ curl -v http://localhost:8080/api/v1/customers | jq

>
< HTTP/1.1 200
<
[
  {
    "id": "354f9c97-7018-469f-bca0-8590e24150a5",
    "name": "Martín",
    "country": "Perú"
  },
  {
    "id": "8491984c-c191-437b-85cc-3203f12768bd",
    "name": "Pedrito",
    "country": "Argentina"
  },
  {
    "id": "da880956-e551-4817-ac22-fddb67b4e65b",
    "name": "Batistuta",
    "country": "Chile"
  },
  {
    "id": "7b66e9b7-0693-40aa-9fa9-fcaa17e9efd5",
    "name": "Ronaldinho",
    "country": "Brasil"
  }
]
````

````bash
$ curl -v http://localhost:8080/api/v1/customers/354f9c97-7018-469f-bca0-8590e24150a5 | jq

< HTTP/1.1 200
<
{
  "id": "354f9c97-7018-469f-bca0-8590e24150a5",
  "name": "Martín",
  "country": "Perú"
}
````

````bash
$ curl -v -X POST -G --data "name=Patricio&country=USA" http://localhost:8080/api/v1/customers | jq

< HTTP/1.1 201
< Location: /api/v1/customers/70f74ffa-dd55-41f0-a2ae-2037133fcb87
<
{
  "id": "70f74ffa-dd55-41f0-a2ae-2037133fcb87",
  "name": "Patricio",
  "country": "USA"
}
````

````bash
$ curl -v -X DELETE http://localhost:8080/api/v1/customers/70f74ffa-dd55-41f0-a2ae-2037133fcb87 | jq

>
< HTTP/1.1 204
````

````bash
$ curl -v -X POST -G --data "customerId=354f9c97-7018-469f-bca0-8590e24150a5&total=650" http://localhost:8080/api/v1/orders | jq

< HTTP/1.1 201
< Location: /api/v1/orders/4e3256ed-d0bc-4e50-afe8-d1240cc86eef
<
{
  "id": "4e3256ed-d0bc-4e50-afe8-d1240cc86eef",
  "customerId": "354f9c97-7018-469f-bca0-8590e24150a5",
  "total": 650
}
````

