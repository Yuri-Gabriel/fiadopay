# FiadoPay - Enhancements & Improvements

> **Documentação Completa** das alterações e funcionalidades implementadas no projeto **FiadoPay** da UCSAL, desenvolvido sobre a base criada pelo professor Mario Jorge Pereira com extensões implementadas pela equipe.
Discentes: Arthur Gabriel Palmeira Teixeira, Emilly Silva Marques dos Santos, Isabel de Oliveira Passos Alves, Maria Eduarda Navarro Datoli Suzart Leal, Yuri Gabriel Ferreira de Jesus Menezes.

---

## Sumário Executivo

**FiadoPay** é uma plataforma de processamento de pagamentos (payment processor) com suporte a múltiplos métodos (CARD, PIX, BOLETO) que oferece webhooks para notificação assíncronas. As melhorias implementadas pela equipe focam em **segurança em camadas**, **observabilidade detalhada**, **performance assíncronas** e **manutenibilidade** através de:

- **7 Anotações Customizadas com AOP e Reflexão** - Logs estruturados, validação de moeda, rate-limiting, reembolsos com prazo, status de merchant, método de pagamento
- **Processamento Assíncronos com Thread Pool** - ExecutorService com FixedThreadPool para entrega de webhooks sem bloqueio
- **Monitoramento e Observabilidade** - Watchdog thread para detectar workers travados
- **Validações em Camadas** - Proteção com múltiplas anotações por endpoint

---

## Arquitetura Geral

```
┌─────────────────────────────────────────────────────────┐
│           REST Controllers                              │
│    (Auth | Payment | Merchant Admin | Health)           │
└────────────────────┬────────────────────────────────────┘
                     │
        ┌────────────┼────────────┐
        │            │            │
┌───────▼────┐  ┌────▼─────┐  ┌───▼───────────┐
│  AOP       │  │ Services │  │ Anotações &   │
│ Aspects    │  │          │  │ Validators    │
└───────┬────┘  └────┬─────┘  └─────┬─────────┘
        │            │              │
        │  ┌─────────┴──────────────┤
        │  │                        │
  ┌─────▼──────────┐        ┌───────▼──────┐
  │  Repositories  │        │ Thread Pool  │
  │    (JPA)       │        │  & Watchdog  │
  └────────┬───────┘        └──────────────┘
           │
  ┌────────▼──────────┐
  │   Database (H2)   │
  └───────────────────┘
```

---

## Funcionalidades Implementadas

### 1 **@Logger** - Registro de Acessos a Rotas

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.logs.Logger`
- Aspect: `edu.ucsal.fiadopay.annotation.logs.LoggerAspect`
- Log Service: `edu.ucsal.fiadopay.annotation.logs.Log`
- Formatter: `edu.ucsal.fiadopay.annotation.logs.LogFormatter`

**Funcionamento:**
Registra automaticamente quando uma rota é acessada, capturando método HTTP, URI, função executada e tempo de execução.

**Definição:**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Logger {
    String file() default "route.log";
}
```

**Uso:**
```java
@Logger(file = "auth.log")
@PostMapping("/token")
public TokenResponse token(@RequestBody @Valid TokenRequest req) { 
    // ...
}
```

**Implementação do LoggerAspect:**
- Intercepta método anotado com `@Logger`
- Extrai informações do `HttpServletRequest`:
  - **httpMethod**: GET, POST, PUT, DELETE, etc.
  - **requestPath**: URI acessada (ex: `/fiadopay/auth/token`)
  - **signature**: Nome da função (ex: `token()`)
  - **tempoMs**: Tempo de execução em milissegundos
- Persiste em arquivo separado (ex: `auth.log`, `payment.log`)
- Formato: `dd/MM/yyyy HH:mm:ss.SSS - LEVEL - Mensagem`

**Exemplo de Log:**
```
20/11/2024 14:30:45.123 - INFO - [POST] /fiadopay/auth/token - Função token executada em 45 ms
20/11/2024 14:30:46.456 - INFO - [POST] /fiadopay/payment/create - Função create executada em 234 ms
```

**Estrutura de Logs:**
```
logs/
├── route.log
├── auth.log
├── payment.log
└── merchant.log
```

---

### 2 **@PaymentMethod** - Validação de Método de Pagamento

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.payment_method.PaymentMethod`
- Aspect: `edu.ucsal.fiadopay.annotation.payment_method.PaymentMethodAspect`
- Validador: `edu.ucsal.fiadopay.annotation.payment_method.PaymentMethodValidator`
- Enum: `edu.ucsal.fiadopay.annotation.payment_method.EPaymentMethod`

**Funcionamento:**
Marca qual método de pagamento um endpoint implementa e valida se requisições usam apenas os métodos permitidos.

**Definição:**
```java
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PaymentMethodValidator.class)
public @interface PaymentMethod {
    String message() default "Método de pagamento inválido";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    EPaymentMethod[] methods();  // Métodos aceitos
}
```

**Enum de Métodos:**
```java
public enum EPaymentMethod {
    PIX("PIX"),
    BOLETO("BOLETO"),
    DEBITO("DEBITO"),
    CREDITO("CREDITO");
    
    public String value;
    // ...
}
```

**Uso em Request DTO:**
```java
public record PaymentRequest(
    @PaymentMethod(methods = {EPaymentMethod.CREDITO, EPaymentMethod.DEBITO})
    String method,
    
    BigDecimal amount,
    String currency
) {}
```

**Implementação do PaymentMethodAspect:**
- Intercepta métodos com `@PaymentMethod`
- Extrai campo `method` do argumento usando reflexão
- Valida contra enum de métodos permitidos
- Se inválido → lança `IllegalStateException`

**Exemplo de Validação:**
```
Requisição: { "method": "TRANSFERENCIA", ... }
Permitidos: [PIX, BOLETO, DEBITO]
Erro: IllegalStateException: "Método de pagamento não aceito: TRANSFERENCIA"
```

---

### 3 **@Currency** - Restrição de Moedas Aceitas

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.Currency`

**Funcionamento:**
Restringe quais moedas são aceitas por um endpoint ou campo através de whitelist.

**Definição:**
```java
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
public @interface Currency {
    String[] allowed() default {"BRL"};
}
```

**Uso:**
```java
@Currency(allowed = {"BRL", "USD"})
@PostMapping("/payment")
public PaymentResponse createPayment(
    @RequestBody @Valid PaymentRequest req
) { ... }
```

**Comportamento:**
- Pode ser aplicada em métodos, parâmetros ou campos
- Valida se moeda da requisição está na whitelist
- Se fora da lista → lança `InvalidCurrencyException`

---

### 4 **@Refundable** - Reembolsos com Prazo Limite

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.refundable.Refundable`
- Aspect: `edu.ucsal.fiadopay.annotation.refundable.RefundableAspect`

**Funcionamento:**
Indica que uma venda pode ser estornada apenas até X dias após a entrega usando reflexão para validar.

**Definição:**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Refundable {
    int days() default 7;
}
```

**Uso:**
```java
@Refundable(days = 30)
@PostMapping("/{paymentId}/refund")
public PaymentResponse refund(@PathVariable String paymentId) { ... }
}
```

**Implementação do RefundableAspect:**
- Executa método normalmente primeiro
- Após sucesso, valida o resultado com reflexão:
  - Procura campo `delivered` (boolean)
  - Procura campo `lastAttemptAt` (Instant - data de entrega)
  - Calcula dias desde entrega: `ChronoUnit.DAYS.between(deliveryDate, Instant.now())`
  - Compara com limite anotado

**Fluxo de Validação:**
```
1. Método executa normalmente
2. Se resultado != null, começa validação
3. Extrai campo "delivered" via reflexão
   → Se false: lança "A mercadoria não foi entregue. Estorno não permitido."
4. Extrai campo "lastAttemptAt" (data de entrega)
5. Calcula dias decorridos
6. Se dias > limite: lança "Estorno não permitido. Prazo de 30 dias expirado..."
7. Se tudo OK: retorna resultado
```

**Exceções:**
- `IllegalStateException`: Prazo expirado
- `RuntimeException`: Campos não encontrados

---

### 5 **@VelocityCheck** - Rate Limiting & Anti-Fraud

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.VelocityCheck`

**Funcionamento:**
Bloqueia muitas tentativas em pouco tempo, protegendo contra força bruta e fraude.

**Definição:**
```java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VelocityCheck {
    int maxAttempts();              // Máximo de tentativas
    int timeWindowSeconds();        // Tamanho da janela em segundos
}
```

**Uso:**
```java
@VelocityCheck(maxAttempts = 5, timeWindowSeconds = 60)
@PostMapping("/token")
public TokenResponse token(@RequestBody @Valid TokenRequest req) { ... }
```

**Comportamento:**
- Rastreia tentativas por IP + rota + método
- Janela deslizante de N segundos
- Após maxAttempts → lança `TooManyAttemptsException`
- Ideal para endpoints de autenticação

---

### 6 *@MerchantStatus** - Validação de Status do Merchant

**Localização:**
- Anotação: `edu.ucsal.fiadopay.annotation.merchant.MerchantStatus`
- Aspect: `edu.ucsal.fiadopay.annotation.merchant.MerchantStatusAspect`

**Funcionamento:**
Não permite operações se o merchant (vendedor) está inativo usando reflexão.

**Definição:**
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MerchantStatus {
    Status value() default Status.ACTIVE;
}

// Enum Status (no Merchant)
public enum Status { ACTIVE, INACTIVE, SUSPENDED }
```

**Uso:**
```java
@MerchantStatus(Status.ACTIVE)
@PostMapping("/payment")
public PaymentResponse createPayment(
    @RequestBody Merchant merchant
) { ... }
```

**Implementação do MerchantStatusAspect:**
- Intercepta método anotado
- Extrai argumentos do método
- Para cada argumento, tenta acessar campo `status` via reflexão
- Compara com status esperado na anotação
- Se diferente → lança `IllegalStateException`

**Estados Possíveis:**
| Status | Permite Operação? | Quando |
|--------|------------------|--------|
| ACTIVE | Sim | Merchant operacional |
| INACTIVE | Não | Ativação pendente |
| SUSPENDED | Não | Violação de política |

---

### 7 **@DeprecatedRoute** - Marcar Rotas Antigas

**Funcionamento:**
Documenta endpoints legados/descontinuados orientando para alternativas.

**Uso (conceitual):**
```java
@DeprecatedRoute(version = "1.0", replacedBy = "/api/v2/payment")
@GetMapping("/oldPayment")
public void oldEndpoint() { 
    // ...
}
```

---

## Processamento Assincronamente com Threads

### 1 **ExecutorService - FixedThreadPool**

**Localização:** `edu.ucsal.fiadopay.config.ThreadConfig`

**Funcionamento:**
Pool fixo de threads para envio de webhooks de forma não-bloqueante.

**Configuração:**
```java
@Configuration
public class ThreadConfig {
    @Bean(name = "webhookExecutor")
    public ExecutorService webhookExecutor() {
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("fiadopay-webhook-worker");
            thread.setDaemon(true);
            return thread;
        };
        return Executors.newFixedThreadPool(4, factory);
    }
}
```

**Características:**
- **Tipo:** FixedThreadPool (melhor para workloads previsíveis)
- **Tamanho:** 4 threads dedicadas
- **Nomes:** `fiadopay-webhook-worker-1`, `fiadopay-webhook-worker-2`, etc.
- **Daemon:** Terminam com a JVM
- **Ideal para:** Entrega de webhooks, notificações, tarefas assincronamente

**Uso em PaymentService:**
```java
@Autowired
@Qualifier("webhookExecutor")
private ExecutorService executorService;

// Executar tarefa assincronamente
executorService.execute(() -> {
    sendWebhookAsync(payment);
});

// Ou usar CompletableFuture
CompletableFuture.runAsync(() -> {
    processAndWebhook(payment.getId());
}, executorService);
```

**Benefícios:**
- Não bloqueia resposta HTTP
- Processamento paralelo eficiente
- Limite de recursos controlado
- Fácil monitoramento com nomes

---

### 2 **Watchdog Thread - Monitoramento e Observabilidade**

**Localização:** `edu.ucsal.fiadopay.service.PaymentService`

**Funcionamento:**
Thread watchdog que reinicia/monitora workers travados detectando inatividade.

**Implementação Conceitual:**
```java
private volatile long lastActivity = System.currentTimeMillis();
private final ScheduledExecutorService watchdog = 
    Executors.newScheduledThreadPool(1);

public PaymentService(...) {
    // Executa a cada 10 segundos
    watchdog.scheduleAtFixedRate(() -> {
        long elapsed = System.currentTimeMillis() - lastActivity;
        
        if (elapsed > 30_000) {  // 30 segundos
            logger.warn("WATCHDOG: {}ms sem atividade", elapsed);
            lastActivity = System.currentTimeMillis();
            System.gc();  // Force garbage collection
        }
    }, 10, 10, TimeUnit.SECONDS);
}
```

**Fluxo:**
```
1. Thread principal atualiza lastActivity a cada operação
2. Watchdog thread acordada a cada 10 segundos
3. Calcula tempo decorrido desde última atividade
4. Se > 30 segundos de inatividade:
   - Registra WARNING no log
   - Força garbage collection
   - Reseta contador
5. Volta a dormir por 10 segundos
```

**Características:**
- **Intervalo de Verificação:** 10 segundos
- **Limiar de Inatividade:** 30 segundos
- **Ações:** Log + GC forçado
- **Uso:** Detectar travamentos e memory leaks

**Exemplo de Log:**
```
2024-11-20 14:35:12.789 - WARNING - WATCHDOG: 30245ms sem atividade
2024-11-20 14:35:22.100 - WARNING - WATCHDOG: 31100ms sem atividade
```

---

## Estrutura de Diretórios

```
src/main/java/edu/ucsal/fiadopay/
│
├── annotation/
│   ├── logs/
│   │   ├── Logger.java           ← Anotação
│   │   ├── LoggerAspect.java     ← AOP Aspect
│   │   ├── Log.java              ← Service de logs
│   │   └── LogFormatter.java     ← Formatador customizado
│   │
│   ├── payment_method/
│   │   ├── PaymentMethod.java          ← Anotação com Constraint
│   │   ├── PaymentMethodValidator.java ← Validador
│   │   ├── PaymentMethodAspect.java    ← AOP Aspect
│   │   └── EPaymentMethod.java         ← Enum
│   │
│   ├── refundable/
│   │   ├── Refundable.java      ← Anotação
│   │   └── RefundableAspect.java ← AOP Aspect com reflexão
│   │
│   ├── merchant/
│   │   ├── MerchantStatus.java       ← Anotação
│   │   └── MerchantStatusAspect.java ← AOP Aspect com reflexão
│   │
│   ├── routes/
│   │   ├── DeprecatedRoute.java       ← Anotação
│   │   └── DeprecatedRouteAspect.java ← AOP Aspect com reflexão
│   │
│   ├── Currency.java             ← Anotação
│   ├── VelocityCheck.java        ← Anotação
│
├── config/
│   ├── ThreadConfig.java         ← Pool de threads
│   ├── OpenApiConfig.java
│   ├── CurrencyAspect.java       ← AOP Aspect com reflexão
│   └── VelocityCheckAspect.java  ← AOP Aspect com reflexão
│
├── controller/
│   ├── AuthController.java
│   ├── PaymentController.java
│   ├── MerchantAdminController.java
│   ├── HealthController.java
│   ├── Teste.java
│   ├── dto/
│   │   ├── MerchantCreateDTO.java
│   ├── request/
│   │   ├── PaymentRequest.java
│   │   ├── TokenRequest.java
│   │   ├── RefundRequest.java
│   └── response/
│       ├── PaymentResponse.java
│       └── TokenResponse.java
│
├── exception/
│   ├── TooManyAttemptsException.java
│   ├── ApplicationExceptionHandler.java
│   └── InvalidCurrencyException.java
│
├── model/
│   ├── Payment.java
│   ├── Merchant.java
│   └── WebhookDelivery.java
│
├── repo/
│   ├── PaymentRepository.java
│   ├── MerchantRepository.java
│   └── WebhookDeliveryRepository.java
│
├── service/
│   ├── PaymentService.java      ← Com Watchdog
│   ├── WebhookService.java
│   ├── VelocityRateLimiter.java
│
├── Currency.java
│
└── FiadoPayApplication.java
```

---

## Ciclo de Vida de um Pagamento

```
┌─ POST /fiadopay/payment/create
│
├─ @Logger
│  └─ Registra: [POST] /fiadopay/payment/create
│
├─ @VelocityCheck(maxAttempts=10, timeWindowSeconds=60)
│  └─ Valida tentativas por IP
│     → Se > 10 em 60s → TooManyAttemptsException
│
├─ @Currency(allowed={"BRL", "USD"})
│  └─ Extrai campo "currency" via reflexão
│     → Se currency = "EUR" → InvalidCurrencyException
│
├─ @MerchantStatus(Status.ACTIVE)
│  └─ Valida status do Merchant
│     → Se INACTIVE/SUSPENDED → IllegalStateException
│
├─ PaymentService.create()
│  ├─ Validar limites de transação
│  ├─ Calcular juros de parcelamento
│  ├─ Criar Payment entity com status: PENDING
│  └─ Salvar em banco de dados
│
├─ Executar assincronamente (FixedThreadPool)
│  ├─ CompletableFuture.runAsync()
│  ├─ Simular processamento (delay: 2000ms)
│  ├─ Atualizar status: APPROVED/DECLINED
│  ├─ lastActivity = current timestamp (para Watchdog)
│  └─ Chamar WebhookService
│
├─ WebhookService.sendWebhookAsync()
│  ├─ Montar JSON com dados do payment
│  ├─ Assinar com HMAC-SHA256
│  ├─ HTTP POST para webhook URL do merchant
│  ├─ Salvar WebhookDelivery (tentativa)
│  ├─ Se falha → retry (até 5 vezes)
│  └─ Registrar sucesso/falha
│
└─ Resposta HTTP (status: 200 OK, body: Payment)
   {
     "id": "pay_abc123",
     "status": "PENDING",
     "method": "CARD",
     "amount": 99.99,
     "currency": "BRL"
   }
   
   [Background] → Atualizado para APPROVED em ~2s
   [Webhook] → Enviado para merchant
```

---

## Guia de Uso - Exemplos Práticos

### Exemplo 1: Fluxo de Autenticação com Rate Limiting

```http
POST /fiadopay/auth/token HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "client_id": "merchant-123",
  "client_secret": "secret-abc"
}
```

**Proteções Aplicadas:**
1. `@Logger(file = "auth.log")` → Registra acesso
2. `@VelocityCheck(maxAttempts=5, timeWindowSeconds=60)` → Bloqueia após 5 tentativas
3. `@MerchantStatus` → Valida se merchant está ACTIVE

**Resposta (Sucesso):**
```json
{
  "access_token": "FAKE-123",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

**Resposta (Rate Limited):**
```
HTTP/1.1 429 Too Many Requests
Content-Type: application/json

{
  "error": "TooManyAttemptsException",
  "message": "Muitas tentativas. Tente novamente mais tarde."
}
```

---

### Exemplo 2: Criar Pagamento com Validação de Moeda

```http
POST /fiadopay/payment/create HTTP/1.1
Host: localhost:8080
Authorization: Bearer FAKE-123
Content-Type: application/json

{
  "method": "CREDITO",
  "amount": 199.99,
  "currency": "BRL",
  "installments": 3,
  "metadataOrderId": "order-789"
}
```

**Proteções Aplicadas:**
1. `@Logger` → Registra em `payment.log`
2. `@PaymentMethod(methods={CREDITO,DEBITO})` → Valida se método é aceito
3. `@Currency(allowed={"BRL","USD"})` → Valida moeda
4. `@MerchantStatus` → Merchant deve estar ACTIVE

**Resposta (Sucesso):**
```json
{
  "id": "pay_xyz789",
  "status": "PENDING",
  "method": "CREDITO",
  "amount": 199.99,
  "currency": "BRL",
  "installments": 3,
  "monthly_interest": 1.5,
  "total_with_interest": 209.07,
  "createdAt": "2024-11-20T14:30:00Z"
}
```

[Após 2 segundos, status muda para APPROVED/DECLINED e webhook é enviado]

---

### Exemplo 3: Reembolso com Validação de Prazo

```http
POST /fiadopay/payment/pay_xyz789/refund HTTP/1.1
Host: localhost:8080
Authorization: Bearer FAKE-123
```

**Proteções Aplicadas:**
1. `@Logger` → Registra em log
2. `@Refundable(days=30)` → Valida se está dentro de 30 dias
   - Extrai campo `delivered` via reflexão
   - Extrai campo `lastAttemptAt` (data de entrega)
   - Calcula dias decorridos
   - Se > 30 dias → IllegalStateException

**Resposta (Prazo Válido):**
```json
{
  "id": "pay_xyz789",
  "status": "REFUNDED",
  "refundedAt": "2024-11-20T14:45:00Z"
}
```

**Resposta (Prazo Expirado):**
```
HTTP/1.1 400 Bad Request
Content-Type: application/json

{
  "error": "IllegalStateException",
  "message": "Estorno não permitido. Prazo de 30 dias expirado (transação há 45 dias)."
}
```

---

## Segurança - Proteções em Camadas

### Matriz de Proteções por Rota

| Rota | @Logger | @VelocityCheck | @MerchantStatus | @Currency | @PaymentMethod | @Refundable |
|------|---------|----------------|-----------------|-----------|----------------|------------|
| POST /auth/token | S | S | S | - | - | - |
| POST /payment/create | S | N | S | S | S | - |
| POST /payment/{id}/refund | S | N | S | - | - | S |
| GET /merchant/{id} | S | N | - | - | - | - |

---

## Monitoramento & Observabilidade

### Estrutura de Logs

**Diretório padrão:** `./logs/`

```
logs/
├── route.log              # Log padrão de rotas
├── auth.log               # Tentativas de autenticação
├── payment.log            # Operações de pagamento
├── merchant.log           # Operações de merchant
└── webhook.log            # Entregas de webhooks
```

### Exemplos de Log

**1. Acesso a Rota (LoggerAspect):**
```
20/11/2024 14:30:45.123 - INFO - [POST] /fiadopay/auth/token - Função token executada em 45 ms
20/11/2024 14:30:46.456 - INFO - [POST] /fiadopay/payment/create - Função create executada em 234 ms
20/11/2024 14:30:47.789 - INFO - [POST] /fiadopay/payment/pay_xyz789/refund - Função refund executada em 156 ms
```

**2. Rate Limiting Acionado (VelocityCheckAspect):**
```
20/11/2024 14:35:00.000 - WARNING - Too many attempts for POST /fiadopay/auth/token from IP 192.168.1.100 within 60s
```

**3. Validação de Moeda Falhou (CurrencyAspect):**
```
20/11/2024 14:40:15.000 - ERROR - Currency not allowed: EUR. Permitted: [USD, BRL]
```

**4. Validação de Método de Pagamento Falhou:**
```
20/11/2024 14:42:30.000 - ERROR - Method not accepted: TRANSFERENCIA. Accepted: [PIX, BOLETO, CREDITO]
```

**5. Status de Merchant Inválido:**
```
20/11/2024 14:45:00.000 - ERROR - Status inválido: esperado ACTIVE, recebido INACTIVE
```

**6. Reembolso Expirado (RefundableAspect):**
```
20/11/2024 14:50:00.000 - ERROR - Estorno não permitido. Prazo de 30 dias expirado (transação há 45 dias).
```

**7. Watchdog Alerta (PaymentService):**
```
20/11/2024 14:55:12.789 - WARNING - WATCHDOG: 30245ms sem atividade
20/11/2024 14:55:22.100 - WARNING - WATCHDOG: 31100ms sem atividade
```

---

## Configuração

### `application.properties`

```properties
# ============ FIADOPAY ============
fiadopay.webhook-secret=seu-secret-webhook-aqui
fiadopay.processing-delay-ms=2000
fiadopay.failure-rate=0.1

# ============ THREADS ============
fiadopay.thread-pool-size=4
fiadopay.watchdog-check-interval-ms=10000
fiadopay.watchdog-inactivity-threshold-ms=30000

# ============ BANCO DE DADOS ============
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false

# ============ LOGGING ============
logging.level.root=INFO
logging.level.edu.ucsal.fiadopay=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n

# ============ SERVER ============
server.port=8080
spring.application.name=fiadopay
```

---

## Testes Práticos

### Teste 1: Rate Limiting (5 tentativas em 60s)

```bash
#!/bin/bash

for i in {1..10}; do
  echo "Tentativa $i..."
  curl -s -X POST http://localhost:8080/fiadopay/auth/token \
    -H "Content-Type: application/json" \
    -d '{"client_id":"test","client_secret":"wrong"}' | jq '.'
  sleep 1
done

# Esperado: 
# Tentativas 1-5: 200 OK (com erro de autenticação)
# Tentativas 6-10: 429 Too Many Requests
```

### Teste 2: Validação de Moeda

```bash
#!/bin/bash

# Teste com moeda permitida
curl -X POST http://localhost:8080/fiadopay/payment/create \
  -H "Authorization: Bearer FAKE-1" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "CREDITO",
    "amount": 99.99,
    "currency": "BRL",
    "installments": 1
  }'
# Esperado: 200 OK

# Teste com moeda não permitida
curl -X POST http://localhost:8080/fiadopay/payment/create \
  -H "Authorization: Bearer FAKE-1" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "CREDITO",
    "amount": 99.99,
    "currency": "JPY",
    "installments": 1
  }'
# Esperado: 400 Bad Request com InvalidCurrencyException
```

### Teste 3: Monitoramento com Watchdog

```bash
#!/bin/bash

# Terminal 1: Ver logs
tail -f logs/route.log | grep -i watchdog

# Terminal 2: Deixar sistema ocioso (sem requisições)
# Após 30 segundos, watchdog deve registrar inatividade

# Esperado:
# 20/11/2024 14:55:12.789 - WARNING - WATCHDOG: 30245ms sem atividade
```

### Teste 4: Reembolso com Prazo

```bash
#!/bin/bash

# Primeiro, criar um pagamento
RESPONSE=$(curl -s -X POST http://localhost:8080/fiadopay/payment/create \
  -H "Authorization: Bearer FAKE-1" \
  -H "Content-Type: application/json" \
  -d '{
    "method": "CREDITO",
    "amount": 150.00,
    "currency": "BRL",
    "installments": 1
  }')

PAYMENT_ID=$(echo $RESPONSE | jq -r '.id')

echo "Pagamento criado: $PAYMENT_ID"

# Tentar reembolsar imediatamente
curl -X POST http://localhost:8080/fiadopay/payment/$PAYMENT_ID/refund \
  -H "Authorization: Bearer FAKE-1"

# Esperado: 200 OK (dentro do prazo de 30 dias)

# Se simular passagem de 40 dias:
# Esperado: 400 Bad Request com "Estorno não permitido. Prazo expirado..."
```

---

## Padrões de Design Utilizados

| Padrão | Implementação | Benefício |
|--------|---------------|-----------|
| **AOP** | Aspects com `@Around` | Separação de concerns (logging, validação) |
| **Decorator** | Anotações em métodos | Comportamento customizável |
| **Observer** | Webhooks assincronos | Notificação desacoplada |
| **Strategy** | PaymentMethods enum | Múltiplas estratégias de pagamento |
| **Builder** | `Payment.builder()` | Construção fluente de objetos |
| **Repository** | Spring Data JPA | Abstração de persistência |
| **Singleton** | `Log.getInstance()` | Instância única por arquivo |
| **Thread Pool** | ExecutorService | Gerenciamento eficiente de threads |

---

## Stack Tecnológico

| Componente | Versão | Propósito |
|-----------|--------|----------|
| **Spring Boot** | 3.x+ | Framework web |
| **Spring Data JPA** | - | ORM/Persistência |
| **Spring AOP** | - | Programação orientada a aspectos |
| **H2 Database** | - | Banco em memória para testes |
| **Lombok** | - | Getters/Setters/Construtores |
| **Jakarta Validation** | 3.x+ | Validação de entrada |
| **Jackson** | - | Serialização JSON |
| **JDK** | 11+ | Thread pools, Completable Future |

---

## Conceitos Avançados

### Reflexão em Ação

**RefundableAspect** usa reflexão para inspecionar objetos em runtime:

```java
Field deliveredField = target.getClass().getDeclaredField("delivered");
deliveredField.setAccessible(true);
Boolean delivered = (Boolean) deliveredField.get(target);
```

**Vantagens:**
- Validação genérica sem casting
- Funciona com qualquer classe que tenha os campos
- Desacoplado da estrutura específica

---

### AOP com Reflexão

**MerchantStatusAspect** combina AOP + reflexão:

```java
@Around("@annotation(merchantstatus)")
public Object around(ProceedingJoinPoint joinPoint, MerchantStatus merchantStatus) {
    // 1. AOP: Intercepta método
    // 2. Reflexão: Extrai campo "status"
    // 3. Validação: Compara com anotação
    // 4. Proceed: Executa se válido
}
```

---

### Thread Pools e Async/Await

**CompletableFuture com ExecutorService:**

```java
CompletableFuture.runAsync(() -> {
    // Executado em thread separada do pool
    processAndWebhook(payment.getId());
    lastActivity = System.currentTimeMillis();  // Para watchdog
}, executorService)
.exceptionally(ex -> {
    logger.error("Erro processando webhook", ex);
    return null;
});
```

---

## Tratamento de Exceções

### Exceções Customizadas

```java
// 1. TooManyAttemptsException
public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException(String message) {
        super(message);
    }
}

// 2. InvalidCurrencyException
public class InvalidCurrencyException extends RuntimeException {
    // ...
}
```

### Exception Handler Global

```java
@RestControllerAdvice
public class ApplicationExceptionHandler {
    
    @ExceptionHandler(TooManyAttemptsException.class)
    public ResponseEntity<?> handleTooManyAttempts(TooManyAttemptsException ex) {
        return ResponseEntity.status(429)
            .body(Map.of("error", "Too Many Requests"));
    }
    
    @ExceptionHandler(InvalidCurrencyException.class)
    public ResponseEntity<?> handleInvalidCurrency(InvalidCurrencyException ex) {
        return ResponseEntity.status(400)
            .body(Map.of("error", ex.getMessage()));
    }
}
```

---

## Escalabilidade Futura

- [ ] **Cache distribuído** (Redis) para rate-limiting
- [ ] **Métricas** (Micrometer + Prometheus)
- [ ] **Tracing distribuído** (Jaeger/Zipkin)
- [ ] **Circuit breaker** para webhooks (Resilience4j)
- [ ] **Autenticação OAuth2** (Spring Security)
- [ ] **Criptografia** de dados sensíveis
- [ ] **Auditoria** completa de operações
- [ ] **Dashboard** de monitoramento em tempo real

---

## Suporte e Documentação

**Instituição:** UCSAL - Universidade Católica de Salvador  
**Projeto:** FiadoPay - Payment Processing Platform  
**Base:** Desenvolvido por Mário Jorge Pereira  
**Extensões:** Implementadas pela equipe Arthur Gabriel Palmeira Teixeira, Emilly Silva Marques dos Santos, Isabel de Oliveira Passos Alves, Maria Eduarda Navarro Datoli Suzart Leal, Yuri Gabriel Ferreira de Jesus Menezes.

---

**Última Atualização:** 20 de Novembro de 2025
**Versão:** 2.0 (Com todas as anotações e Aspects)
