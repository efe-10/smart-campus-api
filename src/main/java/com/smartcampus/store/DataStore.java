package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    // Singleton instance
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // In-memory storage
    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    private final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    // Private constructor - prevents outside instantiation
    private DataStore() {}

    // --- Room methods ---
    public Map<String, Room> getRooms() { return rooms; }

    public Room getRoom(String id) { return rooms.get(id); }

    public void addRoom(Room room) { rooms.put(room.getId(), room); }

    public boolean deleteRoom(String id) {
        if (rooms.containsKey(id)) {
            rooms.remove(id);
            return true;
        }
        return false;
    }

    // --- Sensor methods ---
    public Map<String, Sensor> getSensors() { return sensors; }

    public Sensor getSensor(String id) { return sensors.get(id); }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Also add the sensor ID to its room
        Room room = rooms.get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().add(sensor.getId());
        }
        // Initialise an empty readings list for this sensor
        sensorReadings.put(sensor.getId(), new ArrayList<>());
    }

    public boolean deleteSensor(String id) {
        if (sensors.containsKey(id)) {
            Sensor sensor = sensors.get(id);
            // Remove sensor ID from its room
            Room room = rooms.get(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(id);
            }
            sensors.remove(id);
            sensorReadings.remove(id);
            return true;
        }
        return false;
    }

    // --- SensorReading methods ---
    public List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
        // Update the sensor's current value
        Sensor sensor = sensors.get(sensorId);
        if (sensor != null) {
            sensor.setCurrentValue(reading.getValue());
        }
    }
}