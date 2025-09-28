package Controllers;

import CoreClasses.User;
import Interfaces.PaymentStrategy; // <-- Make sure this is imported
import Services.PaymentService;

public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // MODIFIED METHOD: Add the PaymentStrategy parameter here
    public void processPayment(final String bookingId, final User user, final PaymentStrategy paymentStrategy) throws Exception {
        // Now, pass the strategy along to the service
        paymentService.processPayment(bookingId, user, paymentStrategy);
    }
}