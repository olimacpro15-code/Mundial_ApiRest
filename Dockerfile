FROM eclipse-temurin:21-jdk
COPY "./target/Mundial-1.0.0.jar" "app.jar"
EXPOSE 8101
ENTRYPOINT [ "java", "-jar", "app.jar" ]