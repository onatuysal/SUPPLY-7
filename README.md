# SUPPLY-7

SUPPLY7 is a mobile application developed for **Yeditepe University students** to buy, sell, exchange, and share unused academic or personal items within a safe, campus-exclusive environment.  
The app promotes sustainability, reduces waste, and strengthens student-to-student interaction on campus.

> Developed for the **ACM431 – Mobile Devices Programming** course.

---

## 📌 Project Overview

kıvanc pro

- **Platform:** Android  
- **Language:** Kotlin  
- **IDE:** Android Studio  
- **Backend & Database:** Firebase (Auth, Firestore, Storage)  
- **Target Audience:** Yeditepe University students (ages 18–28)  
- **Goal:** Provide a secure, affordable, and sustainable student-only marketplace  

Main functionality based on the official design document includes:

- Login using **Yeditepe University email**  
- Listing items for sale or exchange  
- Faculty-based filtering  
- In-app messaging for communication  
- Meeting on campus to complete the exchange  

---

## 🧩 Core Features

### 🔹 Authentication
- Login with university email  
- Firebase Authentication  

### 🔹 Item Management
- Add items with photos, title, price, description, faculty, and category  
- Edit or delete own listings  
- Product detail page with seller information  

### 🔹 Offers & Transactions
- Make **price offers**  
- Make **exchange offers**  
- Accept or decline offers  
- Complete the exchange safely on campus  

### 🔹 In-App Messaging
- Real-time chat between buyer and seller  
- Built using Firebase Firestore  
- Optional push notifications via FCM  

### 🔹 Search & Filtering
- Filter by faculty, category, material type, condition, and price range  
- Keyword-based search system  

### 🔹 Favorites
- Save items for quick access later  

### 🔹 User Profiles
- User listings  
- Purchase history  
- Sold items  
- Basic profile information  

---

## 🎨 Design Sources

SUPPLY7’s design process is supported by two main tools:

### **Figma (Primary UI/UX Design)**
- Wireframes  
- Low-fidelity prototypes  
- User flow diagrams  
- Main screen layouts (Home, Chat, Filters, Profile, Item Detail)

### **Canva (Presentation Material)**
- Presentation slides  
- Branding visuals  
- Communication assets  

Full design, UX flow, and mockups were produced by the Visual Communication Design (VCD) Team.

---

## 🏗 Architecture (Planned)

Recommended architecture:

- **MVVM (Model–View–ViewModel)**  
- **Repository Pattern**  
- **Firebase Firestore** (dynamic data storage)  
- **Firebase Storage** (image handling)  
- **Firebase Auth** (secure login)

Directory plan:

/ui → Screens (Activities/Fragments), Adapters
/viewmodel → ViewModels
/data → Models, Firebase Repositories
/utils → Helper classes and extensions
/design → Icons, images, branded assets

---

## 🧪 Git & Collaboration Workflow

### Branch Strategy (Feature-Based)
- `main` — Stable, production-ready  
- `feature/backend` — Backend logic, Firebase integration  
- `feature/ui` — UI, layouts, visual design  
- `feature/chat` — Real-time messaging system  
- `design-assets` — Visual design files from VCD team  

### Workflow Rules
1. Always **pull** before starting work  
2. Create/update work under the correct `feature/` branch  
3. Commit frequently with clear messages  
4. Push your feature branch  
5. Open a **Pull Request** to `main`  
6. Merge after review  

---

## 👥 Team

### Software Development  
- **Onat Uysal** — Android Development / Backend / Firebase  
- **Kıvanç Taşkıran** — Android Development / UI / Frontend 

### Visual Communication Design Team  
- UI/UX Design  
- Branding & App Identity  
- Wireframes & Prototyping  
- Presentation Material  
*(Individual names will be added later.)*

---

## 🚀 How to Run the Project

1. Clone the repository:
   ```bash
   git clone https://github.com/onatuysal/SUPPLY7.git


Figma Wireframe Link 
https://www.figma.com/design/GDQQMTMh6NdzWLqE26aT6v/SUPPLY7?node-id=44-28&t=XPLQN1EAJ9cV2ON0-0


