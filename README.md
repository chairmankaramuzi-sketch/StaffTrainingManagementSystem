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
   C:\Users\KARAMUZI\Desktop\StaffTrainingManagementSystem
   ```
4. NetBeans will automatically detect the `pom.xml` and recognize it as a **Maven Java Application**.
5. Click **Open Project**.

### Step 2: Run the Application
1. In the NetBeans **Projects** tree, right-click `Staff Training Management System` and choose **Run** (or press `F6` / click the green **▶ Play** button on the toolbar).
2. The modern desktop GUI will launch immediately!

### Alternative: Run from Command Line / Explorer
- Simply double-click `run.bat` located inside the project directory, or run:
  ```powershell
  cd C:\Users\KARAMUZI\Desktop\StaffTrainingManagementSystem
  .\run.bat
  ```

---

## 📊 3. System Features & Technologies Used

The application strictly fulfills all coursework requirements using standalone desktop Java technologies:

### 1. Front End (User Interface) - Java Swing
- **Modern Graphical User Interface**: Custom styled UI with dark header, tabbed workflow navigation, responsive tables, modal dialogs, and real-time statistics cards.
- **Event-Driven Architecture**: Fully decoupled Swing Event Dispatch Thread (EDT) with ActionListeners, DocumentFilters for dynamic search, and TableModel synchronization.
- **Diagnostics Dialog**: Dedicated `DatabaseDiagnosticsDialog` allowing live JDBC testing, table inspection, and SQL schema viewing right inside the app!

### 2. Back End (Application Logic) - Pure Java OOP
- **Encapsulation, Inheritance, Polymorphism, and Abstraction**.
- **Factory Method Pattern**: `CourseFactory` dynamically instantiates `TechnicalCourse`, `SoftSkillsCourse`, and `ComplianceCourse`.
- **Business Rule Engine**: Automated polymorphic pass/fail evaluation and dynamic verification certificate generation.

### 3. Database Connectivity - JDBC (Java Database Connectivity)
- **Standard JDBC API**: Uses `java.sql.Connection`, `java.sql.DriverManager`, `java.sql.PreparedStatement`, `java.sql.Statement`, and `java.sql.ResultSet`.
- **Relational Tables**:
  - `employees`: Staff records with department, job title, and hire dates.
  - `trainers`: Instructors with hourly rates and specializations.
  - `courses`: Polymorphic courses with specific attributes and passing criteria.
  - `sessions`: Scheduled training sessions with capacity constraints.
  - `enrollments`: Employee session enrollments with attendance, scores, and status.
  - `certificates`: Official credentials with unique cryptographic verification codes.
- **Out-of-the-Box Zero Configuration**: Bundled with embedded SQLite JDBC (`data/stms.db`) so evaluators can run the project on any PC in NetBeans without configuring database servers or passwords.
- **SQL DDL Schema File**: Included at `database/schema.sql` for inspection or import into MySQL / NetBeans Services database explorer.
- **Dual Persistence Backup**: Auto-synchronizes to clean CSV files in the `data/` folder.

---

## 📁 4. Project Directory Structure
```
StaffTrainingManagementSystem/
├── pom.xml                                    (Maven project configuration with JDBC drivers)
├── README.md                                  (Full documentation & coursework instructions)
├── run.bat                                    (1-Click Windows launch script)
├── database/                                  (SQL Schema DDL)
│   └── schema.sql                             (ANSI SQL / SQLite / MySQL DDL script)
├── data/                                      (Persistent Storage)
│   ├── stms.db                                (Primary SQLite JDBC relational database)
│   ├── employees.csv                          (CSV backup)
│   ├── trainers.csv                           (CSV backup)
│   ├── courses.csv                            (CSV backup)
│   ├── sessions.csv                           (CSV backup)
│   ├── enrollments.csv                        (CSV backup)
│   └── certificates.csv                       (CSV backup)
└── src/
    └── main/
        └── java/
            └── com/
                └── stms/
                    ├── Main.java              (Main Application Entry Point)
                    ├── database/              (JDBC Connectivity Layer)
                    │   └── DatabaseManager.java (JDBC Connection & PreparedStatements)
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
