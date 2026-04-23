# Smart Campus API

A Restful API for managing university campus rooms and sensors, built with Java and JAX-RS (Jersey) with an embedded Grizzly HTTP server.

## API Overview

The API manages three core resources:
- **Rooms** - Physical campus rooms with capacity info
- **Sensors** - Devices deployed in rooms such as temperature
- **Sensor Readings** - Historical data logged by each sensor

Base URL: `http://localhost:8080/api/v1`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/api/v1/
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### 4. Get sensors filtered by type
```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Post a sensor reading
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

---

## Report: Answers to Coursework Questions

### Part 1 - Service Architecture & Setup

**Q: Explain the default lifecycle of a JAX-RS Resource class.**

By default, JAX-RS creates a new instance of a resource class for every incoming HTTP request. This is known as per-request scope. Because each request gets its own instance, instance variables are not shared between requests, which means they cannot be used to store shared state. In this project, shared data is managed using a singleton DataStore class. Since multiple requests could modify the DataStore concurrently, the use of thread-safe data structures or synchronization would be needed in a production environment to prevent race conditions and data loss.

**Q: Why is HATEOAS considered a hallmark of advanced RESTful design?**

HATEOAS (Hypermedia as the Engine of Application State) means that API responses include links to related actions and resources. This benefits client developers because they do not need to hardcode URLs or rely solely on static documentation. Instead, the API itself guides clients through available actions dynamically. This makes the API more self-documenting, reduces coupling between client and server, and allows the server to change URLs without breaking clients that follow the provided links.

---

### Part 2 - Room Management

**Q: Returning only IDs vs full room objects — what are the implications?**

Returning only IDs reduces the payload size, which saves network bandwidth and is faster when there are many rooms. However, the client must then make additional requests to fetch the details of each room, which increases the number of round trips. Returning full room objects gives the client everything it needs in one request, which is better for performance when the client needs all the data, but uses more bandwidth. The best approach depends on the use case — for large collections a lightweight list of IDs or summary objects is preferable, while full objects are better for single-resource lookups.

**Q: Is the DELETE operation idempotent in your implementation?**

Yes, the DELETE operation is idempotent. If a client sends the same DELETE request for a room multiple times, the first request will delete the room and return 200 OK. Every subsequent request for the same room ID will return 404 Not Found because the room no longer exists. The server state remains the same after the first deletion — the room is gone regardless of how many times the request is repeated. This satisfies the definition of idempotency, where multiple identical requests produce the same result as a single request.

---

### Part 3 - Sensor Operations & Linking

**Q: What happens if a client sends data in a format other than JSON to a @Consumes(APPLICATION_JSON) endpoint?**

If a client sends a request with a Content-Type of text/plain or application/xml to an endpoint annotated with @Consumes(MediaType.APPLICATION_JSON), JAX-RS will reject the request before it even reaches the resource method. The runtime returns a 415 Unsupported Media Type response automatically, indicating that the server cannot process the format of the request body. This protects the API from malformed or unexpected input formats without requiring any manual checking in the resource method.

**Q: Why is @QueryParam preferred over a path-based type filter like /sensors/type/CO2?**

Query parameters are semantically more appropriate for filtering, searching, and sorting collections because they are optional by nature. Using @QueryParam("type") means the endpoint GET /api/v1/sensors works for all sensors when no parameter is provided, and filters automatically when one is. In contrast, embedding the type in the URL path like /sensors/type/CO2 implies it is a distinct resource, which is misleading. It also makes the URL structure rigid and harder to extend with additional filters. Query parameters allow multiple filters to be combined cleanly, for example ?type=CO2&status=ACTIVE, which is not easily achieved with path segments.

---

### Part 4 - Deep Nesting with Sub-Resources

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern.**

The Sub-Resource Locator pattern improves code organisation by delegating responsibility for nested resources to dedicated classes. Instead of defining every possible path in a single large resource class, the locator method hands off control to a specialised class such as SensorReadingResource. This separation of concerns makes each class smaller, easier to understand, and easier to test independently. In large APIs with many levels of nesting, keeping all paths in one controller would create an unmaintainable class. The pattern also allows the sub-resource class to receive context, such as the sensorId, through its constructor, making it flexible and reusable.

---

### Part 5 - Error Handling & Logging

**Q: Why is HTTP 422 more semantically accurate than 404 when a referenced resource is missing inside a payload?**

A 404 Not Found response typically means the URL being requested does not exist. In this scenario, the URL /api/v1/sensors is valid and found — the problem is that the roomId value inside the JSON body refers to a room that does not exist. The request was syntactically correct but semantically invalid because it contains a reference that cannot be resolved. HTTP 422 Unprocessable Entity communicates precisely this — the server understood the request and its format, but could not process it due to a logical or referential error in the content.

**Q: What are the cybersecurity risks of exposing Java stack traces to API consumers?**

Exposing stack traces to external users reveals sensitive internal information that attackers can exploit. A stack trace discloses the full package and class structure of the application, the names of frameworks and libraries in use along with their versions, the exact line numbers where errors occurred, and internal logic flow. With this information an attacker can identify known vulnerabilities in specific library versions, craft targeted attacks based on the internal structure, and gain insight into the application's architecture. The GlobalExceptionMapper prevents this by returning a generic error message while logging the real details server-side only.

**Q: Why is it better to use JAX-RS filters for logging rather than manual Logger calls in every method?**

Using a filter implements logging as a cross-cutting concern, meaning it applies automatically to every request and response without modifying any resource method. If logging were added manually to each method, it would result in duplicated code across every endpoint, making the codebase harder to maintain. If the logging format needed to change, every method would need to be updated. Filters also guarantee consistent logging regardless of which developer wrote the resource method, and they can be enabled or disabled in one place without touching business logic.
