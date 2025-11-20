package edu.ucsal.fiadopay.model;

import edu.ucsal.fiadopay.annotation.merchant.MerchantStatus;
import jakarta.persistence.*;
import lombok.*; 

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Merchant {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String name;

    @Column(nullable=false, unique=true)
    private String clientId;

    @Column(nullable=false)
    private String clientSecret;

    private String webhookUrl;

    @Builder.Default
    @Enumerated(EnumType.STRING) 
    private Status status = Status.ACTIVE;

    public enum Status { ACTIVE, BLOCKED }

    public Status getMerchantStatus() {
        return this.status;
    }
}
