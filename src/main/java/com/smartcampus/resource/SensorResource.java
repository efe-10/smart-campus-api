package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // GET /api/v1/sensors or /api/v1/sensors?type=CO2
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensorList = new ArrayList<>(store.getSensors().values());
        if (type != null && !type.isEmpty()) {
            List<Sensor> filtered = new ArrayList<>();
            for (Sensor s : sensorList) {
                if (s.getType().equalsIgnoreCase(type)) {
                    filtered.add(s);
                }
            }
            return Response.ok(filtered).build();
        }
        return Response.ok(sensorList).build();
    }

    // POST /api/v1/sensors - register a new sensor
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor ID is required\"}")
                    .build();
        }
        if (store.getSensor(sensor.getId()) != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Sensor with this ID already exists\"}")
                    .build();
        }
        // Validate that the roomId exists
        if (sensor.getRoomId() == null || store.getRoom(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException(
                "Room with ID '" + sensor.getRoomId() + "' does not exist.");
        }
        store.addSensor(sensor);
        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // GET /api/v1/sensors/{sensorId}
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // DELETE /api/v1/sensors/{sensorId}
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        if (store.getSensor(sensorId) == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"error\":\"Sensor not found\"}")
                    .build();
        }
        store.deleteSensor(sensorId);
        return Response.ok("{\"message\":\"Sensor deleted successfully\"}").build();
    }

    // Sub-resource locator for readings
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            throw new NotFoundException("Sensor not found: " + sensorId);
        }
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(sensorId);
        }
        return new SensorReadingResource(sensorId);
    }
}