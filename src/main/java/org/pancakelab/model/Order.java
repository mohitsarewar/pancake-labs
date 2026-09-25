package org.pancakelab.model;

import java.util.Objects;
import java.util.UUID;

public class Order {
    private final UUID id;
    private final int building;
    private final int room;



    // in production values can flow from config
    private static final int MIN_BUILDING = 1;
    private static final int MIN_ROOM     = 1;

    public enum Status { OPEN, COMPLETED, PREPARED }
    private Status status = Status.OPEN;

    public Order(int building, int room) {
        if (building < MIN_BUILDING) {
            throw new IllegalArgumentException("Building does not exist: " + building);
        }
        if (room < MIN_ROOM) {
            throw new IllegalArgumentException("Room does not exist: " + room);
        }
        this.id = UUID.randomUUID();
        this.building = building;
        this.room = room;
    }

    public UUID getId() {
        return id;
    }

    public int getBuilding() {
        return building;
    }

    public int getRoom() {
        return room;
    }

    public Status getStatus() {
        return status;
    }

    public void complete() {
        moveTo(Status.OPEN, Status.COMPLETED);
    }

    public void prepare() {
        moveTo(Status.COMPLETED, Status.PREPARED);
    }

    public void ensureStatus(Status expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Order " + id + " is " + status + " but must be " + expected);
        }
    }

    private void moveTo(Status from, Status to) {
        ensureStatus(from);
        status = to;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return Objects.equals(id, order.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
