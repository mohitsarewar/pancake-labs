package org.pancakelab.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.pancakelab.model.Order;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PancakeServiceTest {
    private PancakeService pancakeService = new PancakeService();
    private Order          order          = null;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @Test
    @org.junit.jupiter.api.Order(10)
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        // setup

        // exercise
        order = pancakeService.createOrder(10, 20);

        assertEquals(10, order.getBuilding());
        assertEquals(20, order.getRoom());

        // verify

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(20)
    public void GivenOrderExists_WhenAddingPancakes_ThenCorrectNumberOfPancakesAdded_Test() {
        // setup

        // exercise
        addPancakes();

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(30)
    public void GivenPancakesExists_WhenRemovingPancakes_ThenCorrectNumberOfPancakesRemoved_Test() {
        // setup

        // exercise
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, order.getId(), 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, order.getId(), 1);

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION,
                             MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION), ordersPancakes);

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(40)
    public void GivenOrderExists_WhenCompletingOrder_ThenOrderCompleted_Test() {
        // setup

        // exercise
        pancakeService.completeOrder(order.getId());

        // verify
        Set<UUID> completedOrdersOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(order.getId()));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup

        // exercise
        pancakeService.prepareOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertTrue(preparedOrders.contains(order.getId()));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        List<String> pancakesToDeliver = pancakeService.viewOrder(order.getId());

        // exercise
        Object[] deliveredOrder = pancakeService.deliverOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(), ordersPancakes);
        assertEquals(order.getId(), ((Order) deliveredOrder[0]).getId());
        assertEquals(pancakesToDeliver, (List<String>) deliveredOrder[1]);

        // tear down
        order = null;
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        order = pancakeService.createOrder(10, 20);
        addPancakes();

        // exercise
        pancakeService.cancelOrder(order.getId());

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(order.getId()));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(order.getId()));

        List<String> ordersPancakes = pancakeService.viewOrder(order.getId());

        assertEquals(List.of(), ordersPancakes);

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenAddingIngredientToPancake_ThenOrderContainsThatPancake_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20).getId();

        // exercise
        UUID pancakeId = service.createPancake(orderId);
        service.addIngredient(orderId, pancakeId, "dark chocolate");

        // verify
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION), service.viewOrder(orderId));
    }

    @Test
    public void GivenPancakeExists_WhenAddingIngredientNotOnMenu_ThenRejectedAndPancakeUnchanged_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20).getId();
        UUID pancakeId = service.createPancake(orderId);
        service.addIngredient(orderId, pancakeId, "dark chocolate");

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.addIngredient(orderId, pancakeId, "mustard"));

        // verify
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION), service.viewOrder(orderId));

        // tear down
    }

    @Test
    public void GivenOrderDoesNotExist_WhenCreatingPancake_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.createPancake(UUID.randomUUID()));
    }

    @Test
    public void GivenPancakeDoesNotExist_WhenAddingIngredient_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20).getId();

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.addIngredient(orderId, UUID.randomUUID(), "dark chocolate"));
    }

    @Test
    public void GivenPancakeBelongsToAnotherOrder_WhenAddingIngredient_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID firstOrderId = service.createOrder(10, 20).getId();
        UUID secondOrderId = service.createOrder(11, 21).getId();
        UUID pancakeInFirstOrder = service.createPancake(firstOrderId);

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.addIngredient(secondOrderId, pancakeInFirstOrder, "dark chocolate"));
    }

    private void addPancakes() {
        addPancakes(3, "dark chocolate");
        addPancakes(3, "milk chocolate");
        addPancakes(3, "milk chocolate", "hazelnuts");
    }

    private void addPancakes(int count, String... ingredients) {
        for (int i = 0; i < count; i++) {
            UUID pancakeId = pancakeService.createPancake(order.getId());
            for (String ingredient : ingredients) {
                pancakeService.addIngredient(order.getId(), pancakeId, ingredient);
            }
        }
    }
}
