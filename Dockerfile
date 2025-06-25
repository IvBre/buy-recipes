FROM gradle:8.14-jdk24 AS build
WORKDIR /app
COPY . .
RUN mkdir -p src/main/resources/db/migration
RUN cp src/main/resources/schema.sql src/main/resources/db/migration/V1__init.sql
RUN cp src/main/resources/data.sql src/main/resources/db/migration/V2__data.sql
RUN gradle build --no-daemon

FROM amazoncorretto:24
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]