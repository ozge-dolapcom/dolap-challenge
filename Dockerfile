FROM java:8-jdk-alpine
COPY ./target/dolap-challange-0.0.1.jar /usr/app/
WORKDIR /usr/app
RUN sh -c 'touch dolap-challange-0.0.1.jar'
ENTRYPOINT ["java", "-jar", "dolap-challange-0.0.1.jar"]