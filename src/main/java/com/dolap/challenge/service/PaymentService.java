package com.dolap.challenge.service;

import com.dolap.challenge.entity.Payment;
import com.dolap.challenge.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PaymentService {

    private Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private BankService bankService;
    private PaymentRepository paymentRepository;

    /**
     * Constructs a PaymentService with bankService and paymentRepository
     * where these are used to call the bank to capture the money and save the payment log in the
     * database.
     *
     * @param bankService is the interface used to call the bank
     * @param paymentRepository is the interface used to record the payment logs
     */
    public PaymentService(BankService bankService, PaymentRepository paymentRepository) {
        this.bankService = bankService;
        this.paymentRepository = paymentRepository;
    }

    /**
     * Calls the bank and pays the amount that's specified
     * It also keeps a payment log of all the successful or failed payments in the database
     *
     * @param price is the amount you want to pay
     * @return the payment log
     */
    public Payment pay(BigDecimal price) {
        //pay with bank
        BankPaymentRequest request = new BankPaymentRequest();
        request.setPrice(price);
        BankPaymentResponse response = bankService.pay(request);

        return saveBankResponse(price, response);
    }

    /**
     * Saves the bank response into a {@link Payment} object and stores it in the database
     *
     * @param price is the amount that's paid
     * @param response is the bank's response for the transaction
     * @return the payment object
     */
    @Transactional
    public Payment saveBankResponse(BigDecimal price, BankPaymentResponse response) {
        //insert records
        Payment payment = new Payment();
        payment.setBankResponse(response.getResultCode());
        payment.setPrice(price);
        paymentRepository.save(payment);
        logger.info("Payment saved successfully!");
        return payment;
    }

    /**
     * Retrieves all the payments from the database - no sort, no pagination, no nothing.
     * Raw data from the database
     *
     * @return list of payments
     */
    @Transactional
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }
}
