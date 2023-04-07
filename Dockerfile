FROM alpine:latest AS base

RUN apk update
RUN apk upgrade --no-cache

FROM base AS build

RUN apk add openjdk17-jdk git

RUN git clone --depth 1 --branch master https://github.com/MateusF03/MilkshakeSimulator /repo
WORKDIR /repo
RUN chmod +x ./gradlew && \
    ./gradlew build && \
    mv ./build/libs/*-all.jar ./build/milkshake.jar

FROM base AS final

RUN apk add --no-cache openjdk17-jre \
                       imagemagick \
                       font-carlito \
                       msttcorefonts-installer \
                       fontconfig
RUN update-ms-fonts && fc-cache -f

COPY --from=build /repo/build/milkshake.jar /usr/share

WORKDIR /var/lib/milkshake
ENTRYPOINT [ "java", "-jar", "/usr/share/milkshake.jar" ]