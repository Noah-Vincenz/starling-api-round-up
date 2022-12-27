FROM openjdk:11
COPY target/savings-goal-creator-0.0.1-SNAPSHOT.jar savings-goal-creator-0.0.1-SNAPSHOT.jar
ENTRYPOINT ["java","-jar","/savings-goal-creator-0.0.1-SNAPSHOT.jar"]
