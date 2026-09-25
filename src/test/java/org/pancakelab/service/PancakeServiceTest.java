package org.pancakelab.service;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PancakeServiceTest {
    private PancakeService pancakeService = new PancakeService();
    private UUID orderId = null;

    private final static String DARK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with dark chocolate!";
    private final static String MILK_CHOCOLATE_PANCAKE_DESCRIPTION           = "Delicious pancake with milk chocolate!";
    private final static String MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION = "Delicious pancake with milk chocolate, hazelnuts!";

    @Test
    @org.junit.jupiter.api.Order(10)
    public void GivenOrderDoesNotExist_WhenCreatingOrder_ThenOrderCreatedWithCorrectData_Test() {
        // setup

        // exercise
        orderId = pancakeService.createOrder(10, 20);

        OrderSummary summary = pancakeService.orderSummary(orderId);
        assertEquals(10, summary.building());
        assertEquals(20, summary.room());

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
        List<String> ordersPancakes = pancakeService.viewOrder(orderId);

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
        pancakeService.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 2);
        pancakeService.removePancakes(MILK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 3);
        pancakeService.removePancakes(MILK_CHOCOLATE_HAZELNUTS_PANCAKE_DESCRIPTION, orderId, 1);

        // verify
        List<String> ordersPancakes = pancakeService.viewOrder(orderId);

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
        pancakeService.completeOrder(orderId);

        // verify
        Set<UUID> completedOrdersOrders = pancakeService.listCompletedOrders();
        assertTrue(completedOrdersOrders.contains(orderId));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(50)
    public void GivenOrderExists_WhenPreparingOrder_ThenOrderPrepared_Test() {
        // setup

        // exercise
        pancakeService.prepareOrder(orderId);

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertTrue(preparedOrders.contains(orderId));

        // tear down
    }

    @Test
    @org.junit.jupiter.api.Order(60)
    public void GivenOrderExists_WhenDeliveringOrder_ThenCorrectOrderReturnedAndOrderRemovedFromTheDatabase_Test() {
        // setup
        List<String> pancakesToDeliver = pancakeService.viewOrder(orderId);

        // exercise
        OrderSummary delivered = pancakeService.deliverOrder(orderId);

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(orderId));

        List<String> ordersPancakes = pancakeService.viewOrder(orderId);

        assertEquals(List.of(), ordersPancakes);
        assertEquals(orderId, delivered.orderId());
        assertEquals(10, delivered.building());
        assertEquals(20, delivered.room());
        assertEquals(pancakesToDeliver, delivered.pancakes());

        // tear down
        orderId = null;
    }

    @Test
    @org.junit.jupiter.api.Order(70)
    public void GivenOrderExists_WhenCancellingOrder_ThenOrderAndPancakesRemoved_Test() {
        // setup
        orderId = pancakeService.createOrder(10, 20);
        addPancakes();

        // exercise
        pancakeService.cancelOrder(orderId);

        // verify
        Set<UUID> completedOrders = pancakeService.listCompletedOrders();
        assertFalse(completedOrders.contains(orderId));

        Set<UUID> preparedOrders = pancakeService.listPreparedOrders();
        assertFalse(preparedOrders.contains(orderId));

        List<String> ordersPancakes = pancakeService.viewOrder(orderId);

        assertEquals(List.of(), ordersPancakes);

        // tear down
    }

    @Test
    public void GivenOrderExists_WhenAddingIngredientToPancake_ThenOrderContainsThatPancake_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);

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
        UUID orderId = service.createOrder(10, 20);
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
        UUID orderId = service.createOrder(10, 20);

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.addIngredient(orderId, UUID.randomUUID(), "dark chocolate"));
    }

    @Test
    public void GivenPancakeBelongsToAnotherOrder_WhenAddingIngredient_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID firstOrderId = service.createOrder(10, 20);
        UUID secondOrderId = service.createOrder(11, 21);
        UUID pancakeInFirstOrder = service.createPancake(firstOrderId);

        // exercise & verify
        assertThrows(IllegalArgumentException.class,
                () -> service.addIngredient(secondOrderId, pancakeInFirstOrder, "dark chocolate"));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void GivenBuildingDoesNotExist_WhenCreatingOrder_ThenRejected_Test(int building) {
        // setup
        PancakeService service = new PancakeService();

        // exercise & verify
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(building, 20));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0})
    public void GivenRoomDoesNotExist_WhenCreatingOrder_ThenRejected_Test(int room) {
        // setup
        PancakeService service = new PancakeService();

        // exercise & verify
        assertThrows(IllegalArgumentException.class, () -> service.createOrder(10, room));
    }

    @Test
    public void GivenBoundaryBuildingAndRoom_WhenCreatingOrder_ThenAccepted_Test() {
        // setup
        PancakeService service = new PancakeService();

        // exercise & verify
        assertDoesNotThrow(() -> service.createOrder(1, 1));
        assertDoesNotThrow(() -> service.createOrder(20, 999));
    }

    @Test
    public void GivenOrderNotCompleted_WhenPreparingOrder_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);

        // exercise & verify
        assertThrows(IllegalStateException.class, () -> service.prepareOrder(orderId));
    }

    @Test
    public void GivenOrderNotPrepared_WhenDeliveringOrder_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class, () -> service.deliverOrder(orderId));
    }

    @Test
    public void GivenOrderCompleted_WhenAddingPancake_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class, () -> service.createPancake(orderId));
    }

    @Test
    public void GivenOrderCompleted_WhenAddingIngredient_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        UUID pancakeId = service.createPancake(orderId);
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class,
                () -> service.addIngredient(orderId, pancakeId, "dark chocolate"));
    }

    @Test
    public void GivenOrderCompleted_WhenCompletingAgain_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class, () -> service.completeOrder(orderId));
    }

    @Test
    public void GivenOrderCompleted_WhenCancellingOrder_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class, () -> service.cancelOrder(orderId));
    }

    @Test
    public void GivenOrderDoesNotExist_WhenCompletingOrder_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();

        // exercise & verify
        assertThrows(IllegalArgumentException.class, () -> service.completeOrder(UUID.randomUUID()));
    }

    @Test
    public void GivenOrderCompleted_WhenRemovingPancakes_ThenRejected_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        UUID pancakeId = service.createPancake(orderId);
        service.addIngredient(orderId, pancakeId, "dark chocolate");
        service.completeOrder(orderId);

        // exercise & verify
        assertThrows(IllegalStateException.class,
                () -> service.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 1));

        // verify: the pancake is still there
        assertEquals(List.of(DARK_CHOCOLATE_PANCAKE_DESCRIPTION), service.viewOrder(orderId));
    }

    @Test
    public void GivenThreeMatchingPancakes_WhenRemovingOne_ThenOneReportedRemoved_Test() {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        for (int i = 0; i < 3; i++) {
            UUID pancakeId = service.createPancake(orderId);
            service.addIngredient(orderId, pancakeId, "dark chocolate");
        }

        // exercise
        int removed = service.removePancakes(DARK_CHOCOLATE_PANCAKE_DESCRIPTION, orderId, 1);

        // verify
        assertEquals(1, removed);
        assertEquals(2, service.viewOrder(orderId).size());
    }

    @Test
    public void GivenManyDisciplesAddingPancakesConcurrently_WhenAllFinish_ThenNoPancakeIsLost_Test() throws Exception {
        // setup
        PancakeService service = new PancakeService();
        UUID orderId = service.createOrder(10, 20);
        int pancakeCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CountDownLatch startSignal = new CountDownLatch(1);
        List<Future<?>> results = new ArrayList<>();

        // exercise
        for (int i = 0; i < pancakeCount; i++) {
            results.add(executor.submit(() -> {
                startSignal.await();
                UUID pancakeId = service.createPancake(orderId);
                service.addIngredient(orderId, pancakeId, "dark chocolate");
                return null;
            }));
        }
        startSignal.countDown();
        for (Future<?> result : results) {
            result.get(5, TimeUnit.SECONDS);
        }
        executor.shutdown();

        // verify
        assertEquals(pancakeCount, service.viewOrder(orderId).size());
    }

    private void addPancakes() {
        addPancakes(3, "dark chocolate");
        addPancakes(3, "milk chocolate");
        addPancakes(3, "milk chocolate", "hazelnuts");
    }

    private void addPancakes(int count, String... ingredients) {
        for (int i = 0; i < count; i++) {
            UUID pancakeId = pancakeService.createPancake(orderId);
            for (String ingredient : ingredients) {
                pancakeService.addIngredient(orderId, pancakeId, ingredient);
            }
        }
    }
}
