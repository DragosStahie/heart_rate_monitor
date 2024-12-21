# Heart Rate Monitor BLE App 🚴‍♂️❤️  

This is a **Jetpack Compose-based Android application** that connects to **Bluetooth Low Energy (BLE) heart rate monitors** and visualizes heart rate data in real time. Designed with a focus on **clean architecture**, the app combines **performance and simplicity** to provide users with actionable insights for fitness and health tracking.

---

## 📸 Screenshots  

Here are some highlights of the app in action:  

| Feature                         | Screenshot                                                                                                                                   |
|---------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------|
| **Device Search & Connection**  | ![Search and connect to devices](![ble_hr_select_device](https://github.com/user-attachments/assets/d92580e8-1bb7-447e-8726-43766d2cebd5))   |
| **Color-Coded Heart Rate Zones**| ![Heart rate zones in color](![ble_hr_chart](https://github.com/user-attachments/assets/fe96d1c0-5b3b-459f-b50b-fe8415da0f4f))               |

---

## 🌟 Current Features  

### 1. Device Discovery  
- Scans for BLE devices with the **Heart Rate Service**.  
- Displays a **bottom sheet** for users to select a device to connect to.


### 2. Heart Rate Visualization  
- Real-time BPM reading from the connected heart rate monitor.  
- A **color-coded graph** representing heart rate zones for better tracking.  


### 3. Efficient BLE Data Handling  
- Reads and subscribes to BLE **heart rate characteristic changes**.  
- Parses characteristic bytes to extract heart rate data.

---

## 🚀 Planned Features  

- **Session Recording**  
   - Record heart rate sessions for later review.  
   - Visualize session data with trends and insights.

- **Additional Sensor Support**  
   - Connect and visualize data from **cadence, speed, and power sensors** for cycling.  

- **Advanced Metrics**  
   - Display **Heart Rate Variability (HRV)** and **respiration rate** for compatible devices.

- **UI Improvements**  
   - Enhanced graph animations and a more customizable user experience.

---

## 🛠️ Technical Overview  

This app is built using a **simple version of clean architecture** and **Jetpack Compose**, with a focus on maintainability and scalability.

### Key Components:  
- **BLE Scanning and Connection:**  
  - Uses the **Bluetooth Low Energy API** to discover devices advertising the Heart Rate Service.  
  - Creates a stable connection and subscribes to real-time data updates.  

- **Heart Rate Data Parsing:**  
  - Parses the heart rate measurement characteristic’s byte data to extract accurate BPM values.  

- **UI:**  
  - Fully implemented in **Jetpack Compose** for a modern and dynamic UI experience.  
  - Includes a **real-time graph** with heart rate zones color-coded for easy interpretation.  

---

## 📚 Tech Stack  

- **Language:** Kotlin  
- **Architecture:** Clean Architecture (simplified version)  
- **UI:** Jetpack Compose  
- **BLE API:** Android BLE Library  
- **Other Tools:** Coroutines, SharedFlow, StateFlow  

---

## 💡 Future Goals  

This app aims to provide athletes and fitness enthusiasts with **advanced BLE-based tracking tools**. The planned features will make it a comprehensive platform for monitoring multiple metrics like heart rate, cadence, and power, all while delivering an intuitive user experience.

---

## 🚧 Development Progress  

✅ Scanning and connecting to BLE devices with Heart Rate Service  
✅ Parsing heart rate measurement characteristic data  
✅ Visualizing BPM and heart rate zones in real-time  

🔜 Session recording and review  
🔜 Support for HRV, respiration rate, and other sensors  

---

## 🤝 Contributing  

Contributions are welcome! If you’d like to suggest features, report bugs, or improve the codebase, feel free to create an issue or submit a pull request.  

---

## 📬 Contact  

For questions or collaboration opportunities:  
📧 **Email:** [dragosstahie@gmail.com](mailto:dragosstahie@gmail.com)  
🌐 **Upwork:** [Dragos Stahie](https://www.upwork.com/freelancers/~01ba2fc4047884c4f3)
