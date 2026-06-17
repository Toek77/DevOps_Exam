FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    openjdk-21-jdk \
    nginx \
    git \
    php-cli \
    openssh-server \
    curl \
    wget \
    unzip \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir /var/run/sshd

WORKDIR /app

# Copy pre-built jar (build on host first: ./gradlew build -x test)
COPY build/libs/idcard-0.0.1-SNAPSHOT.jar /app/app.jar
COPY nginx.conf /etc/nginx/sites-available/default

EXPOSE 22
EXPOSE 8081

CMD service ssh start && \
    service nginx start && \
    echo "Waiting for MySQL..." && \
    while ! nc -z mysql 3306; do sleep 3; done && \
    echo "MySQL ready, starting app..." && \
    java -jar /app/app.jar
