package com.statemachine.state_machine.services;

import com.statemachine.state_machine.domain.Payment;
import com.statemachine.state_machine.domain.PaymentEvent;
import com.statemachine.state_machine.domain.PaymentState;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;

public interface PaymentService {
   Payment newPayment(Payment payment);
   StateMachine<PaymentState, PaymentEvent> preAuth(String paymentId);
   StateMachine<PaymentState, PaymentEvent> authorizePayment(String paymentId);
   StateMachine<PaymentState, PaymentEvent> declineAuth(String paymentId);
}
