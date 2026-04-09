package com.pharmone;

import com.pharmone.controller.MedicineController;
import com.pharmone.model.Medicine;
import com.pharmone.service.MedicineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicineController.class)
@AutoConfigureMockMvc(addFilters = false)
public class MedicineControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicineService pharmacyService;

    @Test
    @WithMockUser(roles={"ADMIN"})
    void getAllMedicine_ShouldReturnList() throws Exception {
        Medicine medicine = new Medicine();
        medicine.setMedicineName("Test Med");

        Page<Medicine> page=new PageImpl<>(List.of(medicine));

        when(pharmacyService.getAllMedicines(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/medicines/all")
                        .param("page", "0")
                        .param("size", "10")
                        .with(httpBasic("admin", "password")) // Force credentials
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].medicineName").value("Test Med"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadImage_ShouldReturnSuccess() throws Exception {
        // 1. Arrange: Create a fake image file
        MockMultipartFile fakeImage = new MockMultipartFile(
                "file",                      // The parameter name in your controller
                "paracetamol.png",           // Original filename
                "image/png",                 // Content type
                "fake image content".getBytes() // Content
        );

        // Mock the service response
        String expectedUrl = "Image Uploaded Successfully. Access it here: http://localhost:8080/content/medicines/1-paracetamol.png";
        when(pharmacyService.uploadImage(eq(1L), any(MultipartFile.class))).thenReturn(expectedUrl);

        // 2. Act & Assert
        mockMvc.perform(multipart("/api/medicines/1/upload")
                        .file(fakeImage) // Pass the fake file here
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Image Uploaded Successfully")));
    }
}
