package com.stms.service;

import com.stms.model.Certificate;
import com.stms.model.ComplianceCourse;
import com.stms.model.CourseLevel;
import com.stms.model.Department;
import com.stms.model.Employee;
import com.stms.model.Enrollment;
import com.stms.model.EnrollmentStatus;
import com.stms.model.SessionStatus;
import com.stms.model.SoftSkillsCourse;
import com.stms.model.TechnicalCourse;
import com.stms.model.Trainer;
import com.stms.model.TrainingCourse;
import com.stms.repository.CertificateRepository;
import com.stms.repository.CourseRepository;
import com.stms.repository.EmployeeRepository;
import com.stms.repository.EnrollmentRepository;
import com.stms.repository.SessionRepository;
import com.stms.repository.TrainerRepository;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;

/**
 * Handles persistent file storage and auto-seeding of initial realistic data.
 */
public class DataPersistenceService {
    private final String dataDirectory;

    public DataPersistenceService() {
        this("data");
    }

    public DataPersistenceService(String dataDirectory) {
        this.dataDirectory = dataDirectory;
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void loadOrSeedAll(EmployeeRepository employeeRepo,
                              TrainerRepository trainerRepo,
                              CourseRepository courseRepo,
                              SessionRepository sessionRepo,
                              EnrollmentRepository enrollmentRepo,
                              CertificateRepository certificateRepo) {
        com.stms.database.DatabaseManager db = com.stms.database.DatabaseManager.getInstance();
        db.initializeSchema();

        File empFile = new File(dataDirectory, "employees.csv");
        if (db.isDatabaseEmpty() && (!empFile.exists() || empFile.length() == 0)) {
            System.out.println("Database is empty. Seeding initial demonstration data via JDBC...");
            seedSampleData(employeeRepo, trainerRepo, courseRepo, sessionRepo, enrollmentRepo, certificateRepo);
            saveAll(employeeRepo, trainerRepo, courseRepo, sessionRepo, enrollmentRepo, certificateRepo);
        } else {
            loadAll(employeeRepo, trainerRepo, courseRepo, sessionRepo, enrollmentRepo, certificateRepo);
        }
    }

    public void saveAll(EmployeeRepository employeeRepo,
                        TrainerRepository trainerRepo,
                        CourseRepository courseRepo,
                        SessionRepository sessionRepo,
                        EnrollmentRepository enrollmentRepo,
                        CertificateRepository certificateRepo) {
        // 1. Primary Relational Storage via JDBC PreparedStatement Execution
        com.stms.database.DatabaseManager db = com.stms.database.DatabaseManager.getInstance();
        for (Employee e : employeeRepo.findAll()) {
            db.insertEmployee(e);
        }
        for (Trainer t : trainerRepo.findAll()) {
            db.insertTrainer(t);
        }
        for (TrainingCourse c : courseRepo.findAll()) {
            db.insertCourse(c);
        }
        for (com.stms.model.TrainingSession s : sessionRepo.findAll()) {
            db.insertSession(s);
        }
        for (Enrollment en : enrollmentRepo.findAll()) {
            db.insertEnrollment(en);
        }
        for (Certificate cert : certificateRepo.findAll()) {
            db.insertCertificate(cert);
        }

        // 2. CSV File Export Backup
        saveEmployees(employeeRepo);
        saveTrainers(trainerRepo);
        saveCourses(courseRepo);
        saveSessions(sessionRepo);
        saveEnrollments(enrollmentRepo);
        saveCertificates(certificateRepo);
    }

    public void loadAll(EmployeeRepository employeeRepo,
                        TrainerRepository trainerRepo,
                        CourseRepository courseRepo,
                        SessionRepository sessionRepo,
                        EnrollmentRepository enrollmentRepo,
                        CertificateRepository certificateRepo) {
        com.stms.database.DatabaseManager db = com.stms.database.DatabaseManager.getInstance();
        java.util.List<Employee> employees = db.loadAllEmployees();

        if (!employees.isEmpty()) {
            employees.forEach(employeeRepo::save);
            db.loadAllTrainers().forEach(trainerRepo::save);
            db.loadAllCourses().forEach(courseRepo::save);
            db.loadAllSessions().forEach(sessionRepo::save);
            db.loadAllEnrollments().forEach(enrollmentRepo::save);
            db.loadAllCertificates().forEach(certificateRepo::save);
            System.out.println("JDBC Database: Loaded all records from relational database tables successfully.");
        } else {
            // Fallback to CSV if DB had no rows
            loadEmployees(employeeRepo);
            loadTrainers(trainerRepo);
            loadCourses(courseRepo);
            loadSessions(sessionRepo);
            loadEnrollments(enrollmentRepo);
            loadCertificates(certificateRepo);
            System.out.println("Loaded records from local CSV storage.");
        }
    }

    // --- CSV SAVING ---

    private void saveEmployees(EmployeeRepository repo) {
        File file = new File(dataDirectory, "employees.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,firstName,lastName,email,phone,department,jobTitle,hireDate,active\n");
            for (Employee e : repo.findAll()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        clean(e.getId()), clean(e.getFirstName()), clean(e.getLastName()),
                        clean(e.getEmail()), clean(e.getPhone()), e.getDepartment().name(),
                        clean(e.getJobTitle()), e.getHireDate(), e.isActive()));
            }
        } catch (IOException ex) {
            System.err.println("Error saving employees: " + ex.getMessage());
        }
    }

    private void saveTrainers(TrainerRepository repo) {
        File file = new File(dataDirectory, "trainers.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,firstName,lastName,email,phone,specialization,yearsExp,hourlyRate,external\n");
            for (Trainer t : repo.findAll()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%d,%.2f,%s\n",
                        clean(t.getId()), clean(t.getFirstName()), clean(t.getLastName()),
                        clean(t.getEmail()), clean(t.getPhone()), clean(t.getSpecialization()),
                        t.getYearsOfExperience(), t.getHourlyRate(), t.isExternal()));
            }
        } catch (IOException ex) {
            System.err.println("Error saving trainers: " + ex.getMessage());
        }
    }

    private void saveCourses(CourseRepository repo) {
        File file = new File(dataDirectory, "courses.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("type,id,title,description,durationHours,level,maxCapacity,costPerSeat,specificAttr,flagAttr\n");
            for (TrainingCourse c : repo.findAll()) {
                String type = "TECHNICAL";
                String specificAttr = "";
                boolean flagAttr = false;

                if (c instanceof TechnicalCourse tc) {
                    type = "TECHNICAL";
                    specificAttr = tc.getPrimaryTechStack();
                    flagAttr = tc.isLabAssessmentRequired();
                } else if (c instanceof SoftSkillsCourse sc) {
                    type = "SOFT_SKILLS";
                    specificAttr = sc.getCoreCompetency();
                    flagAttr = sc.isWorkshopSessionRequired();
                } else if (c instanceof ComplianceCourse cc) {
                    type = "COMPLIANCE";
                    specificAttr = cc.getRegulatoryStandard();
                    flagAttr = cc.isMandatoryForEntireCompany();
                }

                writer.write(String.format("%s,%s,%s,%s,%d,%s,%d,%.2f,%s,%s\n",
                        type, clean(c.getId()), clean(c.getTitle()), clean(c.getDescription()),
                        c.getDurationHours(), c.getLevel().name(), c.getMaxCapacity(),
                        c.getCostPerSeat(), clean(specificAttr), flagAttr));
            }
        } catch (IOException ex) {
            System.err.println("Error saving courses: " + ex.getMessage());
        }
    }

    private void saveSessions(SessionRepository repo) {
        File file = new File(dataDirectory, "sessions.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,courseId,courseTitle,trainerId,trainerName,sessionDate,timeSlot,locationOrUrl,maxSeats,status\n");
            for (var s : repo.findAll()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%d,%s\n",
                        clean(s.getId()), clean(s.getCourseId()), clean(s.getCourseTitle()),
                        clean(s.getTrainerId()), clean(s.getTrainerName()), s.getSessionDate(),
                        clean(s.getTimeSlot()), clean(s.getLocationOrUrl()), s.getMaxSeats(),
                        s.getStatus().name()));
            }
        } catch (IOException ex) {
            System.err.println("Error saving sessions: " + ex.getMessage());
        }
    }

    private void saveEnrollments(EnrollmentRepository repo) {
        File file = new File(dataDirectory, "enrollments.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,sessionId,employeeId,employeeName,courseId,courseTitle,enrollmentDate,attended,score,status,certId,notes\n");
            for (Enrollment e : repo.findAll()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        clean(e.getId()), clean(e.getSessionId()), clean(e.getEmployeeId()),
                        clean(e.getEmployeeName()), clean(e.getCourseId()), clean(e.getCourseTitle()),
                        e.getEnrollmentDate(), e.isAttended(),
                        (e.getScore() != null ? e.getScore().toString() : "null"),
                        e.getStatus().name(), clean(e.getCertificateId()), clean(e.getNotes())));
            }
        } catch (IOException ex) {
            System.err.println("Error saving enrollments: " + ex.getMessage());
        }
    }

    private void saveCertificates(CertificateRepository repo) {
        File file = new File(dataDirectory, "certificates.csv");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("id,enrollmentId,employeeId,employeeName,courseId,courseTitle,courseCategory,issueDate,expiryDate,verificationCode\n");
            for (Certificate c : repo.findAll()) {
                writer.write(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
                        clean(c.getId()), clean(c.getEnrollmentId()), clean(c.getEmployeeId()),
                        clean(c.getEmployeeName()), clean(c.getCourseId()), clean(c.getCourseTitle()),
                        clean(c.getCourseCategory()), c.getIssueDate(),
                        (c.getExpiryDate() != null ? c.getExpiryDate().toString() : "null"),
                        clean(c.getVerificationCode())));
            }
        } catch (IOException ex) {
            System.err.println("Error saving certificates: " + ex.getMessage());
        }
    }

    // --- CSV LOADING ---

    private void loadEmployees(EmployeeRepository repo) {
        File file = new File(dataDirectory, "employees.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    Employee e = new Employee(parts[0], parts[1], parts[2], parts[3], parts[4],
                            Department.valueOf(parts[5]), parts[6], LocalDate.parse(parts[7]));
                    e.setActive(Boolean.parseBoolean(parts[8]));
                    repo.save(e);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading employees: " + ex.getMessage());
        }
    }

    private void loadTrainers(TrainerRepository repo) {
        File file = new File(dataDirectory, "trainers.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 9) {
                    Trainer t = new Trainer(parts[0], parts[1], parts[2], parts[3], parts[4],
                            parts[5], Integer.parseInt(parts[6]), Double.parseDouble(parts[7]),
                            Boolean.parseBoolean(parts[8]));
                    repo.save(t);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading trainers: " + ex.getMessage());
        }
    }

    private void loadCourses(CourseRepository repo) {
        File file = new File(dataDirectory, "courses.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    String type = parts[0];
                    String id = parts[1];
                    String title = parts[2];
                    String desc = parts[3];
                    int hours = Integer.parseInt(parts[4]);
                    CourseLevel level = CourseLevel.valueOf(parts[5]);
                    int capacity = Integer.parseInt(parts[6]);
                    double cost = Double.parseDouble(parts[7]);
                    String attr = parts[8];
                    boolean flag = Boolean.parseBoolean(parts[9]);

                    TrainingCourse c = CourseFactory.createCourse(
                            CourseFactory.CourseType.valueOf(type),
                            id, title, desc, hours, level, capacity, cost, attr, flag);
                    repo.save(c);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading courses: " + ex.getMessage());
        }
    }

    private void loadSessions(SessionRepository repo) {
        File file = new File(dataDirectory, "sessions.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    var s = new com.stms.model.TrainingSession(
                            parts[0], parts[1], parts[2], parts[3], parts[4],
                            LocalDate.parse(parts[5]), parts[6], parts[7],
                            Integer.parseInt(parts[8]), SessionStatus.valueOf(parts[9]));
                    repo.save(s);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading sessions: " + ex.getMessage());
        }
    }

    private void loadEnrollments(EnrollmentRepository repo) {
        File file = new File(dataDirectory, "enrollments.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 12) {
                    Enrollment e = new Enrollment(parts[0], parts[1], parts[2], parts[3],
                            parts[4], parts[5], LocalDate.parse(parts[6]));
                    e.setAttended(Boolean.parseBoolean(parts[7]));
                    if (!"null".equalsIgnoreCase(parts[8]) && !parts[8].isEmpty()) {
                        e.setScore(Double.parseDouble(parts[8]));
                    }
                    e.setStatus(EnrollmentStatus.valueOf(parts[9]));
                    if (!"null".equalsIgnoreCase(parts[10]) && !parts[10].isEmpty()) {
                        e.setCertificateId(parts[10]);
                    }
                    e.setNotes(parts[11]);
                    repo.save(e);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading enrollments: " + ex.getMessage());
        }
    }

    private void loadCertificates(CertificateRepository repo) {
        File file = new File(dataDirectory, "certificates.csv");
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",", -1);
                if (parts.length >= 10) {
                    Certificate c = new Certificate();
                    c.setId(parts[0]);
                    c.setEnrollmentId(parts[1]);
                    c.setEmployeeId(parts[2]);
                    c.setEmployeeName(parts[3]);
                    c.setCourseId(parts[4]);
                    c.setCourseTitle(parts[5]);
                    c.setCourseCategory(parts[6]);
                    c.setIssueDate(LocalDate.parse(parts[7]));
                    if (!"null".equalsIgnoreCase(parts[8]) && !parts[8].isEmpty()) {
                        c.setExpiryDate(LocalDate.parse(parts[8]));
                    }
                    c.setVerificationCode(parts[9]);
                    repo.save(c);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading certificates: " + ex.getMessage());
        }
    }

    private String clean(String input) {
        if (input == null) return "";
        return input.replace(",", ";").replace("\n", " ").trim();
    }

    /**
     * Seeds rich, realistic initial demonstration data if starting fresh.
     */
    private void seedSampleData(EmployeeRepository empRepo, TrainerRepository trainerRepo,
                                CourseRepository courseRepo, SessionRepository sessRepo,
                                EnrollmentRepository enrollmentRepo, CertificateRepository certRepo) {
        // 1. Employees
        Employee e1 = new Employee("EMP-101", "Alex", "Morgan", "alex.morgan@company.com", "+1-555-0101", Department.ENGINEERING, "Senior Software Engineer", LocalDate.now().minusMonths(24));
        Employee e2 = new Employee("EMP-102", "Samantha", "Ray", "samantha.ray@company.com", "+1-555-0102", Department.HUMAN_RESOURCES, "Talent Development Lead", LocalDate.now().minusMonths(18));
        Employee e3 = new Employee("EMP-103", "David", "Kim", "david.kim@company.com", "+1-555-0103", Department.FINANCE, "Financial Analyst", LocalDate.now().minusMonths(12));
        Employee e4 = new Employee("EMP-104", "Elena", "Rostova", "elena.rostova@company.com", "+1-555-0104", Department.ENGINEERING, "DevOps Engineer", LocalDate.now().minusMonths(8));
        Employee e5 = new Employee("EMP-105", "Marcus", "Johnson", "marcus.j@company.com", "+1-555-0105", Department.MARKETING, "Product Marketing Specialist", LocalDate.now().minusMonths(30));
        Employee e6 = new Employee("EMP-106", "Chloe", "Bennett", "chloe.b@company.com", "+1-555-0106", Department.LEGAL_COMPLIANCE, "Compliance Officer", LocalDate.now().minusMonths(6));

        empRepo.save(e1);
        empRepo.save(e2);
        empRepo.save(e3);
        empRepo.save(e4);
        empRepo.save(e5);
        empRepo.save(e6);

        // 2. Trainers
        Trainer t1 = new Trainer("TRN-201", "Dr. Robert", "Chen", "robert.chen@training.org", "+1-555-0201", "Advanced Java & Cloud Architectures", 12, 120.0, true);
        Trainer t2 = new Trainer("TRN-202", "Sarah", "Jenkins", "sarah.jenkins@company.com", "+1-555-0202", "Corporate Leadership & Agile Coaching", 8, 85.0, false);
        Trainer t3 = new Trainer("TRN-203", "Michael", "Vance", "michael.vance@legalgroup.com", "+1-555-0203", "Cybersecurity & Data Privacy (GDPR/ISO)", 15, 140.0, true);

        trainerRepo.save(t1);
        trainerRepo.save(t2);
        trainerRepo.save(t3);

        // 3. Courses
        TrainingCourse c1 = new TechnicalCourse("CRS-301", "Design Patterns & Object-Oriented Java", "Comprehensive deep-dive into clean architecture, SOLID principles, and design patterns.", 24, CourseLevel.ADVANCED, 25, 450.0, "Java 21 / Spring / Clean Architecture", true);
        TrainingCourse c2 = new SoftSkillsCourse("CRS-302", "High-Impact Agile Leadership", "Mastering team psychological safety, active coaching, and rapid feedback loops.", 16, CourseLevel.INTERMEDIATE, 20, 300.0, "Empathetic Leadership", true);
        TrainingCourse c3 = new ComplianceCourse("CRS-303", "Enterprise Information Security & GDPR", "Annual mandatory cyber defense, phishing mitigation, and data governance compliance.", 8, CourseLevel.BEGINNER, 50, 150.0, "ISO 27001 / GDPR Article 32", true);
        TrainingCourse c4 = new TechnicalCourse("CRS-304", "Docker, Kubernetes & Cloud Native CI/CD", "Production-grade microservices deployment and container orchestration.", 32, CourseLevel.ADVANCED, 20, 600.0, "Kubernetes / Helm / AWS", true);

        courseRepo.save(c1);
        courseRepo.save(c2);
        courseRepo.save(c3);
        courseRepo.save(c4);

        // 4. Sessions
        var s1 = new com.stms.model.TrainingSession("SES-401", c1.getId(), c1.getTitle(), t1.getId(), t1.getFullName(), LocalDate.now().minusDays(10), "09:00 - 17:00", "Tech Lab Room 302", 20, SessionStatus.COMPLETED);
        var s2 = new com.stms.model.TrainingSession("SES-402", c3.getId(), c3.getTitle(), t3.getId(), t3.getFullName(), LocalDate.now().minusDays(5), "10:00 - 14:00", "Zoom Virtual Auditorium", 50, SessionStatus.COMPLETED);
        var s3 = new com.stms.model.TrainingSession("SES-403", c2.getId(), c2.getTitle(), t2.getId(), t2.getFullName(), LocalDate.now().plusDays(7), "09:30 - 16:30", "Executive Suite Room 101", 15, SessionStatus.SCHEDULED);

        sessRepo.save(s1);
        sessRepo.save(s2);
        sessRepo.save(s3);

        // 5. Enrollments & Certificates
        // Alex completed SES-401 with 92% (Pass)
        Enrollment en1 = new Enrollment("ENR-501", s1.getId(), e1.getId(), e1.getFullName(), c1.getId(), c1.getTitle(), LocalDate.now().minusDays(20));
        en1.setAttended(true);
        en1.setScore(92.0);
        en1.setStatus(EnrollmentStatus.PASSED);
        en1.setCertificateId("CERT-901");
        enrollmentRepo.save(en1);

        Certificate cert1 = new Certificate("CERT-901", en1.getId(), e1.getId(), e1.getFullName(), c1.getId(), c1.getTitle(), c1.getCategoryName(), LocalDate.now().minusDays(9), c1.getCertificateValidityMonths());
        certRepo.save(cert1);

        // Samantha completed SES-402 (GDPR Compliance) with 95% (Pass)
        Enrollment en2 = new Enrollment("ENR-502", s2.getId(), e2.getId(), e2.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
        en2.setAttended(true);
        en2.setScore(95.0);
        en2.setStatus(EnrollmentStatus.PASSED);
        en2.setCertificateId("CERT-902");
        enrollmentRepo.save(en2);

        Certificate cert2 = new Certificate("CERT-902", en2.getId(), e2.getId(), e2.getFullName(), c3.getId(), c3.getTitle(), c3.getCategoryName(), LocalDate.now().minusDays(4), c3.getCertificateValidityMonths());
        certRepo.save(cert2);

        // David enrolled in SES-402, attended but scored 72% (Compliance requires 85% -> Failed)
        Enrollment en3 = new Enrollment("ENR-503", s2.getId(), e3.getId(), e3.getFullName(), c3.getId(), c3.getTitle(), LocalDate.now().minusDays(14));
        en3.setAttended(true);
        en3.setScore(72.0);
        en3.setStatus(EnrollmentStatus.FAILED);
        en3.setNotes("Scored 72%. Below compliance threshold of 85%. Retake mandatory.");
        enrollmentRepo.save(en3);

        // Marcus enrolled in upcoming SES-403
        Enrollment en4 = new Enrollment("ENR-504", s3.getId(), e5.getId(), e5.getFullName(), c2.getId(), c2.getTitle(), LocalDate.now().minusDays(2));
        en4.setStatus(EnrollmentStatus.REGISTERED);
        enrollmentRepo.save(en4);
    }
}
