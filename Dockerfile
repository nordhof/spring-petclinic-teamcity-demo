FROM eclipse-temurin:17-jre-jammy

RUN adduser --system --disabled-login --disabled-password --group app-user

USER app-user
WORKDIR /home/app-user

COPY target/spring-petclinic-*.jar app.jar

CMD ["java", "-jar", "app.jar"]
