package com.ansh.smart_commerce.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.io.ByteArrayOutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ansh.smart_commerce.dto.CheckoutRequest;
import com.ansh.smart_commerce.dto.CheckoutResponse;
import com.ansh.smart_commerce.dto.OrderItemResponse;
import com.ansh.smart_commerce.dto.OrderResponse;
import com.ansh.smart_commerce.entity.Address;
import com.ansh.smart_commerce.entity.CartItem;
import com.ansh.smart_commerce.entity.Order;
import com.ansh.smart_commerce.entity.OrderItem;
import com.ansh.smart_commerce.entity.Payment;
import com.ansh.smart_commerce.entity.Product;
import com.ansh.smart_commerce.entity.User;
import com.ansh.smart_commerce.enums.OrderStatus;
import com.ansh.smart_commerce.enums.PaymentStatus;
import com.ansh.smart_commerce.exception.AddressNotFoundException;
import com.ansh.smart_commerce.exception.CartEmptyException;
import com.ansh.smart_commerce.exception.InsufficientStockException;
import com.ansh.smart_commerce.exception.OrderNotFoundException;
import com.ansh.smart_commerce.repository.AddressRepository;
import com.ansh.smart_commerce.repository.CartRepository;
import com.ansh.smart_commerce.repository.OrderRepository;
import com.ansh.smart_commerce.repository.PaymentRepository;
import com.ansh.smart_commerce.repository.ProductRepository;
import com.ansh.smart_commerce.security.SecurityHelper;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final PaymentRepository paymentRepository;
    private final SecurityHelper securityHelper;
    private final CouponService couponService;
    public OrderService(OrderRepository orderRepository,
                        CartRepository cartRepository,
                        ProductRepository productRepository,
                        AddressRepository addressRepository,
                        PaymentRepository paymentRepository,
                        SecurityHelper securityHelper,
                        CouponService couponService) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
        this.paymentRepository = paymentRepository;
        this.securityHelper = securityHelper;
        this.couponService = couponService;
    }

    @Transactional
    public CheckoutResponse checkout(CheckoutRequest request) {
        User user = securityHelper.getCurrentUser();
        log.info("Checkout initiated for user {} with method {}", user.getId(), request.getPaymentMethod());

        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new AddressNotFoundException(request.getAddressId()));

        if (address.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this shipping address");
        }

        List<CartItem> cartItems = cartRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new CartEmptyException("Cart is empty for user id: " + user.getId());
        }

        for (CartItem item : cartItems) {
            if (item.getProduct().getStock() < item.getQuantity()) {
                throw new InsufficientStockException(
                        item.getProduct().getName(), item.getQuantity(), item.getProduct().getStock());
            }
        }

        Order order = new Order(user, LocalDateTime.now(), 0.0, OrderStatus.PENDING, null, 0.0, 0.0);

        List<OrderItem> orderItems = new ArrayList<>();

double subtotal = 0.0;

for (CartItem cartItem : cartItems) {

    Product product = cartItem.getProduct();

    product.setStock(product.getStock() - cartItem.getQuantity());
    productRepository.save(product);

    double unitPrice = product.getCost();

    subtotal += unitPrice * cartItem.getQuantity();

    orderItems.add(
            new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    unitPrice
            )
    );
}

double finalAmount = subtotal;
double discount = 0.0;
String couponCode = null;

if (request.getCouponCode() != null &&
    !request.getCouponCode().isBlank()) {

    finalAmount = couponService.applyDiscount(
            request.getCouponCode(),
            subtotal
    );

    discount = subtotal - finalAmount;

    couponCode = request.getCouponCode().toUpperCase();
}

order.setSubtotal(subtotal);
order.setDiscountAmount(discount);
order.setCouponCode(couponCode);
order.setTotalAmount(finalAmount);

order.setShippingName(address.getFullName());
order.setShippingPhone(address.getPhoneNumber());

String formattedAddress = String.format("%s, %s%s, %s, %s - %s, %s",
        address.getHouseNumber(),
        address.getStreet(),
        (address.getLandmark() != null && !address.getLandmark().isBlank()) ? ", " + address.getLandmark() : "",
        address.getCity(),
        address.getState(),
        address.getPostalCode(),
        address.getCountry());
order.setShippingAddress(formattedAddress);

order.setOrderItems(orderItems);

Order savedOrder = orderRepository.save(order);
        cartRepository.deleteAll(cartItems);

        Payment payment = new Payment(
                savedOrder, request.getPaymentMethod(), PaymentStatus.PENDING,
                null, finalAmount, LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(payment);

        log.info("Checkout complete — order={}, payment={}, total={}",
                savedOrder.getId(), savedPayment.getId(), finalAmount);

        return new CheckoutResponse(
                savedOrder.getId(),
                savedPayment.getId(),
                request.getPaymentMethod(),
                PaymentStatus.PENDING,
                finalAmount,
                java.time.LocalDate.now().plusDays(5));
    }

    @Transactional
    public OrderResponse placeOrder(Long userId) {
        User user = securityHelper.getCurrentUser();
        log.info("Placing order for user {}", user.getId());

        List<CartItem> cartItems = cartRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            log.warn("Order placement failed — cart is empty for user {}", user.getId());
            throw new CartEmptyException("Cannot place order — cart is empty for user id: " + user.getId());
        }

        Order order = new Order(user, LocalDateTime.now(), 0.0, OrderStatus.PENDING, null, 0.0, 0.0);

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0.0;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQty = cartItem.getQuantity();

            if (product.getStock() < requestedQty) {
                log.warn("Insufficient stock for product '{}': requested={}, available={}",
                        product.getName(), requestedQty, product.getStock());
                throw new InsufficientStockException(product.getName(), requestedQty, product.getStock());
            }

            product.setStock(product.getStock() - requestedQty);
            productRepository.save(product);

            double unitPrice = product.getCost();
            double subtotal = unitPrice * requestedQty;
            total += subtotal;

            orderItems.add(new OrderItem(order, product, requestedQty, unitPrice));
        }

        order.setSubtotal(total);
        order.setDiscountAmount(0.0);
        order.setCouponCode(null);
        order.setTotalAmount(total);

        // Fetch default or first address for the user if available
        List<Address> userAddresses = addressRepository.findByUser(user);
        if (!userAddresses.isEmpty()) {
            Address address = userAddresses.stream()
                    .filter(Address::isDefault)
                    .findFirst()
                    .orElse(userAddresses.get(0));
            order.setShippingName(address.getFullName());
            order.setShippingPhone(address.getPhoneNumber());
            String formattedAddress = String.format("%s, %s%s, %s, %s - %s, %s",
                    address.getHouseNumber(),
                    address.getStreet(),
                    (address.getLandmark() != null && !address.getLandmark().isBlank()) ? ", " + address.getLandmark() : "",
                    address.getCity(),
                    address.getState(),
                    address.getPostalCode(),
                    address.getCountry());
            order.setShippingAddress(formattedAddress);
        } else {
            order.setShippingName(user.getName());
            order.setShippingPhone("N/A");
            order.setShippingAddress("No address selected");
        }

        order.setOrderItems(orderItems);
        Order savedOrder = orderRepository.save(order);

        cartRepository.deleteAll(cartItems);

        log.info("Order {} placed successfully for user {} — total: {}", savedOrder.getId(), user.getId(), total);
        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrderHistory(Long userId) {
        User user = securityHelper.getCurrentUser();
        log.info("Fetching order history for user {}", user.getId());
        return orderRepository.findByUser(user).stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        User user = securityHelper.getCurrentUser();
        log.info("Fetching order {} for user {}", orderId, user.getId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found with id: {}", orderId);
                    return new OrderNotFoundException(orderId);
                });

        if (order.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this order");
        }

        return mapToOrderResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        User user = securityHelper.getCurrentUser();
        log.info("Cancelling order {} for user {}", orderId, user.getId());
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("You do not own this order");
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            log.warn("Cancel rejected — order {} is in status {}", orderId, order.getStatus());
            throw new RuntimeException(
                    "Order " + orderId + " cannot be cancelled. Current status: " + order.getStatus());
        }

        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order {} cancelled and stock restored", orderId);
        return mapToOrderResponse(saved);
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getOrderItems().stream()
                .map(item -> new OrderItemResponse(
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity(),
                        item.getProduct().getId(),
                        item.getProduct().getImageUrl(),
                        item.getPrice() * item.getQuantity()))
                .toList();

        OrderResponse response = new OrderResponse(
                order.getId(),
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCouponCode(),
                order.getSubtotal(),
                order.getDiscountAmount(),
                items);

        response.setShippingName(order.getShippingName());
        response.setShippingPhone(order.getShippingPhone());
        response.setShippingAddress(order.getShippingAddress());

        // Look up payment details if available
        paymentRepository.findByOrder(order).ifPresent(payment -> {
            response.setPaymentMethod(payment.getPaymentMethod().name());
            response.setPaymentStatus(payment.getPaymentStatus().name());
            response.setTransactionId(payment.getTransactionId() != null ? payment.getTransactionId() : "N/A");
        });

        return response;
    }

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        Payment payment = paymentRepository.findByOrder(order).orElse(null);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, baos);
            document.open();

            // Fonts
            Font mainTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, java.awt.Color.BLACK);
            Font sectionTitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, java.awt.Color.DARK_GRAY);
            Font textBoldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, java.awt.Color.BLACK);
            Font textNormalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, java.awt.Color.BLACK);
            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9, java.awt.Color.GRAY);

            // 1. Header Section
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setSpacingAfter(20);

            PdfPCell cell1 = new PdfPCell(new Paragraph("SMART COMMERCE", mainTitleFont));
            cell1.setBorder(PdfPCell.NO_BORDER);
            cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell();
            cell2.setBorder(PdfPCell.NO_BORDER);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph invDetails = new Paragraph();
            invDetails.setAlignment(Element.ALIGN_RIGHT);
            String dateStr = order.getOrderDate().toLocalDate().toString();
            invDetails.add(new Phrase("Invoice Number: INV-" + dateStr.replace("-", "") + "-" + order.getId() + "\n", textBoldFont));
            invDetails.add(new Phrase("Order Number: #" + order.getId() + "\n", textNormalFont));
            invDetails.add(new Phrase("Invoice Date: " + dateStr + "\n", textNormalFont));
            cell2.addElement(invDetails);
            headerTable.addCell(cell2);
            document.add(headerTable);

            // Line Separator
            Paragraph line = new Paragraph("----------------------------------------------------------------------------------------------------------------------------------");
            line.setSpacingAfter(15);
            document.add(line);

            // 2. Customer Section (2 Columns)
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(20);

            PdfPCell billingCell = new PdfPCell();
            billingCell.setBorder(PdfPCell.NO_BORDER);
            Paragraph billingPara = new Paragraph();
            billingPara.add(new Phrase("Customer Info\n", sectionTitleFont));
            billingPara.add(new Phrase("Name: " + order.getUser().getName() + "\n", textNormalFont));
            billingPara.add(new Phrase("Email: " + order.getUser().getEmail() + "\n", textNormalFont));
            if (order.getShippingPhone() != null) {
                billingPara.add(new Phrase("Phone: " + order.getShippingPhone() + "\n", textNormalFont));
            }
            billingCell.addElement(billingPara);
            infoTable.addCell(billingCell);

            PdfPCell shippingCell = new PdfPCell();
            shippingCell.setBorder(PdfPCell.NO_BORDER);
            Paragraph shippingPara = new Paragraph();
            shippingPara.add(new Phrase("Shipping Address\n", sectionTitleFont));
            shippingPara.add(new Phrase(order.getShippingName() != null ? order.getShippingName() : order.getUser().getName(), textBoldFont));
            shippingPara.add(new Phrase("\n" + (order.getShippingAddress() != null ? order.getShippingAddress() : "No shipping address"), textNormalFont));
            shippingCell.addElement(shippingPara);
            infoTable.addCell(shippingCell);
            document.add(infoTable);

            // Line Separator
            document.add(line);

            // 3. Products Table
            PdfPTable productsTable = new PdfPTable(4);
            productsTable.setWidthPercentage(100);
            productsTable.setSpacingAfter(20);
            float[] columnWidths = {4f, 1.5f, 2f, 2f};
            productsTable.setWidths(columnWidths);

            // Table Headers
            String[] headers = {"Product", "Quantity", "Unit Price", "Subtotal"};
            for (String header : headers) {
                PdfPCell hCell = new PdfPCell(new Phrase(header, textBoldFont));
                hCell.setBackgroundColor(java.awt.Color.LIGHT_GRAY);
                hCell.setPadding(6);
                productsTable.addCell(hCell);
            }

            // Table Rows
            for (OrderItem item : order.getOrderItems()) {
                PdfPCell pNameCell = new PdfPCell(new Phrase(item.getProduct().getName(), textNormalFont));
                pNameCell.setPadding(6);
                productsTable.addCell(pNameCell);

                PdfPCell qtyCell = new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), textNormalFont));
                qtyCell.setPadding(6);
                qtyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                productsTable.addCell(qtyCell);

                PdfPCell priceCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", item.getPrice()), textNormalFont));
                priceCell.setPadding(6);
                priceCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(priceCell);

                PdfPCell subCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", item.getPrice() * item.getQuantity()), textNormalFont));
                subCell.setPadding(6);
                subCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                productsTable.addCell(subCell);
            }
            document.add(productsTable);

            // 4. Pricing & Totals
            PdfPTable pricingTable = new PdfPTable(2);
            pricingTable.setWidthPercentage(50);
            pricingTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pricingTable.setSpacingAfter(20);
            pricingTable.setWidths(new float[]{2.2f, 1.8f});

            // Subtotal
            pricingTable.addCell(createNoBorderCell("Subtotal:", textNormalFont, Element.ALIGN_LEFT));
            pricingTable.addCell(createNoBorderCell("₹" + String.format("%.2f", order.getSubtotal()), textNormalFont, Element.ALIGN_RIGHT));

            // Coupon
            if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
                pricingTable.addCell(createNoBorderCell("Coupon (" + order.getCouponCode() + "):", textNormalFont, Element.ALIGN_LEFT));
                pricingTable.addCell(createNoBorderCell("-₹" + String.format("%.2f", order.getDiscountAmount()), textNormalFont, Element.ALIGN_RIGHT));
            }

            // Shipping
            pricingTable.addCell(createNoBorderCell("Shipping:", textNormalFont, Element.ALIGN_LEFT));
            pricingTable.addCell(createNoBorderCell("FREE", textNormalFont, Element.ALIGN_RIGHT));

            // Grand Total
            PdfPCell totalLabelCell = new PdfPCell(new Phrase("Grand Total:", textBoldFont));
            totalLabelCell.setBorder(PdfPCell.TOP);
            totalLabelCell.setPadding(6);
            pricingTable.addCell(totalLabelCell);

            PdfPCell totalValCell = new PdfPCell(new Phrase("₹" + String.format("%.2f", order.getTotalAmount()), textBoldFont));
            totalValCell.setBorder(PdfPCell.TOP);
            totalValCell.setPadding(6);
            totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            pricingTable.addCell(totalValCell);

            document.add(pricingTable);

            // 5. Payment Details
            if (payment != null) {
                Paragraph paymentInfo = new Paragraph();
                paymentInfo.setSpacingAfter(25);
                paymentInfo.add(new Phrase("Payment Info\n", sectionTitleFont));
                paymentInfo.add(new Phrase("Method: " + payment.getPaymentMethod().name() + "\n", textNormalFont));
                paymentInfo.add(new Phrase("Status: " + payment.getPaymentStatus().name() + "\n", textNormalFont));
                paymentInfo.add(new Phrase("Transaction ID: " + (payment.getTransactionId() != null ? payment.getTransactionId() : "N/A") + "\n", textNormalFont));
                document.add(paymentInfo);
            }

            // Line Separator
            document.add(line);

            // 6. Footer
            Paragraph footer = new Paragraph("Thank you for shopping with Smart Commerce.\nThis invoice is system generated.", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

        } catch (DocumentException e) {
            log.error("Failed to generate PDF invoice", e);
            throw new RuntimeException("Error generating invoice PDF", e);
        } finally {
            document.close();
        }

        return baos.toByteArray();
    }

    private PdfPCell createNoBorderCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPadding(4);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }
}
