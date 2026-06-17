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
    mysql-client \
    && rm -rf /var/lib/apt/lists/*

# SSH setup
RUN mkdir /var/run/sshd && echo 'root:ansible' | chpasswd
RUN sed -i 's/#PermitRootLogin prohibit-password/PermitRootLogin yes/' /etc/ssh/sshd_config

WORKDIR /app

# Copy pre-built jar for running
COPY build/libs/idcard-0.0.1-SNAPSHOT.jar /app/app.jar

# Copy project files for git pull + gradle build (Ansible tasks)
COPY . /app

RUN chmod +x gradlew

COPY nginx.conf /etc/nginx/sites-available/default

EXPOSE 22
EXPOSE 8081

CMD service ssh start && \
    service nginx start && \
    echo "Waiting for MySQL..." && \
    while ! nc -z mysql 3306; do sleep 3; done && \
    echo "MySQL ready, starting app..." && \
    java -jar /app/app.jar
