# 🌱 KRISHI – Smart Agriculture Assistant for Farmers  

Krishi is a feature-rich mobile application designed to empower farmers with accurate weather insights, crop predictions, soil metrics, satellite analytics, disease diagnosis, and a knowledge-sharing community — all in one modern, easy-to-use app.

---

## 🚀 Features  

### 1️⃣ **🔐 Login & Signup with Mobile Number**  
- Secure authentication using Firebase Phone Auth.  
- Automatic OTP verification and lightning-fast login.  
- Session is preserved so users stay logged in until they choose to log out.

---

### 2️⃣ **🌤️ Advanced Weather & Forecast System**  
Powered by the OpenWeather API  
- Real-time temperature, humidity, wind speed, pressure, and visibility.  
- **Hourly forecast for the next 4 days** with visually appealing charts.  
- **Extended 15-day weather forecast** to help farmers plan field activities.  
- Attractive UI with Material components and animated weather icons.

---

### 3️⃣ **🌾 Soil Metrics & Satellite Insights (AgroMonitoring API)**  
A powerful analytics dashboard offering:  
- **Soil surface temperature**  
- **Soil temperature at 10 cm depth**  
- **Soil moisture content**  
- **UV index & radiation metrics**  
- **TrueColor, FalseColor, NDVI, EVI, EVI2, NRI, DSWI, NDWI maps and satellite imagery** for crop health monitoring  
- High-resolution maps and time-series charts for deeper analysis  

This feature helps farmers understand soil health before sowing and during crop growth.

---

### 4️⃣ **📰 Agriculture & Agritech News**  
- Latest news fetched from trusted sources using the NewsAPI.  
- Focused on agriculture, agritech innovations, market trends, and government schemes.  
- Clean card-based UI for quick reading.

---

### 5️⃣ **✍️ Farmer Blogs – Community Knowledge Sharing**  
- Users can create and publish their own blogs.  
- Blogs are stored securely in Firebase Firestore.  
- All blogs are visible to the entire community.  
- A separate section shows **only user-created blogs** for easy management.

---

### 6️⃣ **🌱 Crop Cultivation Tips & Guidance** *(upcoming)*  
- Seasonal crop recommendations  
- Best farming practices  
- Fertilizer guides  
- Irrigation and pest-control suggestions  

A complete knowledge hub for farmers.

---

### 7️⃣ **🤖 ML-Based Crop Prediction System**  
Trained using **XGBoost** on a large multi-parameter dataset including:  
- Nitrogen, Phosphorus, Potassium levels  
- Temperature, humidity, rainfall  
- Soil pH and other physical parameters  

The system predicts the **most suitable crop** for the user's location and soil profile.

---

### 8️⃣ **🌿 Plant Disease Detection (AI + Computer Vision)**  
- Upload a leaf photo  
- A ResNet-based CNN model identifies plant diseases with high accuracy  
- Provides the disease name, severity, and treatment suggestions  

Helps farmers detect issues early and prevent crop loss.

---

### 9️⃣ **🎨 Modern Material UI Design**  
- Clean, attractive layout using Material components  
- Smooth navigation and responsive design  
- Well-structured screens with meaningful icons and color themes  
- Intuitive user experience suitable for farmers of all ages  

---

## 🛠️ Tech Stack  

### **Frontend (Android)**  
- Java  
- XML + Material Components  

### **Backend & APIs**  
- Firebase Authentication  
- Firebase Firestore  
- OpenWeather API  
- AgroMonitoring API  
- News API  

### **Machine Learning**  
- XGBoost for Crop Prediction  
- ResNet for Disease Detection  
- Trained on extensive agricultural datasets  
- Kaggle Dataset for Crop Prediction - https://www.kaggle.com/datasets/atharvaingle/crop-recommendation-dataset
- kaggle Dataset for Disease Detection - https://www.kaggle.com/datasets/vipoooool/new-plant-diseases-dataset
- Kaggle Notebook Link - https://www.kaggle.com/code/subhranshupatra29/krishi-best-crop-prediction-model
- Switch to GPU T4 x2 accelerator while training on Kaggle (compulsorily while for training the disease detection model) 
---

## 📦 Project Highlights  
- End-to-end working app tailored for real farmers  
- Combines weather, soil, satellite, ML, and community features  
- Highly scalable and modular design  

---

## 📱 Screenshots

<table> <tr> <td align="center"> <b>🟢 Login Screen</b><br> <img src="https://github.com/user-attachments/assets/fd600125-6aeb-4b58-bffd-e25f4b2f9b61" width="250"> </td> <td align="center"> <b>🟢 Crop Prediction</b><br> <img src="https://github.com/user-attachments/assets/7c8122d1-25bf-427d-aea2-430ab1970bbf" width="250"> </td> <td align="center"> <b>🟢 Disease Detection</b><br> <img src="https://github.com/user-attachments/assets/9d776916-d66f-4a6e-8cb8-df2fdd6625dd" width="250"> </td> </tr> <tr> <td align="center"> <b>🟢 Weather & Forecast</b><br> <img src="https://github.com/user-attachments/assets/c9877154-fec1-4e06-bfe4-64356e114757" width="250"> </td> <td align="center"> <b>🟢 Soil & Satellite Metrics</b><br> <img src="https://github.com/user-attachments/assets/b33ae6bf-49c1-41c8-ad78-ea2022223d5e" width="250"> </td> <td align="center"> <b>🟢 Blogs & Community</b><br> <img src="https://github.com/user-attachments/assets/b24ea67f-ff11-4293-a93e-559c3318409e" width="250"> </td> </tr> </table>

---
## 🤝 Contributing  

Pull requests are welcome! Feel free to contribute new features such as:  
- Market price prediction  
- Smart irrigation scheduling  
- Voice chatbot for farmers  

---

## 📞 **Contact Me**
For any queries or contributions, feel free to reach out:  
📌 **GitHub:** [Subhranshu-Patra29](https://github.com/Subhranshu-Patra29)  
📌 **Email:** bongspatra@gmail.com

---
## ⭐ If you like this project, give it a star on GitHub!  
