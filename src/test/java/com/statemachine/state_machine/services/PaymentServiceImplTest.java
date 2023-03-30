package com.statemachine.state_machine.services;

import com.statemachine.state_machine.domain.Payment;
import com.statemachine.state_machine.domain.PaymentEvent;
import com.statemachine.state_machine.domain.PaymentState;
import com.statemachine.state_machine.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.statemachine.StateMachine;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class PaymentServiceImplTest {

   @Autowired
   PaymentService paymentService;
   Payment payment = new Payment();
   @Autowired
   PaymentRepository paymentRepository;
   @BeforeEach
   void setUp() {
      payment.setAmount(new BigDecimal("12.99"));
   }

   @Test
   void preAuth() {
      Payment savedPayment = paymentService.newPayment(payment);
      StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());

      Payment preAuthedPayment = paymentRepository.findById(savedPayment.getId()).get();
      System.out.println(sm.getState());
      System.out.println(preAuthedPayment);
   }

   @Test
   void Auth() {
      Payment savedPayment = paymentService.newPayment(payment);
      StateMachine<PaymentState, PaymentEvent> sm = paymentService.preAuth(savedPayment.getId());
      if(sm.getState().getId() == PaymentState.PRE_AUTH){
         StateMachine<PaymentState, PaymentEvent> sm1 = paymentService.authorizePayment(savedPayment.getId());

         Payment authedPayment = paymentRepository.findById(savedPayment.getId()).get();
         System.out.println(sm.getState());
         System.out.println(authedPayment);
      }
      else{
         System.out.println("DEclined Pre Auth error.");
      }
   }
}