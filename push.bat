git add .gitignore
git add medimart-backend/
git commit -m "Stage 1-6: Xây dựng Backend Node.js & REST API"

git add medimart-android/build.gradle.kts medimart-android/settings.gradle.kts medimart-android/gradle medimart-android/gradle.properties medimart-android/gradlew* medimart-android/app/build.gradle.kts medimart-android/app/src/main/res medimart-android/app/src/main/AndroidManifest.xml
git add medimart-android/app/src/main/java/com/example/medimart/theme medimart-android/app/src/main/java/com/example/medimart/util medimart-android/app/src/main/java/com/example/medimart/data
git commit -m "Stage 7-9: Khởi tạo Android, Cấu hình Retrofit/Room & Theme Minimalist"

git add medimart-android/app/src/main/java/com/example/medimart/ui/components medimart-android/app/src/main/java/com/example/medimart/ui/screens/auth medimart-android/app/src/main/java/com/example/medimart/ui/screens/home medimart-android/app/src/main/java/com/example/medimart/ui/screens/cart medimart-android/app/src/main/java/com/example/medimart/ui/screens/category medimart-android/app/src/main/java/com/example/medimart/ui/screens/profile
git commit -m "Stage 10-11: Xây dựng các màn hình cốt lõi (Auth, Home, Cart, Profile)"

git add .
git commit -m "Stage 12: Hoàn thiện hệ thống Navigation, Checkout flow và Tối ưu ứng dụng"

git push origin main
