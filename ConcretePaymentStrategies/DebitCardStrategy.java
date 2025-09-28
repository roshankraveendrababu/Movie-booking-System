// Modify file: src/ConcretePaymentStrategies/DebitCardStrategy.java
package ConcretePaymentStrategies;

import CommonEnum.PaymentStatus;
import Interfaces.PaymentStrategy;

public class DebitCardStrategy implements PaymentStrategy {
    @Override
    public PaymentStatus processPayment() {
        System.out.println("Processing Debit Card payment... Success!");
        // Simulate a successful payment
        return PaymentStatus.SUCCESS;
    }
}