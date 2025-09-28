// Create new file: src/Factories/PaymentStrategyFactory.java
package Factories;

import CommonEnum.PaymentMethod;
import ConcretePaymentStrategies.DebitCardStrategy;
import ConcretePaymentStrategies.UpiStrategy;
import Interfaces.PaymentStrategy;

public class PaymentStrategyFactory {

    public static PaymentStrategy getPaymentStrategy(PaymentMethod method) {
        if (method == null) {
            return null;
        }
        switch (method) {
            case DEBIT_CARD:
                return new DebitCardStrategy();
            case UPI:
                return new UpiStrategy();
            // case CREDIT_CARD:
            //     return new CreditCardStrategy(); // Add this when you create the class
            default:
                throw new IllegalArgumentException("Unknown Payment Method: " + method);
        }
    }
}