package com.qsmart.config;

import com.qsmart.model.*;
import com.qsmart.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final ServiceCenterRepository centerRepository;
    private final DepartmentRepository departmentRepository;
    private final ServiceCounterRepository counterRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final QueueTokenRepository tokenRepository;
    private final TokenHistoryRepository historyRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           ServiceCenterRepository centerRepository,
                           DepartmentRepository departmentRepository,
                           ServiceCounterRepository counterRepository,
                           DoctorAvailabilityRepository doctorAvailabilityRepository,
                           QueueTokenRepository tokenRepository,
                           TokenHistoryRepository historyRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.centerRepository = centerRepository;
        this.departmentRepository = departmentRepository;
        this.counterRepository = counterRepository;
        this.doctorAvailabilityRepository = doctorAvailabilityRepository;
        this.tokenRepository = tokenRepository;
        this.historyRepository = historyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already seeded. Skipping initial data setup.");
            return;
        }

        log.info("Seeding initial demo data for QSmart Virtual Queue System...");

        // 1. Create Default Users
        User admin = userRepository.save(new User(
                "System Administrator",
                "admin@qsmart.com",
                "+919876500001",
                passwordEncoder.encode("Admin@123"),
                Role.ROLE_ADMIN
        ));

        User staff = userRepository.save(new User(
                "Rahul Verma (Reception Staff)",
                "staff.rahul@qsmart.com",
                "+919876500002",
                passwordEncoder.encode("Staff@123"),
                Role.ROLE_STAFF
        ));

        User doctor1 = userRepository.save(new User(
                "Dr. A. K. Sharma (MD General Medicine)",
                "dr.sharma@qsmart.com",
                "+919876500003",
                passwordEncoder.encode("Doctor@123"),
                Role.ROLE_DOCTOR
        ));

        User doctor2 = userRepository.save(new User(
                "Dr. Priya Sen (Cardiologist)",
                "dr.priya@qsmart.com",
                "+919876500004",
                passwordEncoder.encode("Doctor@123"),
                Role.ROLE_DOCTOR
        ));

        User patient1 = userRepository.save(new User(
                "Amit Kumar",
                "patient.amit@qsmart.com",
                "+919876500005",
                passwordEncoder.encode("Patient@123"),
                Role.ROLE_PATIENT
        ));

        User patient2 = userRepository.save(new User(
                "Sneha Roy",
                "patient.sneha@qsmart.com",
                "+919876500006",
                passwordEncoder.encode("Patient@123"),
                Role.ROLE_PATIENT
        ));

        // 2. Create Service Centers (Hospitals)
        ServiceCenter hospital1 = centerRepository.save(new ServiceCenter(
                "HOSPITAL01",
                "Apex City Multispecialty Hospital",
                "HOSPITAL",
                "Sector 14, Health City, New Delhi",
                "New Delhi",
                "+91-11-23456789",
                "08:00 AM - 08:00 PM"
        ));

        ServiceCenter hospital2 = centerRepository.save(new ServiceCenter(
                "METRO02",
                "Metro Care General Hospital",
                "HOSPITAL",
                "Civil Lines, Central Avenue, Mumbai",
                "Mumbai",
                "+91-22-87654321",
                "08:00 AM - 09:00 PM"
        ));

        // 3. Create Departments for HOSPITAL01
        Department opd = departmentRepository.save(new Department("OPD", "General Medicine (OPD)", hospital1, 10));
        Department card = departmentRepository.save(new Department("CARD", "Cardiology", hospital1, 15));
        Department ortho = departmentRepository.save(new Department("ORTHO", "Orthopedics & Joint Clinic", hospital1, 12));
        Department ped = departmentRepository.save(new Department("PED", "Pediatrics (Child Care)", hospital1, 10));

        // Create Departments for METRO02
        departmentRepository.save(new Department("OPD", "General OPD", hospital2, 10));
        departmentRepository.save(new Department("CARD", "Cardiac Care Unit", hospital2, 15));

        // 4. Create Service Counters
        ServiceCounter counter1 = counterRepository.save(new ServiceCounter(
                "Counter 1 (Room 101)", opd, staff, null
        ));
        ServiceCounter counter2 = counterRepository.save(new ServiceCounter(
                "Counter 2 (Room 102)", opd, null, doctor1
        ));
        ServiceCounter counter3 = counterRepository.save(new ServiceCounter(
                "Cardio Clinic Room 204", card, null, doctor2
        ));

        // 5. Create Doctor Availabilities
        doctorAvailabilityRepository.save(new DoctorAvailability(
                doctor1, opd, DoctorStatus.AVAILABLE, "Room 102"
        ));
        doctorAvailabilityRepository.save(new DoctorAvailability(
                doctor2, card, DoctorStatus.AVAILABLE, "Room 204"
        ));

        // 6. Create Initial Pre-populated Queue Tokens for OPD Today
        LocalDate today = LocalDate.now();

        // Token 1: SERVED (Walk-in)
        QueueToken t1 = new QueueToken(
                "OPD-001", 1, opd, null, "Kavita Rao", "+919811122233",
                AccessChannel.WALK_IN, PriorityCategory.NORMAL, null
        );
        t1.setStatus(TokenStatus.SERVED);
        t1.setAssignedCounter(counter1);
        t1.setServedBy(staff);
        t1.setCalledAt(LocalDateTime.now().minusMinutes(25));
        t1.setServedAt(LocalDateTime.now().minusMinutes(12));
        tokenRepository.save(t1);
        historyRepository.save(new TokenHistory(t1, null, TokenStatus.WAITING, staff, "Created walk-in"));
        historyRepository.save(new TokenHistory(t1, TokenStatus.WAITING, TokenStatus.CALLED, staff, "Called to Counter 1"));
        historyRepository.save(new TokenHistory(t1, TokenStatus.CALLED, TokenStatus.SERVED, staff, "Served"));

        // Token 2: CALLED (Smartphone - Sneha)
        QueueToken t2 = new QueueToken(
                "OPD-002", 2, opd, patient2, patient2.getName(), patient2.getPhone(),
                AccessChannel.SMARTPHONE, PriorityCategory.NORMAL, null
        );
        t2.setStatus(TokenStatus.CALLED);
        t2.setAssignedCounter(counter2);
        t2.setServedBy(doctor1);
        t2.setCalledAt(LocalDateTime.now().minusMinutes(4));
        tokenRepository.save(t2);
        counter2.setStatus(CounterStatus.BUSY);
        counter2.setCurrentTokenId(t2.getId());
        counterRepository.save(counter2);
        historyRepository.save(new TokenHistory(t2, null, TokenStatus.WAITING, patient2, "Booked via Smartphone"));
        historyRepository.save(new TokenHistory(t2, TokenStatus.WAITING, TokenStatus.CALLED, doctor1, "Called to Room 102"));

        // Token 3: WAITING (Smartphone - Amit)
        QueueToken t3 = new QueueToken(
                "OPD-003", 3, opd, patient1, patient1.getName(), patient1.getPhone(),
                AccessChannel.SMARTPHONE, PriorityCategory.NORMAL, null
        );
        t3.setStatus(TokenStatus.WAITING);
        tokenRepository.save(t3);
        historyRepository.save(new TokenHistory(t3, null, TokenStatus.WAITING, patient1, "Booked via Smartphone"));

        // Token 4: WAITING (SMS from Keypad Phone)
        QueueToken t4 = new QueueToken(
                "OPD-004", 4, opd, null, "SMS User (3210)", "+919876543210",
                AccessChannel.SMS, PriorityCategory.NORMAL, null
        );
        t4.setStatus(TokenStatus.WAITING);
        tokenRepository.save(t4);
        historyRepository.save(new TokenHistory(t4, null, TokenStatus.WAITING, null, "Booked via SMS command"));

        // Token 5: WAITING (Walk-in - Senior Citizen)
        QueueToken t5 = new QueueToken(
                "OPD-005", 5, opd, null, "Ramesh Patel (Senior Citizen)", "+919822233344",
                AccessChannel.WALK_IN, PriorityCategory.SENIOR_CITIZEN, "Age 72, verified by reception"
        );
        t5.setStatus(TokenStatus.WAITING);
        tokenRepository.save(t5);
        historyRepository.save(new TokenHistory(t5, null, TokenStatus.WAITING, staff, "Assisted Walk-in registered"));

        log.info("QSmart demo dataset successfully initialized!");
    }
}
