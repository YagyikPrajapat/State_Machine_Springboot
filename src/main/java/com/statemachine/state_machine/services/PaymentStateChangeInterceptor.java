package com.statemachine.state_machine.services;

import com.statemachine.state_machine.domain.Payment;
import com.statemachine.state_machine.domain.PaymentEvent;
import com.statemachine.state_machine.domain.PaymentState;
import com.statemachine.state_machine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import javax.swing.text.html.Option;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentStateChangeInterceptor extends StateMachineInterceptorAdapter<PaymentState, PaymentEvent> {
   private final PaymentRepository paymentRepository;
   @Override
   public void preStateChange(State<PaymentState, PaymentEvent> state, Message<PaymentEvent> message, Transition<PaymentState, PaymentEvent> transition,
                              StateMachine<PaymentState, PaymentEvent> stateMachine, StateMachine<PaymentState, PaymentEvent> rootStateMachine) {

      Optional.ofNullable(message).ifPresent(msg -> {
         Optional.ofNullable(String.class.cast(msg.getHeaders().getOrDefault(PaymentServiceImpl.PAYMENT_ID_HEADER, -1)))
                 .ifPresent(paymentId -> {
                    Payment payment = paymentRepository.findById(paymentId).get();
                    payment.setState(state.getId());
                    paymentRepository.save(payment);
                 });
      });
   }
}
