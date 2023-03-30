package com.statemachine.state_machine.services;

import com.statemachine.state_machine.domain.Payment;
import com.statemachine.state_machine.domain.PaymentEvent;
import com.statemachine.state_machine.domain.PaymentState;
import com.statemachine.state_machine.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
   public static final String PAYMENT_ID_HEADER = "payment_id";
   private final PaymentRepository paymentRepository;
   private final StateMachineFactory<PaymentState, PaymentEvent> stateMachineFactory;
   private final PaymentStateChangeInterceptor paymentStateChangeInterceptor;
   @Override
   public Payment newPayment(Payment payment) {
      payment.setState(PaymentState.NEW);
      payment.setId(UUID.randomUUID().toString());
      paymentRepository.save(payment);
      return payment;
   }

   @Override
   public StateMachine<PaymentState, PaymentEvent> preAuth(String paymentId) {
      StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

      sendEvent(paymentId, sm, PaymentEvent.PRE_AUTHORIZE);
      return sm;
   }

   @Override
   public StateMachine<PaymentState, PaymentEvent> authorizePayment(String paymentId) {
      StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

      sendEvent(paymentId, sm, PaymentEvent.AUTHORIZE);
      return sm;
   }


   //NOT USED
   @Override
   public StateMachine<PaymentState, PaymentEvent> declineAuth(String paymentId) {
      StateMachine<PaymentState, PaymentEvent> sm = build(paymentId);

      sendEvent(paymentId, sm, PaymentEvent.AUTH_DECLINED);
      return sm;
   }

   private void sendEvent(String paymentId, StateMachine<PaymentState, PaymentEvent> sm, PaymentEvent event){
      Message<PaymentEvent> msg = MessageBuilder.withPayload(event)
                      .setHeader(PAYMENT_ID_HEADER, paymentId)
                              .build();
      sm.sendEvent(msg);
   }

   private StateMachine<PaymentState, PaymentEvent> build(String paymentId){
      Payment payment = paymentRepository.findById(paymentId).get();
      StateMachine<PaymentState, PaymentEvent> sm = stateMachineFactory.getStateMachine(payment.getId());
      sm.stop();

      sm.getStateMachineAccessor()
              .doWithAllRegions(sma -> {
                 sma.addStateMachineInterceptor(paymentStateChangeInterceptor);
                 sma.resetStateMachine(new DefaultStateMachineContext<>(payment.getState(), null, null, null));
              });
      sm.start();
      return sm;
   }
}
