package com.pharmone;

import com.pharmone.exceptions.InsufficientStockException;
import com.pharmone.model.Medicine;
import com.pharmone.model.MedicineOrder;
import com.pharmone.repository.MedicineRepository;
import com.pharmone.repository.OrderRepository;
import com.pharmone.service.OrderService;
import com.pharmone.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private MedicineService pharmacyService;

    @InjectMocks
    private OrderService orderService;

    @Test
    void placeOrder_success() {
        Medicine mockMedicine = new Medicine();
        mockMedicine.setMedicineId(1L);
        mockMedicine.setMedicineName("Paracetamol");
        mockMedicine.setQuantity(10);
        mockMedicine.setMedicinePrice(new BigDecimal("50.0"));

        Authentication authentication = new UsernamePasswordAuthenticationToken("admin", "password");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        when(orderRepository.save(any(MedicineOrder.class))).thenAnswer(i->i.getArguments()[0]);

        MedicineOrder result=orderService.placeOrder(1L,2);
        assertNotNull(result);
        assertEquals(8,mockMedicine.getQuantity());
        assertEquals("admin",result.getCustomerName());
    }

    @Test
    void placeOrder_fail() {
        Medicine mockMedicine = new Medicine();
        mockMedicine.setMedicineId(1L);
        mockMedicine.setMedicineName("Paracetamol");
        mockMedicine.setQuantity(5);
        mockMedicine.setMedicinePrice(new BigDecimal("50.0"));

        when(medicineRepository.findById(1L)).thenReturn(Optional.of(mockMedicine));
        assertThrows(InsufficientStockException.class,()->orderService.placeOrder(1L,10));

        assertEquals(5,mockMedicine.getQuantity());
    }
    @Test
    void uploadImage_ShouldSaveFileToDisk(@TempDir Path tempDir) throws IOException {
        // 1. Arrange
        // We override the 'uploads' path for the test so it uses the temporary folder
        Medicine medicine = new Medicine();
        medicine.setMedicineId(1L);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(medicine));

        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

        // 2. Act
        String result = pharmacyService.uploadImage(1L, file);

        // 3. Assert
        assertNotNull(result);
        verify(medicineRepository, times(1)).save(any(Medicine.class));

        // Check if the file actually exists in our temp directory!
        Path expectedPath = Paths.get("uploads", "medicines", "1-test.jpg");
        assertTrue(Files.exists(expectedPath));

        // Cleanup: Delete the test folder created during the test
        Files.deleteIfExists(expectedPath);
    }
}
