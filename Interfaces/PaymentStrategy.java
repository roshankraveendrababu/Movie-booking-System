// Modify file: src/Interfaces/PaymentStrategy.java
package Interfaces;

import CommonEnum.PaymentStatus;

public interface PaymentStrategy {
    PaymentStatus processPayment(); // Changed to return PaymentStatus
}