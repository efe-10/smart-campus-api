# Smart Campus API

A RESTful API for managing university campus rooms and sensors, built with Java and JAX-RS (Jersey) with an embedded Grizzly HTTP server.

## API Overview

The API manages three core resources:
- **Rooms** - Physical campus rooms with capacity info
- **Sensors** - Devices deployed in rooms (temperature, CO2, occupancy etc.)
- **Sensor Readings** - Historical data logged by each sensor

Base URL: `http://localhost:8080/api/v1`

---

## How to Build and Run

### Prerequisites
- Java 11 or higher
- Apache Maven
- Apache NetBeans (recommended) or any IDE

### Steps
1. Clone the repository:
