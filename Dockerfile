FROM java:8-jdk-alpine
COPY ./target/dolap-challenge-0.0.1.jar /usr/app/
WORKDIR /usr/app
RUN sh -c 'touch dolap-challenge-0.0.1.jar'
ENTRYPOINT ["java", "-jar", "dolap-challenge-0.0.1.jar"]