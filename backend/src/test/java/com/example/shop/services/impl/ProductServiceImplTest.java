package com.example.shop.services.impl;

import com.example.shop.dtos.common.PageResponse;
import com.example.shop.dtos.product.ProductCardResponse;
import com.example.shop.dtos.product.ProductFilter;
import com.example.shop.dtos.product.ProductFiltersResponse;
import com.example.shop.dtos.product.ProductRequest;
import com.example.shop.dtos.product.ProductResponse;
import com.example.shop.dtos.product.ProductSort;
import com.example.shop.dtos.product.ProductSpecRequest;
import com.example.shop.exceptions.ResourceNotFoundException;
import com.example.shop.mappers.ProductMapper;
import com.example.shop.models.entities.Brand;
import com.example.shop.models.entities.Category;
import com.example.shop.models.entities.Product;
import com.example.shop.models.entities.ProductSpec;
import com.example.shop.models.enums.CategoryEnum;
import com.example.shop.repositories.BrandRepository;
import com.example.shop.repositories.CategoryRepository;
import com.example.shop.repositories.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    private static final String OLD_IMAGE = "http://localhost:9000/shop-files/client_old.jpg";
    private static final String NEW_IMAGE = "http://localhost:9000/shop-files/client_new.jpg";

    @Mock
    private ProductRepository productRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BrandRepository brandRepository;
    @Mock
    private ProductMapper productMapper;
    @Mock
    private ImageUploaderService imageUploaderService;

    @InjectMocks
    private ProductServiceImpl productService;

    @Nested
    @DisplayName("Каталог")
    class Search {

        @Test
        @DisplayName("Найденные в базе данных товары возвращаются карточками вместе с данными о странице")
        void found() {
            Product product = product(null);
            ProductCardResponse card = new ProductCardResponse();
            when(productRepository.findAll(any(Specification.class), any(Pageable.class)))
                    .thenAnswer(invocation -> new PageImpl<>(List.of(product), invocation.getArgument(1), 1));
            when(productMapper.toCardResponse(product)).thenReturn(card);

            PageResponse<ProductCardResponse> response = productService.search(emptyFilter(), ProductSort.NEW, 0, 24);

            assertThat(response.getContent()).containsExactly(card);
            assertThat(response.getPage()).isZero();
            assertThat(response.getSize()).isEqualTo(24);
            assertThat(response.getTotalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("Сортировка по новизне запрашивает в базе данных сначала самые новые товары")
        void sortNew() {
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            productService.search(emptyFilter(), ProductSort.NEW, 0, 24);

            verify(productRepository).findAll(any(Specification.class),
                    argThat((Pageable pageable) -> pageable.getSort().equals(Sort.by(Sort.Direction.DESC, "createdAt", "id"))));
        }

        @Test
        @DisplayName("Сортировка по цене запрашивает в базе данных сначала дешёвые товары")
        void sortPriceAsc() {
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            productService.search(emptyFilter(), ProductSort.PRICE_ASC, 0, 24);

            verify(productRepository).findAll(any(Specification.class),
                    argThat((Pageable pageable) -> pageable.getSort().equals(Sort.by(Sort.Order.asc("price"), Sort.Order.desc("id")))));
        }

        @Test
        @DisplayName("При сортировке по скидке порядок задаёт сам запрос к базе данных, а не параметры страницы")
        void sortDiscount() {
            when(productRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

            productService.search(emptyFilter(), ProductSort.DISCOUNT, 0, 24);

            verify(productRepository).findAll(any(Specification.class),
                    argThat((Pageable pageable) -> pageable.getSort().isUnsorted()));
        }

        @Test
        @DisplayName("Минимальная цена больше максимальной отклоняется без запроса к базе данных")
        void minPriceGreaterThanMax() {
            ProductFilter filter = ProductFilter.builder()
                    .minPrice(new BigDecimal("60000"))
                    .maxPrice(new BigDecimal("20000"))
                    .build();

            assertThatThrownBy(() -> productService.search(filter, ProductSort.NEW, 0, 24))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Min price cannot be greater than max price");
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Размер страницы больше 48 отклоняется без запроса к базе данных")
        void sizeTooLarge() {
            assertThatThrownBy(() -> productService.search(emptyFilter(), ProductSort.NEW, 0, 49))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Page size must be from 1 to 48");
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Номер страницы, при котором смещение не помещается в int, отклоняется без запроса к базе данных")
        void pageTooLarge() {
            assertThatThrownBy(() -> productService.search(emptyFilter(), ProductSort.NEW, Integer.MAX_VALUE, 48))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Page is too large");
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Цена больше допустимой отклоняется без запроса к базе данных")
        void priceOutOfRange() {
            ProductFilter filter = ProductFilter.builder().minPrice(new BigDecimal("10000000000")).build();

            assertThatThrownBy(() -> productService.search(filter, ProductSort.NEW, 0, 24))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Min price must be from 0 to 9999999999.99 with up to 2 decimals");
            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("Отрицательный номер страницы отклоняется без запроса к базе данных")
        void negativePage() {
            assertThatThrownBy(() -> productService.search(emptyFilter(), ProductSort.NEW, -1, 24))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Page cannot be negative");
            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("Варианты фильтров")
    class GetFilters {

        @Test
        @DisplayName("Бренды и цвета загружаются из базы данных для выбранной категории")
        void byCategory() {
            when(productRepository.findBrandNames(CategoryEnum.PHONES)).thenReturn(List.of("Lumen", "Nord"));
            when(productRepository.findColors(CategoryEnum.PHONES)).thenReturn(List.of("Чёрный"));

            ProductFiltersResponse response = productService.getFilters(CategoryEnum.PHONES);

            assertThat(response.getBrands()).containsExactly("Lumen", "Nord");
            assertThat(response.getColors()).containsExactly("Чёрный");
        }
    }

    @Nested
    @DisplayName("Похожие товары")
    class GetRelated {

        @Test
        @DisplayName("Товары той же категории без самого товара загружаются из базы данных и возвращаются карточками")
        void found() {
            Product product = product(null);
            Category category = new Category();
            product.setCategory(category);
            Product other = product(null);
            List<ProductCardResponse> cards = List.of(new ProductCardResponse());
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(productRepository.findTop4ByCategoryAndIdNotOrderByCreatedAtDescIdDesc(category, product.getId()))
                    .thenReturn(List.of(other));
            when(productMapper.toCardResponses(List.of(other))).thenReturn(cards);

            assertThat(productService.getRelated(product.getUuid())).isSameAs(cards);
        }

        @Test
        @DisplayName("Для несуществующего uuid возвращается ошибка «не найден»")
        void notFound() {
            UUID uuid = UUID.randomUUID();
            when(productRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getRelated(uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");
        }
    }

    @Nested
    @DisplayName("Получение товара")
    class GetByUuid {

        @Test
        @DisplayName("Найденный в базе данных товар возвращается вместе с характеристиками")
        void found() {
            Product product = product(null);
            ProductResponse expected = new ProductResponse();
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
            when(productMapper.toResponse(product)).thenReturn(expected);

            assertThat(productService.getByUuid(product.getUuid())).isSameAs(expected);
        }

        @Test
        @DisplayName("Для несуществующего uuid возвращается ошибка «не найден»")
        void notFound() {
            UUID uuid = UUID.randomUUID();
            when(productRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getByUuid(uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");
        }
    }

    @Nested
    @DisplayName("Создание товара")
    class Create {

        @Test
        @DisplayName("Запрос с новым фото и одновременным удалением фото отклоняется без изменений в базе данных и хранилище")
        void imageAndDeletion() {
            assertThatThrownBy(() -> productService.create(request(true), imageFile()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Cannot upload and delete image at the same time");
            verifyNoInteractions(productRepository, imageUploaderService);
        }

        @Test
        @DisplayName("Товар сохраняется в базе данных с новым uuid, датами, категорией и существующим брендом")
        void existingBrand() {
            ProductRequest request = request(false);
            Product product = new Product();
            Category category = new Category();
            Brand brand = brand("Nord");
            when(productMapper.toEntity(request)).thenReturn(product);
            when(categoryRepository.findByName(CategoryEnum.PHONES)).thenReturn(Optional.of(category));
            when(brandRepository.findByNameIgnoreCase("nord")).thenReturn(Optional.of(brand));

            productService.create(request, null);

            assertThat(product.getUuid()).isNotNull();
            assertThat(product.getCreatedAt()).isNotNull().isEqualTo(product.getUpdatedAt());
            assertThat(product.getCategory()).isSameAs(category);
            assertThat(product.getBrand()).isSameAs(brand);
            verify(brandRepository, never()).save(any());
            verify(productRepository).saveAndFlush(product);
            verifyNoInteractions(imageUploaderService);
        }

        @Test
        @DisplayName("Бренд, которого нет в базе данных, создаётся по названию из запроса")
        void newBrand() {
            ProductRequest request = request(false);
            request.setBrand("  Kova ");
            Product product = new Product();
            when(productMapper.toEntity(request)).thenReturn(product);
            when(categoryRepository.findByName(CategoryEnum.PHONES)).thenReturn(Optional.of(new Category()));
            when(brandRepository.findByNameIgnoreCase("Kova")).thenReturn(Optional.empty());
            when(brandRepository.save(any(Brand.class))).thenAnswer(invocation -> invocation.getArgument(0));

            productService.create(request, null);

            assertThat(product.getBrand().getName()).isEqualTo("Kova");
        }

        @Test
        @DisplayName("Цвет сохраняется с заглавной первой буквой и строчными остальными")
        void colorNormalized() {
            ProductRequest request = request(false);
            request.setColor(" СИНЕ-СЕРЫЙ ");
            Product product = new Product();
            when(productMapper.toEntity(request)).thenReturn(product);
            when(categoryRepository.findByName(CategoryEnum.PHONES)).thenReturn(Optional.of(new Category()));
            when(brandRepository.findByNameIgnoreCase("nord")).thenReturn(Optional.of(brand("Nord")));

            productService.create(request, null);

            assertThat(product.getColor()).isEqualTo("Сине-серый");
        }

        @Test
        @DisplayName("Характеристики сохраняются в порядке из запроса с номерами позиций")
        void specsOrdered() {
            ProductRequest request = request(false);
            Product product = new Product();
            when(productMapper.toEntity(request)).thenReturn(product);
            when(categoryRepository.findByName(CategoryEnum.PHONES)).thenReturn(Optional.of(new Category()));
            when(brandRepository.findByNameIgnoreCase("nord")).thenReturn(Optional.of(brand("Nord")));

            productService.create(request, null);

            assertThat(product.getSpecs())
                    .extracting(ProductSpec::getName, ProductSpec::getValue, ProductSpec::getPosition)
                    .containsExactly(
                            tuple("Диагональ", "6,1″", 0),
                            tuple("Память", "128 ГБ", 1));
            assertThat(product.getSpecs()).allMatch(spec -> spec.getProduct() == product);
        }

        @Test
        @DisplayName("Фото загружается в хранилище после сохранения товара в базе данных")
        void withImage() {
            ProductRequest request = request(false);
            Product product = new Product();
            MockMultipartFile image = imageFile();
            when(productMapper.toEntity(request)).thenReturn(product);
            when(categoryRepository.findByName(CategoryEnum.PHONES)).thenReturn(Optional.of(new Category()));
            when(brandRepository.findByNameIgnoreCase("nord")).thenReturn(Optional.of(brand("Nord")));
            when(imageUploaderService.uploadImage(image)).thenReturn(NEW_IMAGE);

            productService.create(request, image);

            assertThat(product.getImageUrl()).isEqualTo(NEW_IMAGE);
            InOrder order = inOrder(productRepository, imageUploaderService);
            order.verify(productRepository).saveAndFlush(product);
            order.verify(imageUploaderService).uploadImage(image);
        }
    }

    @Nested
    @DisplayName("Изменение товара")
    class Update {

        @Test
        @DisplayName("Для несуществующего uuid возвращается ошибка «не найден», хранилище не затрагивается")
        void notFound() {
            UUID uuid = UUID.randomUUID();
            when(productRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(uuid, request(false), imageFile()))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");
            verifyNoInteractions(imageUploaderService);
        }

        @Test
        @DisplayName("Старые характеристики заменяются характеристиками из запроса")
        void specsReplaced() {
            Product product = product(OLD_IMAGE);
            ProductSpec oldSpec = new ProductSpec();
            oldSpec.setName("Вес");
            product.getSpecs().add(oldSpec);
            ProductRequest request = request(false);
            stubUpdate(product, request);

            productService.update(product.getUuid(), request, null);

            assertThat(product.getSpecs()).extracting(ProductSpec::getName).containsExactly("Диагональ", "Память");
            assertThat(product.getImageUrl()).isEqualTo(OLD_IMAGE);
            verifyNoInteractions(imageUploaderService);
        }

        @Test
        @DisplayName("Новое фото загружается после сохранения изменений в базе данных, старый файл удаляется последним")
        void newImage() {
            Product product = product(OLD_IMAGE);
            ProductRequest request = request(false);
            MockMultipartFile image = imageFile();
            stubUpdate(product, request);
            when(imageUploaderService.uploadImage(image)).thenReturn(NEW_IMAGE);

            productService.update(product.getUuid(), request, image);

            assertThat(product.getImageUrl()).isEqualTo(NEW_IMAGE);
            InOrder order = inOrder(productRepository, imageUploaderService);
            order.verify(productRepository).saveAndFlush(product);
            order.verify(imageUploaderService).uploadImage(image);
            order.verify(imageUploaderService).deleteImage(OLD_IMAGE);
        }

        @Test
        @DisplayName("При удалении фото ссылка в базе данных обнуляется, файл удаляется из хранилища")
        void deleteImage() {
            Product product = product(OLD_IMAGE);
            ProductRequest request = request(true);
            stubUpdate(product, request);

            productService.update(product.getUuid(), request, null);

            assertThat(product.getImageUrl()).isNull();
            verify(imageUploaderService).deleteImage(OLD_IMAGE);
            verify(imageUploaderService, never()).uploadImage(any());
        }

        @Test
        @DisplayName("Ошибка удаления старого файла из хранилища не препятствует изменению товара")
        void oldFileNotDeleted() {
            Product product = product(OLD_IMAGE);
            ProductRequest request = request(false);
            MockMultipartFile image = imageFile();
            stubUpdate(product, request);
            when(imageUploaderService.uploadImage(image)).thenReturn(NEW_IMAGE);
            doThrow(new IllegalStateException("Image deletion failed")).when(imageUploaderService).deleteImage(OLD_IMAGE);

            productService.update(product.getUuid(), request, image);

            assertThat(product.getImageUrl()).isEqualTo(NEW_IMAGE);
        }
    }

    @Nested
    @DisplayName("Удаление товара")
    class Delete {

        @Test
        @DisplayName("Товар удаляется из базы данных, затем его фото удаляется из хранилища")
        void withImage() {
            Product product = product(OLD_IMAGE);
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));

            productService.delete(product.getUuid());

            InOrder order = inOrder(productRepository, imageUploaderService);
            order.verify(productRepository).delete(product);
            order.verify(productRepository).flush();
            order.verify(imageUploaderService).deleteImage(OLD_IMAGE);
        }

        @Test
        @DisplayName("Товар без фото удаляется без обращения к хранилищу")
        void withoutImage() {
            Product product = product(null);
            when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));

            productService.delete(product.getUuid());

            verify(productRepository).delete(product);
            verify(imageUploaderService, never()).deleteImage(anyString());
        }

        @Test
        @DisplayName("Для несуществующего uuid возвращается ошибка «не найден»")
        void notFound() {
            UUID uuid = UUID.randomUUID();
            when(productRepository.findByUuid(uuid)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.delete(uuid))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Product not found");
            verify(productRepository, never()).delete(any(Product.class));
        }
    }

    private void stubUpdate(Product product, ProductRequest request) {
        when(productRepository.findByUuid(product.getUuid())).thenReturn(Optional.of(product));
        when(categoryRepository.findByName(request.getCategory())).thenReturn(Optional.of(new Category()));
        when(brandRepository.findByNameIgnoreCase("nord")).thenReturn(Optional.of(brand("Nord")));
    }

    private static Product product(String imageUrl) {
        Product product = new Product();
        product.setId(1L);
        product.setUuid(UUID.randomUUID());
        product.setName("Смартфон Nord X1");
        product.setImageUrl(imageUrl);
        return product;
    }

    private static ProductFilter emptyFilter() {
        return ProductFilter.builder().build();
    }

    private static Brand brand(String name) {
        Brand brand = new Brand();
        brand.setName(name);
        return brand;
    }

    private static ProductRequest request(boolean deleteImage) {
        ProductRequest request = new ProductRequest();
        request.setName("Смартфон Nord X1");
        request.setSummary("6,1″ OLED · 128 ГБ");
        request.setCategory(CategoryEnum.PHONES);
        request.setBrand("nord");
        request.setPrice(new BigDecimal("54990.00"));
        request.setStock(12);
        request.setSpecs(List.of(spec("Диагональ", "6,1″"), spec("Память", "128 ГБ")));
        request.setDeleteImage(deleteImage);
        return request;
    }

    private static ProductSpecRequest spec(String name, String value) {
        ProductSpecRequest spec = new ProductSpecRequest();
        spec.setName(name);
        spec.setValue(value);
        return spec;
    }

    private static MockMultipartFile imageFile() {
        return new MockMultipartFile("image", "product.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }
}
