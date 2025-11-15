package edu.ucsal.fiadopay.repo;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.ucsal.fiadopay.model.WebhookDelivery;
public interface WebhookDeliveryRepository extends JpaRepository<WebhookDelivery, Long> { }
