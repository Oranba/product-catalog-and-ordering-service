# Product Catalog and Ordering Service - Detailed Design

## 1. Domain Model

### 1.1 Product Domain

#### Entity Classes

- **Product**
    - `id`: Long (Primary Key)
    - `sku`: String (Unique)
    - `name`: String
    - `description`: String
    - `price`: BigDecimal
    - `categoryId`: Long
    - `inventory`: Integer
    - `imageUrl`: String
    - `isActive`: Boolean
    - `createdAt`: LocalDateTime
    - `updatedAt`: LocalDateTime

- **Category**
    - `id`: Long (Primary Key)
    - `name`: String
    - `description`: String
    - `parentCategoryId`: Long (Optional)
    - `createdAt`: LocalDateTime
    - `updatedAt`: LocalDateTime

### 1.2 Order Domain

#### Entity Classes

- **Order**
    - `id`: Long (Primary Key)
    - `orderNumber`: String (Unique)
    - `customerId`: Long
    - `orderStatus`: Enum (CREATED, PAID, SHIPPED, DELIVERED, CANCELLED)
    - `totalAmount`: BigDecimal
    - `shippingAddress`: String
    - `billingAddress`: String
    - `createdAt`: LocalDateTime
    - `updatedAt`: LocalDateTime

- **OrderItem**
    - `id`: Long (Primary Key)
    - `orderId`: Long (Foreign Key)
    - `productId`: Long (Foreign Key)
    - `quantity`: Integer
    - `priceAtOrder`: BigDecimal
    - `createdAt`: LocalDateTime
    - `updatedAt`: LocalDateTime

## 2. Component Design

### 2.1 API Layer (Controllers)

#### ProductController

- `GET /api/products` - List all products with pagination and filtering
- `GET /api/products/{id}` - Get product by ID
- `POST /api/products` - Create new product
- `PUT /api/products/{id}` - Update product
- `DELETE /api/products/{id}` - Delete product (logical delete)
- `GET /api/products/category/{categoryId}` - Get products by category

#### CategoryController

- `GET /api/categories` - List all categories
- `GET /api/categories/{id}` - Get category by ID
- `POST /api/categories` - Create new category
- `PUT /api/categories/{id}` - Update category
- `DELETE /api/categories/{id}` - Delete category

#### OrderController

- `GET /api/orders` - List all orders with pagination and filtering
- `GET /api/orders/{id}` - Get order by ID
- `POST /api/orders` - Create new order
- `PUT /api/orders/{id}/status` - Update order status
- `GET /api/orders/customer/{customerId}` - Get orders by customer

### 2.2 Service Layer

#### ProductService

- `findAllProducts(Pageable pageable, Map<String, String> filters)`
- `findProductById(Long id)`
- `createProduct(ProductDTO product)`
- `updateProduct(Long id, ProductDTO product)`
- `deleteProduct(Long id)`
- `findProductsByCategory(Long categoryId)`
- `updateInventory(Long productId, int quantity)`

#### CategoryService

- `findAllCategories()`
- `findCategoryById(Long id)`
- `createCategory(CategoryDTO category)`
- `updateCategory(Long id, CategoryDTO category)`
- `deleteCategory(Long id)`

#### OrderService

- `findAllOrders(Pageable pageable, Map<String, String> filters)`
- `findOrderById(Long id)`
- `createOrder(OrderDTO order)`
- `updateOrderStatus(Long id, OrderStatus status)`
- `findOrdersByCustomer(Long customerId)`

### 2.3 Repository Layer

#### ProductRepository

- `findAll(Pageable pageable)`
- `findById(Long id)`
- `save(Product product)`
- `findByIsActiveTrue()`
- `findByCategoryId(Long categoryId)`
- `findByNameContaining(String name)`
- `findByPriceBetween(BigDecimal min, BigDecimal max)`

#### CategoryRepository

- `findAll()`
- `findById(Long id)`
- `save(Category category)`
- `findByParentCategoryId(Long parentId)`

#### OrderRepository

- `findAll(Pageable pageable)`
- `findById(Long id)`
- `save(Order order)`
- `findByCustomerId(Long customerId)`
- `findByOrderStatus(OrderStatus status)`
- `findByCreatedAtBetween(LocalDateTime start, LocalDateTime end)`

#### OrderItemRepository

- `findByOrderId(Long orderId)`
- `save(OrderItem orderItem)`

### 2.4 Event Producers and Consumers

#### OrderEventProducer

- `publishOrderCreatedEvent(Order order)`
- `publishOrderStatusChangedEvent(Order order, OrderStatus previousStatus)`

#### InventoryEventProducer

- `publishInventoryChangedEvent(Long productId, int quantityChange)`

#### OrderEventConsumer

- `handleOrderPaidEvent(OrderPaidEvent event)`
- `handleOrderCancelledEvent(OrderCancelledEvent event)`

### 2.5 Caching Implementation

#### CacheConfig

- Configuration for Spring Cache
- Cache manager setup
- TTL settings for different cache regions

#### Cached Operations

- Product listings
- Category hierarchy
- Product details by ID
- Cache eviction strategies on updates

### 2.6 Metrics and Monitoring

#### MetricsConfig

- Custom metrics for DB operations
- API response time metrics
- Cache hit/miss metrics

#### HealthIndicators

- Database connectivity
- Kafka connectivity
- Overall application health

## 3. Database Schema

```
┌─────────────────────┐       ┌─────────────────────┐
│     categories      │       │      products       │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ name                │       │ sku                 │
│ description         │       │ name                │
│ parent_category_id  │◄──────┤ category_id (FK)    │
│ created_at          │       │ description         │
│ updated_at          │       │ price               │
└─────────────────────┘       │ inventory           │
                              │ image_url           │
                              │ is_active           │
                              │ created_at          │
                              │ updated_at          │
                              └─────────────────────┘
                                        ▲
                                        │
                                        │
┌─────────────────────┐       ┌─────────────────────┐
│       orders        │       │     order_items     │
├─────────────────────┤       ├─────────────────────┤
│ id (PK)             │       │ id (PK)             │
│ order_number        │       │ order_id (FK)       │
│ customer_id         │       │ product_id (FK)     │
│ order_status        │◄──────┤ quantity            │
│ total_amount        │       │ price_at_order      │
│ shipping_address    │       │ created_at          │
│ billing_address     │       │ updated_at          │
│ created_at          │       └─────────────────────┘
│ updated_at          │
└─────────────────────┘
```

## 4. API Contract Details

### 4.1 Product API

#### GET /api/products

- **Query Parameters**:
    - `page`: Page number (default: 0)
    - `size`: Page size (default: 20)
    - `sort`: Sort field (default: "name,asc")
    - `category`: Filter by category ID
    - `minPrice`: Filter by minimum price
    - `maxPrice`: Filter by maximum price
    - `name`: Filter by name (partial match)
- **Response**:
    - Page of ProductDTO objects with total count and pagination info

#### POST /api/products

- **Request Body**: ProductDTO
- **Response**: Created ProductDTO with ID
- **Required Fields**: name, price, categoryId, inventory

### 4.2 Order API

#### POST /api/orders

- **Request Body**: OrderCreateDTO (containing customer info and order items)
- **Response**: Created OrderDTO with order number and status
- **Side Effects**:
    - Inventory updated
    - OrderCreatedEvent published to Kafka

#### PUT /api/orders/{id}/status

- **Path Parameter**: order ID
- **Request Body**: `{"status": "SHIPPED"}`
- **Response**: Updated OrderDTO
- **Side Effects**: OrderStatusChangedEvent published to Kafka

## 5. Event Schema

### 5.1 OrderCreatedEvent

```json
{
  "eventId": "uuid",
  "eventType": "ORDER_CREATED",
  "timestamp": "ISO-8601 timestamp",
  "payload": {
    "orderId": 123,
    "orderNumber": "ORD-12345",
    "customerId": 456,
    "totalAmount": 99.99,
    "items": [
      {
        "productId": 789,
        "quantity": 2,
        "price": 49.99
      }
    ]
  }
}
```

### 5.2 InventoryChangedEvent

```json
{
  "eventId": "uuid",
  "eventType": "INVENTORY_CHANGED",
  "timestamp": "ISO-8601 timestamp",
  "payload": {
    "productId": 789,
    "quantityChange": -2,
    "reason": "ORDER",
    "referenceId": "ORD-12345"
  }
}
```

## 6. Error Handling

### 6.1 Global Exception Handler

- ResourceNotFoundException (HTTP 404)
- ValidationException (HTTP 400)
- ConcurrencyException (HTTP 409)
- SystemException (HTTP 500)

### 6.2 Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid product data",
  "path": "/api/products",
  "timestamp": "ISO-8601 timestamp",
  "details": [
    "Price must be greater than zero",
    "Product name is required"
  ]
}
```

## 7. Caching Strategy

| Cache Name      | TTL | Eviction Policy | Eviction Triggers |
|-----------------|-----|-----------------|-------------------|
| products        | 30m | LRU             | Product updates   |
| categories      | 1h  | LRU             | Category updates  |
| product_details | 15m | LRU             | Product updates   |

## 8. Security Considerations (Future Implementation)

- JWT Authentication
- Role-based access control
- Input validation
- API rate limiting
