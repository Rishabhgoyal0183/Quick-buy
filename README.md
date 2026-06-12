# QuickBuy — Payment Gateway Demo

A full-stack payment gateway integration project built with **Spring Boot** and **Razorpay**. Demonstrates a complete end-to-end payment flow — product listing, order creation, payment processing, signature verification, and failure handling — without a cart or user account system.

---

## Live Flow

```
User visits index.html
        ↓
Browses products (fetched from DB via REST API)
        ↓
Clicks "Buy Now" → fills name + email
        ↓
Backend creates Razorpay order → saves Order(PENDING) in DB
        ↓
Razorpay popup opens in browser
        ↓
User pays (card / UPI / netbanking)
        ↓
Backend verifies HMAC-SHA256 signature
        ↓
Order status → PAID / FAILED in DB
        ↓
User redirected to success.html or failure.html
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.2 |
| Payment Gateway | Razorpay Java SDK 1.4.3 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Frontend | Vanilla HTML, CSS, JavaScript |
| Build Tool | Maven |
| Utilities | Lombok, Spring Validation |

---

## Project Structure

```
src/
├── main/
│   ├── java/Quickbuy/application/
│   │   ├── controller/
│   │   │   ├── ProductController.java      # GET  /api/products
│   │   │   ├── OrderController.java        # POST /api/orders/create
│   │   │   │                               # GET  /api/orders/{id}
│   │   │   └── PaymentController.java      # POST /api/payment/verify
│   │   │                                   # POST /api/payment/failure
│   │   ├── service/
│   │   │   ├── RazorpayService.java        # Razorpay SDK wrapper + HMAC verification
│   │   │   ├── OrderService.java           # Order creation logic
│   │   │   └── PaymentService.java         # Payment verify + failure logic
│   │   ├── entity/
│   │   │   ├── Product.java
│   │   │   ├── Order.java                  # Status: PENDING / PAID / FAILED
│   │   │   └── Payment.java                # Status: SUCCESS / FAILED
│   │   ├── repository/
│   │   │   ├── ProductRepository.java
│   │   │   ├── OrderRepository.java
│   │   │   └── PaymentRepository.java
│   │   ├── dto/
│   │   │   ├── ProductListResponse.java
│   │   │   ├── CreateOrderRequest.java
│   │   │   ├── CreateOrderResponse.java
│   │   │   ├── PaymentVerifyRequest.java
│   │   │   ├── PaymentVerifyResponse.java
│   │   │   └── PaymentFailureRequest.java
│   │   └── exception/
│   │       └── GlobalExceptionHandler.java
│   └── resources/
│       ├── static/
│       │   ├── index.html                  # Product listing + Razorpay popup
│       │   ├── success.html                # Payment success page
│       │   └── failure.html                # Payment failure page
│       ├── application.properties
│       └── data.sql                        # Product seed data
```

---

## Database Schema

### `products`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary key |
| name | VARCHAR | Product name |
| description | TEXT | Short description |
| price | DECIMAL(10,2) | Price in INR |
| image_url | VARCHAR | Product image URL |

### `orders`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary key |
| product_id | BIGINT | FK → products.id |
| razorpay_order_id | VARCHAR | Razorpay's order ID |
| customer_name | VARCHAR | From checkout form |
| customer_email | VARCHAR | From checkout form |
| amount | DECIMAL(10,2) | Price in INR |
| status | ENUM | PENDING / PAID / FAILED |
| created_at | TIMESTAMP | Auto-set on insert |

### `payments`
| Column | Type | Notes |
|---|---|---|
| id | BIGINT | Primary key |
| order_id | BIGINT | FK → orders.id |
| razorpay_payment_id | VARCHAR | Razorpay payment ID |
| razorpay_signature | VARCHAR | HMAC-SHA256 hash (null on failure) |
| status | ENUM | SUCCESS / FAILED |
| paid_at | TIMESTAMP | Auto-set on insert |

---

## API Reference

### `GET /api/products`
Returns all products. Called by `index.html` on page load.

**Response**
```json
[
  {
    "id": 1,
    "name": "Mechanical Keyboard",
    "description": "TKL layout, red switches, RGB backlit",
    "price": 2999.00,
    "imageUrl": "https://images.unsplash.com/..."
  }
]
```

---

### `POST /api/orders/create`
Creates a Razorpay order and saves a `PENDING` order in DB.

**Request**
```json
{
  "productId": 1,
  "customerName": "Rishabh Sharma",
  "customerEmail": "rishabh@example.com"
}
```

**Response**
```json
{
  "orderId": 4,
  "razorpayOrderId": "order_Xxxxxxxxxxxxxxxxx",
  "amount": 299900,
  "currency": "INR",
  "keyId": "rzp_test_xxxxxxxxxxxx",
  "productName": "Mechanical Keyboard",
  "customerName": "Rishabh Sharma",
  "customerEmail": "rishabh@example.com"
}
```

> `amount` is in **paise** (₹2999 × 100 = 299900). Razorpay requires paise.

---

### `POST /api/payment/verify`
Verifies Razorpay's HMAC-SHA256 signature after successful payment. Updates order status to `PAID`.

**Request**
```json
{
  "razorpayOrderId": "order_Xxxxxxxxxxxxxxxxx",
  "razorpayPaymentId": "pay_Xxxxxxxxxxxxxxxxx",
  "razorpaySignature": "abc123def456..."
}
```

**Response**
```json
{
  "status": "SUCCESS",
  "orderId": 4,
  "message": "Payment verified successfully"
}
```

> **How signature verification works:**
> `payload = razorpayOrderId + "|" + razorpayPaymentId`
> `expectedSignature = HMAC-SHA256(payload, razorpay_secret_key)`
> If `expectedSignature == razorpaySignature` → payment is genuine.

---

### `POST /api/payment/failure`
Called by frontend when Razorpay reports a payment failure. Updates order status to `FAILED` and saves a failed payment record.

**Request**
```json
{
  "razorpayOrderId": "order_Xxxxxxxxxxxxxxxxx",
  "razorpayPaymentId": "pay_Xxxxxxxxxxxxxxxxx",
  "failureReason": "Your card was declined"
}
```

---

### `GET /api/orders/{orderId}`
Fetches order details. Called by `success.html` to display confirmed order summary.

**Response**
```json
{
  "orderId": 4,
  "productName": "Mechanical Keyboard",
  "amount": 2999.00,
  "status": "PAID",
  "customerName": "Rishabh Sharma",
  "customerEmail": "rishabh@example.com",
  "createdAt": "2026-06-04T10:03:47"
}
```

---

## Getting Started

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL running on localhost:5432
- A [Razorpay account](https://dashboard.razorpay.com) (free)

---

### 1. Clone the repository

```bash
git clone https://github.com/Rishabhgoyal0183/Quick-buy.git
cd quickbuy
```

---

### 2. Create PostgreSQL database

```sql
psql -U postgres
CREATE DATABASE quickbuy;
```

---

### 3. Configure environment variables

Set these in your IntelliJ Run Configuration or as system environment variables:

```
RAZORPAY_KEY_ID       = rzp_test_xxxxxxxxxxxx
RAZORPAY_KEY_SECRET   = xxxxxxxxxxxxxxxxxxxxxxxx
DB_PASSWORD           = your_postgres_password
```

> Get your Razorpay test keys from:
> Dashboard → Settings → API Keys → Generate Test Key

---

### 4. Update `application.properties`

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/quickbuy
spring.datasource.username=postgres
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

razorpay.api.key=${RAZORPAY_KEY_ID}
razorpay.api.secret=${RAZORPAY_KEY_SECRET}
app.currency=INR
```

---

### 5. Run the application

```bash
mvn spring-boot:run
```

Or run `QuickBuyApplication.java` directly from IntelliJ.

---

### 6. Open in browser

```
http://localhost:8080/index.html
```

Products are seeded automatically from `data.sql` on first run.

---

## Testing Payments

This project runs entirely in **Razorpay test mode**. No real money is charged.

### Test cards

| Card Number | Result |
|---|---|
| `4111 1111 1111 1111` | Payment success |
| `4000 0000 0000 0002` | Card declined |
| `4000 0000 0000 0069` | Expired card |
| `4000 0000 0000 0127` | Incorrect CVV |

Use any future expiry date, any CVV, and `1234` as the OTP.

---

## Key Concepts Demonstrated

**HMAC-SHA256 Signature Verification**
Every successful payment from Razorpay includes a cryptographic signature. The backend recomputes this signature using the secret key and compares it — this prevents anyone from faking a payment by directly calling the verify API.

**Order lifecycle**
An order is created as `PENDING` when the user clicks Pay Now. It becomes `PAID` only after signature verification succeeds, or `FAILED` if payment is declined or verification fails.

**Frontend + Backend in one project**
Static HTML files are served directly from Spring Boot's `/resources/static/` directory — no separate frontend server needed.

**Separation of concerns**
Razorpay SDK calls are isolated in `RazorpayService`. Business logic lives in `OrderService` and `PaymentService`. Controllers only handle HTTP concerns.

---

## What's Not Included (intentionally)

- User authentication / login
- Shopping cart
- Order history page
- Email notifications
- Webhook handler (Razorpay async events)
- Admin panel

These are left out to keep the focus on the payment integration flow.

---

## Author

**Rishabh** — Associate Software Developer  
Built as a personal learning project for payment gateway integration with Spring Boot.

---

## License

This project is open source and available under the [MIT License](LICENSE).
