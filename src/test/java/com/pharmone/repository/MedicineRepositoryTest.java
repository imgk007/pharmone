package com.pharmone.repository;

import com.pharmone.model.Medicine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
public class MedicineRepositoryTest {
    @Autowired
    private MedicineRepository medicineRepository;

    @Test
    void shouldFindMedicineByPartialNameIgnoreCase() {

        medicineRepository.save(createValidMedicine("Paracetamol"));
        medicineRepository.save(createValidMedicine("Aspirin"));

        Pageable pageable= PageRequest.of(0, 10);
        Page<Medicine> medicinePage=medicineRepository.findByMedicineNameContainingIgnoreCase("para",pageable);

        List<Medicine> results=medicinePage.getContent();
        assertEquals(1,results.size(),"Should find exactly one medicine matching 'para'");
        assertEquals("Paracetamol",results.get(0).getMedicineName());

        //asserting page metadata
        assertEquals(1,medicinePage.getTotalElements());
        assertEquals(1, medicinePage.getTotalPages());
    }

    //helper method to create valid medicine object to avoid jakarta validation ConstraintVioliatinexception while executing test class
    private Medicine createValidMedicine(String name) {
        Medicine m=new Medicine();
        m.setMedicineName(name);
        m.setMedicinePrice(new BigDecimal("50.0"));
        m.setQuantity(100);
        m.setCategory("General");
        m.setExpiryDate(LocalDate.now().plusYears(1));
        return m;
    }
}
