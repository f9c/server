FROM openjdk:10

ADD target/lib  /usr/share/f9c/lib

ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/f9c/f9c.jar

ARG JAR_FILE
ADD ${JAR_FILE} f9c.jar
EXPOSE 8443
ENTRYPOINT ["java","-jar","/usr/share/f9c/f9c.jar"]