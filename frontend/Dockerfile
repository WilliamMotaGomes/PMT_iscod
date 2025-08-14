FROM node:20-alpine AS build
WORKDIR /app

COPY package.json package-lock.json* ./
RUN npm ci --no-audit --no-fund || npm i --no-audit --no-fund

COPY angular.json tsconfig*.json karma.conf.js proxy.conf.json .browserslistrc ./
COPY src ./src
RUN npm run build

FROM nginx:alpine

RUN rm /etc/nginx/conf.d/default.conf
COPY nginx.conf /etc/nginx/conf.d/default.conf

COPY --from=build /app/dist/ /tmp/dist/
RUN dest=$(dirname $(find /tmp/dist -name index.html | head -n1)); \
    mkdir -p /usr/share/nginx/html && \
    cp -r "$dest"/* /usr/share/nginx/html/

EXPOSE 80
