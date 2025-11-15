package edu.ucsal.fiadopay.repo;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.ucsal.fiadopay.model.Payment;

import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment, String> {
  Optional<Payment> findByIdempotencyKeyAndMerchantId(String ik, Long mid);
}
