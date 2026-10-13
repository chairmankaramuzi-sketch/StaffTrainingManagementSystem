# 🎓 Staff Training Management System (STMS)
> An Enterprise Object-Oriented Application designed for Apache NetBeans IDE & Java SE 17+

---

## 📌 1. Project Overview & Architecture
The **Staff Training Management System (STMS)** is an end-to-end, enterprise-ready software solution designed to administer, schedule, track, and evaluate corporate employee training programs. 

The system implements the four core pillars of **Object-Oriented Programming (OOP)**:
- **Encapsulation**: Rigorous state isolation, private access modifiers, domain input validation, and immutability where applicable.
- **Inheritance**: 
  - `Person` (Base) $\rightarrow$ `Employee` (Corporate Staff) & `Trainer` (Certified Instructor).
  - `TrainingCourse` (Abstract Base) $\rightarrow$ `TechnicalCourse`, `SoftSkillsCourse`, and `ComplianceCourse`.
- **Polymorphism**: Dynamic dispatch across course categories for:
  - Minimum passing thresholds (`70%` for Technical, `60%` for Soft Skills, `85%` for Mandatory Compliance).
  - Certificate validity lifetimes (`36 months` for Technical, `Lifetime` for Soft Skills, `12 months` annual renewal for Compliance).
  - Query filtering and report formatting.
- **Abstraction**: High-level contracts established via interfaces:
  - `Identifiable`: Universal entity identification contract.
  - `Certifiable`: Specification for certificate issuance eligibility.
  - `Reportable`: Unified text and transcript generation.
  - `Searchable`: Polymorphic query pattern.
  - `Repository<T>`: Generic repository abstraction decoupling data access from business logic.

---

## 🛠️ 2. How to Open and Run in Apache NetBeans

### Step 1: Open Apache NetBeans
1. Launch **Apache NetBeans IDE**.
2. Click **File** $\rightarrow$ **Open Project...** (`Ctrl + Shift + O`).
3. Browse to:
   ```
   C:\Users\KARAMUZI\.gemini\antigravity-ide\scratch\StaffTrainingManagementSystem
   ```
4. NetBeans will automatically detect the `pom.xml` and recognize it as a **Maven Java Application**.
5. Click **Open Project**.

### Step 2: Run the Application
1. In the NetBeans **Projects** tree, right-click `Staff Training Management System` and choose **Run** (or press `F6` / click the green **▶ Play** button on the toolbar).
2. The modern desktop GUI will launch immediately!

### Alternative: Run from Command Line / Explorer
- Simply double-click `run.bat` located inside the project directory, or run:
  ```powershell
  cd C:\Users\KARAMUZI\.gemini\antigravity-ide\scratch\StaffTrainingManagementSystem
  .\run.bat
  ```

---

## 📊 3. System Features & Modules

1. **📊 Executive Dashboard**:
   - Real-time KPI summary cards: Total Staff, Training Courses, Scheduled Sessions, Total Enrollments, Overall Pass Rate (%), and Certificates Issued.
   - Live activity & audit stream logging all transactions.

2. **👥 Staff Directory Management**:
   - Complete employee records with department affiliation, job titles, and contact details.
   - Dynamic search by employee name, ID, or job title.
   - Department filter dropdown (Engineering, HR, Finance, Marketing, Operations, etc.).
   - Full CRUD: Add Staff dialog, Edit Staff, and Delete Staff.

3. **📚 Course Catalog**:
   - Multi-disciplinary course management:
     - **Technical Courses**: Track technology stack and hands-on lab requirements.
     - **Soft Skills Courses**: Focus on leadership, psychological safety, and teamwork.
     - **Mandatory Compliance**: Strict annual regulatory courses (e.g. GDPR, ISO 27001).
   - Built with the **Factory Pattern** (`CourseFactory.createCourse(...)`).

4. **📅 Training Sessions & Scheduling**:
   - Session scheduling with assigned instructors, room/virtual link, and seat capacity limits.
   - Prevents overbooking; tracks seat availability.
   - Lifecycle state machine: `SCHEDULED` $\rightarrow$ `IN_PROGRESS` $\rightarrow$ `COMPLETED` / `CANCELLED`.
   - "View Attendees" dialog displays all registered staff in real time.

5. **📝 Enrollment & Polymorphic Grading**:
   - Enrolls staff into sessions with duplicate enrollment and capacity checks.
   - Record Attendance & Assessment Scores (0 - 100%).
   - **Polymorphic Certification Engine**: Evaluates completion criteria according to the course type. If passed, automatically generates and links a unique verification certificate!

6. **📄 Official Transcripts & Reports**:
   - Formal corporate **Staff Training Transcript** generation with completed courses, scores, and valid certificates.
   - Departmental Participation Summary Report.
   - Export report directly to a `.txt` file on disk.

7. **💾 Automated File Persistence**:
   - Real-time synchronization to clean CSV storage in the `data/` folder.
   - Automatically preloaded with realistic demo data on initial startup.

---

## 📁 4. Project Directory Structure
```
StaffTrainingManagementSystem/
├── pom.xml                                    (Maven project configuration)
├── README.md                                  (Full documentation & instructions)
├── run.bat                                    (1-Click Windows launch script)
├── data/                                      (Persistent CSV data storage)
│   ├── employees.csv
│   ├── trainers.csv
│   ├── courses.csv
│   ├── sessions.csv
│   ├── enrollments.csv
│   └── certificates.csv
└── src/
    └── main/
        └── java/
            └── com/
                └── stms/
                    ├── Main.java              (Main Application Entry Point)
                    ├── model/                 (OOP Domain Entities & Enums)
                    │   ├── Person.java        (Abstract base)
                    │   ├── Employee.java      (Inherits Person)
                    │   ├── Trainer.java       (Inherits Person)
                    │   ├── Department.java    (Enum)
                    │   ├── CourseLevel.java   (Enum)
                    │   ├── TrainingCourse.java(Abstract base)
                    │   ├── TechnicalCourse.java
                    │   ├── SoftSkillsCourse.java
                    │   ├── ComplianceCourse.java
                    │   ├── TrainingSession.java
                    │   ├── SessionStatus.java (Enum)
                    │   ├── Enrollment.java
                    │   ├── EnrollmentStatus.java (Enum)
                    │   ├── Certificate.java
                    │   └── interfaces/        (Abstraction contracts)
                    │       ├── Identifiable.java
                    │       ├── Certifiable.java
                    │       ├── Reportable.java
                    │       └── Searchable.java
                    ├── repository/            (Data Access Layer)
                    │   ├── Repository.java    (Generic interface)
                    │   ├── InMemoryRepository.java (Generic base)
                    │   ├── EmployeeRepository.java
                    │   ├── TrainerRepository.java
                    │   ├── CourseRepository.java
                    │   ├── SessionRepository.java
                    │   ├── EnrollmentRepository.java
                    │   └── CertificateRepository.java
                    ├── service/               (Business Logic & Architecture)
                    │   ├── TrainingManagerService.java
                    │   ├── DataPersistenceService.java
                    │   ├── NotificationService.java
                    │   └── CourseFactory.java
                    └── ui/                    (Java Swing GUI Layer)
                        ├── MainApplicationFrame.java
                        ├── DashboardPanel.java
                        ├── StaffManagementPanel.java
                        ├── CourseCatalogPanel.java
                        ├── SessionSchedulePanel.java
                        ├── EnrollmentGradingPanel.java
                        ├── ReportsPanel.java
                        └── components/
                            ├── ModernCard.java
                            └── UITheme.java
```
