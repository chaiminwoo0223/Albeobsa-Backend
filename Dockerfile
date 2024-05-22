# 기본 이미지 설정
FROM --platform=linux/amd64 openjdk:21-jdk-slim

# Google Chrome 설치
RUN apt-get -y update \
    && apt -y install wget \
    && wget https://dl.google.com/linux/direct/google-chrome-stable_current_amd64.deb \
    && apt -y install ./google-chrome-stable_current_amd64.deb \
    && rm google-chrome-stable_current_amd64.deb

# 애플리케이션 JAR 파일 복사
ARG JAR_FILE=build/libs/JiJiJig-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} app.jar

# 애플리케이션 포트 노출
EXPOSE 8080

# 엔트리 포인트
ENTRYPOINT ["java","-jar","/app.jar"]