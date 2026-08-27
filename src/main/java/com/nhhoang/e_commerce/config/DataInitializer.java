package com.nhhoang.e_commerce.config;

import com.nhhoang.e_commerce.dto.Enum.Role;
import com.nhhoang.e_commerce.entity.*;
import com.nhhoang.e_commerce.entity.Coupon.DiscountType;
import com.nhhoang.e_commerce.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AttributeRepository attributeRepository;

    @Autowired
    private ProductAttributesRepository productAttributesRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private com.nhhoang.e_commerce.service.ProductSearchService productSearchService;

    @Override
    public void run(String... args) throws Exception {
        seedUsers();
        seedCategoriesAndProducts();
        seedCoupons();
    }

    private void seedUsers() {
        if (userRepository.findByEmail("admin@gmail.com").isEmpty()) {
            User admin = new User();
            admin.setId(UUID.randomUUID().toString());
            admin.setName("System Admin");
            admin.setEmail("admin@gmail.com");
            admin.setPassword(passwordEncoder.encode("12345678"));
            admin.setPhone("0901234567");
            admin.setRole(Role.ADMIN);
            admin.setGender(User.Gender.MALE);
            admin.setAddress(List.of("123 Lê Lợi, Quận 1, TP. Hồ Chí Minh"));
            userRepository.save(admin);
            System.out.println("✅ Seeded Admin User: admin@gmail.com / 12345678");
        }

        if (userRepository.findByEmail("user@gmail.com").isEmpty()) {
            User customer = new User();
            customer.setId(UUID.randomUUID().toString());
            customer.setName("Nguyễn Văn Khách");
            customer.setEmail("user@gmail.com");
            customer.setPassword(passwordEncoder.encode("12345678"));
            customer.setPhone("0987654321");
            customer.setRole(Role.USER);
            customer.setGender(User.Gender.MALE);
            customer.setAddress(List.of("456 Nguyễn Thị Minh Khai, Quận 3, TP. Hồ Chí Minh"));
            userRepository.save(customer);
            System.out.println("✅ Seeded Customer User: user@gmail.com / 12345678");
        }
    }

    private void seedCategoriesAndProducts() {
        if (categoryRepository.count() == 0) {
            // Categories
            Category phoneCat = new Category();
            phoneCat.setId(UUID.randomUUID().toString());
            phoneCat.setCategoryName("Điện thoại & Máy tính bảng");
            phoneCat.setImage("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9");
            categoryRepository.save(phoneCat);

            Category laptopCat = new Category();
            laptopCat.setId(UUID.randomUUID().toString());
            laptopCat.setCategoryName("Laptop & Máy tính xách tay");
            laptopCat.setImage("https://images.unsplash.com/photo-1496181133206-80ce9b88a853");
            categoryRepository.save(laptopCat);

            Category accessoryCat = new Category();
            accessoryCat.setId(UUID.randomUUID().toString());
            accessoryCat.setCategoryName("Phụ kiện công nghệ");
            accessoryCat.setImage("https://images.unsplash.com/photo-1505740420928-5e560c06d30e");
            categoryRepository.save(accessoryCat);

            // Attributes
            Attributes colorAttr = new Attributes();
            colorAttr.setId(UUID.randomUUID().toString());
            colorAttr.setAttributeName("Màu sắc");
            attributeRepository.save(colorAttr);

            Attributes storageAttr = new Attributes();
            storageAttr.setId(UUID.randomUUID().toString());
            storageAttr.setAttributeName("Dung lượng");
            attributeRepository.save(storageAttr);

            // Products
            Product iphone = new Product();
            iphone.setId(UUID.randomUUID().toString());
            iphone.setProductName("iPhone 15 Pro Max 256GB");
            iphone.setSlug("iphone-15-pro-max-256gb");
            iphone.setDescription("Flagship cao cấp nhất của Apple với khung viền Titanium nhẹ bền, chip A17 Pro siêu mạnh mẽ và camera zoom 5x.");
            iphone.setPrice(29990000f);
            iphone.setStock(50);
            iphone.setStatus(Product.Status.AVAILABLE);
            iphone.setThumbImage("https://images.unsplash.com/photo-1695048133142-1a20484d2569");
            iphone.setImageUrls(List.of("https://images.unsplash.com/photo-1695048133142-1a20484d2569", "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5"));
            iphone.setCategory(phoneCat);
            productRepository.save(iphone);

            Product macbook = new Product();
            macbook.setId(UUID.randomUUID().toString());
            macbook.setProductName("MacBook Air M3 16GB 512GB");
            macbook.setSlug("macbook-air-m3-16gb-512gb");
            macbook.setDescription("Laptop mỏng nhẹ hiệu năng vượt trội với chip Apple M3, màn hình Liquid Retina sắc nét và thời lượng pin 18 giờ.");
            macbook.setPrice(32490000f);
            macbook.setStock(30);
            macbook.setStatus(Product.Status.AVAILABLE);
            macbook.setThumbImage("https://images.unsplash.com/photo-1517336714731-489689fd1ca8");
            macbook.setImageUrls(List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8"));
            macbook.setCategory(laptopCat);
            productRepository.save(macbook);

            Product airpods = new Product();
            airpods.setId(UUID.randomUUID().toString());
            airpods.setProductName("Tai nghe AirPods Pro 2 USB-C");
            airpods.setSlug("airpods-pro-2-usbc");
            airpods.setDescription("Tai nghe chống ồn chủ động đỉnh cao, âm thanh cá nhân hóa và cổng sạc USB-C tiện lợi.");
            airpods.setPrice(5990000f);
            airpods.setStock(100);
            airpods.setStatus(Product.Status.AVAILABLE);
            airpods.setThumbImage("https://images.unsplash.com/photo-1600294037681-c80b4cb5b434");
            airpods.setImageUrls(List.of("https://images.unsplash.com/photo-1600294037681-c80b4cb5b434"));
            airpods.setCategory(accessoryCat);
            productRepository.save(airpods);

            // Product Attributes
            ProductAttribute pa1 = new ProductAttribute();
            pa1.setId(UUID.randomUUID().toString());
            pa1.setProduct(iphone);
            pa1.setAttribute(colorAttr);
            pa1.setValue("Titan Tự Nhiên");
            productAttributesRepository.save(pa1);

            ProductAttribute pa2 = new ProductAttribute();
            pa2.setId(UUID.randomUUID().toString());
            pa2.setProduct(iphone);
            pa2.setAttribute(storageAttr);
            pa2.setValue("256GB");
            productAttributesRepository.save(pa2);

            productSearchService.indexProduct(iphone);
            productSearchService.indexProduct(macbook);
            productSearchService.indexProduct(airpods);

            System.out.println("✅ Seeded Categories & Sample Products (Indexed into Search Engine)");
        }
    }

    private void seedCoupons() {
        if (couponRepository.count() == 0) {
            Coupon c1 = Coupon.builder()
                    .code("WELCOME100")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(100000f)
                    .minOrderAmount(500000f)
                    .usageLimit(500)
                    .usedCount(0)
                    .active(true)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusMonths(6))
                    .build();
            couponRepository.save(c1);

            Coupon c2 = Coupon.builder()
                    .code("SUMMER20")
                    .discountType(DiscountType.PERCENTAGE)
                    .discountValue(20f)
                    .minOrderAmount(200000f)
                    .maxDiscountAmount(200000f)
                    .usageLimit(200)
                    .usedCount(0)
                    .active(true)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusMonths(3))
                    .build();
            couponRepository.save(c2);

            Coupon c3 = Coupon.builder()
                    .code("VIPFLASH")
                    .discountType(DiscountType.FIXED_AMOUNT)
                    .discountValue(500000f)
                    .minOrderAmount(2000000f)
                    .usageLimit(50)
                    .usedCount(0)
                    .active(true)
                    .startDate(LocalDateTime.now().minusDays(1))
                    .endDate(LocalDateTime.now().plusMonths(1))
                    .build();
            couponRepository.save(c3);

            System.out.println("✅ Seeded Coupons: WELCOME100, SUMMER20, VIPFLASH");
        }
    }
}
