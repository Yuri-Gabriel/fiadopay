package edu.ucsal.fiadopay.repo;
import org.springframework.data.jpa.repository.JpaRepository;

import edu.ucsal.fiadopay.model.Merchant;

import java.util.Optional;
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
  Optional<Merchant> findByClientId(String clientId);
  boolean existsByName(String name);
}
