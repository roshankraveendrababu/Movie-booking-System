// Modify file: src/ConcretePaymentStrategies/UpiStrategy.java
package ConcretePaymentStrategies;

import CommonEnum.PaymentStatus;
import Interfaces.PaymentStrategy;

public class UpiStrategy implements PaymentStrategy {
    @Override
    public PaymentStatus processPayment() {
        System.out.println("Processing UPI payment... Bank error!");
        // Simulate a failed payment
        return PaymentStatus.FAILURE_BANK_ERROR;
    }
}