FROM openjdk:10

VOLUME /usr/share/f9c/keys

ADD target/lib  /usr/share/f9c/lib

ADD src/main/aiml  /usr/share/f9c/aiml

ARG JAR_FILE
ADD ${JAR_FILE} /usr/share/f9c/f9c-bot.jar

ARG JAR_FILE
ADD ${JAR_FILE} f9c-bot.jar

ENTRYPOINT ["java", "-jar","/usr/share/f9c/f9c-bot.jar", "/usr/share/f9c/keys", "/usr/share/f9c/aiml", "Hearty Hart"]