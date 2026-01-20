package com.learning.e_commerce_rabbit.concurrency.controllers;

//@Slf4j
//@RestController
//@RequestMapping("/test")
//@RequiredArgsConstructor
//public class StarvationTestController {
//
//    private final OrderEventProducer producer;
//    private final Random random = new Random();
//
//    @PostMapping("/starvation")
//    public String runStarvationTest() {
//        int premiumCount = 20;
//        int normalCount = 40;
//        int totalOrders = premiumCount + normalCount;
//
//        log.info("=== STARVATION TEST BAŞLIYOR ===");
//        log.info("Premium mesajlar: {}", premiumCount);
//        log.info("Normal mesajlar: {}", normalCount);
//        log.info("Toplam: {}", totalOrders);
//        log.info("================================");
//
//        List<Order> orders = new ArrayList<>();
//
//        // Premium mesajlar oluştur (priority 8)
//        for (int i = 0; i < premiumCount; i++) {
//            orders.add(new Order(
//                    UUID.randomUUID().toString(),
//                    "Premium-" + i,
//                    500.0,
//                    Order.OrderStatus.PENDING,
//                    true // premium
//            ));
//        }
//
//        // Normal mesajlar oluştur (priority 1)
//        for (int i = 0; i < normalCount; i++) {
//            orders.add(new Order(
//                    UUID.randomUUID().toString(),
//                    "Normal-" + i,
//                    100.0,
//                    Order.OrderStatus.PENDING,
//                    false // normal
//            ));
//        }
//
//        // Karıştır - böylece premium ve normal mesajlar karışık gönderilir
//        Collections.shuffle(orders);
//
//        // HIZLICA tüm mesajları gönder (gecikme YOK!)
//        for (Order order : orders) {
//            producer.publishOrderCreated(order);
//        }
//
//        log.info("=== {} MESAJ GÖNDERİLDİ ===", totalOrders);
//        log.info("Şimdi logları izleyin:");
//        log.info("- Premium mesajlar (priority=8) önce işlenmeli");
//        log.info("- Normal mesajlar (priority=1) sonra işlenmeli");
//
//        return String.format(
//                "Starvation test tamamlandı!%n" +
//                        "Premium: %d, Normal: %d, Toplam: %d%n" +
//                        "Logları kontrol edin.",
//                premiumCount, normalCount, totalOrders
//        );
//    }
//
//    @PostMapping("/starvation-extreme")
//    public String runExtremeStarvationTest() {
//        //  Aşırı senaryo: 100 normal, 10 premium
//        int premiumCount = 10;
//        int normalCount = 100;
//
//        log.info("=== EXTREME STARVATION TEST ===");
//        log.info("Normal mesajlar: {} (çok fazla!)", normalCount);
//        log.info("Premium mesajlar: {} (az)", premiumCount);
//
//        List<Order> orders = new ArrayList<>();
//
//        // Önce 50 normal mesaj
//        for (int i = 0; i < 50; i++) {
//            orders.add(createNormalOrder("Normal-Start-" + i));
//        }
//
//        // Sonra premium mesajlar
//        for (int i = 0; i < premiumCount; i++) {
//            orders.add(createPremiumOrder("Premium-" + i));
//        }
//
//        // Sonra 50 normal mesaj daha
//        for (int i = 50; i < normalCount; i++) {
//            orders.add(createNormalOrder("Normal-End-" + i));
//        }
//
//        // Karıştırmadan gönder - böylece premium'lar ortada
//        for (Order order : orders) {
//            producer.publishOrderCreated(order);
//        }
//
//        log.info("=== Test gönderildi, premium'lar ÖNCE işlenmeli ===");
//
//        return "Extreme starvation test tamamlandı!";
//    }
//
//    private Order createPremiumOrder(String name) {
//        return new Order(
//                UUID.randomUUID().toString(),
//                name,
//                500.0,
//                Order.OrderStatus.PENDING,
//                true
//        );
//    }
//
//    private Order createNormalOrder(String name) {
//        return new Order(
//                UUID.randomUUID().toString(),
//                name,
//                100.0,
//                Order.OrderStatus.PENDING,
//                false
//        );
//    }
//}