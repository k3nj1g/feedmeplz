# Build stage
FROM theasp/clojurescript-nodejs:shadow-cljs-alpine AS build

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy project files
COPY . .

RUN apk add --no-cache git

# Build the project
RUN npm run build

# Production stage
FROM nginx:alpine

# Install envsubst
RUN apk add --no-cache gettext

# Copy built assets from build stage
COPY --from=build /app/resources/public /usr/share/nginx/html

# Copy nginx configuration template
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
