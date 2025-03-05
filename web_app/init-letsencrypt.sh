#!/bin/bash

domains=(feedmeplz.ru)
rsa_key_size=4096
data_path="./nginx/certbot"
email="i@dnibaev.ru"
staging=0 # Установите 1 для тестирования, чтобы избежать ограничений запросов

if [ -d "$data_path" ]; then
  read -p "Найдены существующие данные для $domains. Продолжить и заменить существующий сертификат? (y/N) " decision
  if [ "$decision" != "Y" ] && [ "$decision" != "y" ]; then
    exit
  fi
fi

if [ ! -e "$data_path/conf/options-ssl-nginx.conf" ] || [ ! -e "$data_path/conf/ssl-dhparams.pem" ]; then
  echo "### Загрузка рекомендуемых TLS параметров ..."
  mkdir -p "$data_path/conf"
  curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf > "$data_path/conf/options-ssl-nginx.conf"
  curl -s https://raw.githubusercontent.com/certbot/certbot/master/certbot/certbot/ssl-dhparams.pem > "$data_path/conf/ssl-dhparams.pem"
  echo
fi

echo "### Создание временного сертификата для $domains ..."
path="/etc/letsencrypt/live/$domains"
mkdir -p "$data_path/conf/live/$domains"
docker compose -f docker-compose.deploy.yaml run --rm --entrypoint "\
  openssl req -x509 -nodes -newkey rsa:$rsa_key_size -days 1\
    -keyout '$path/privkey.pem' \
    -out '$path/fullchain.pem' \
    -subj '/CN=localhost'" certbot
echo

echo "### Запуск nginx ..."
docker compose -f docker-compose.deploy.yaml up --force-recreate -d nginx
echo

echo "### Удаление временного сертификата для $domains ..."
docker compose -f docker-compose.deploy.yaml run --rm --entrypoint "\
  rm -Rf /etc/letsencrypt/live/$domains && \
  rm -Rf /etc/letsencrypt/archive/$domains && \
  rm -Rf /etc/letsencrypt/renewal/$domains.conf" certbot
echo

echo "### Запрос сертификата Let's Encrypt для $domains ..."
#Join $domains to -d args
domain_args=""
for domain in "${domains[@]}"; do
  domain_args="$domain_args -d $domain"
done

# Выбор соответствующего аргумента email
case "$email" in
  "") email_arg="--register-unsafely-without-email" ;;
  *) email_arg="--email $email" ;;
esac

# Включение режима тестирования при необходимости
if [ $staging != "0" ]; then staging_arg="--staging"; fi

docker compose -f docker-compose.deploy.yaml run --rm --entrypoint "\
  certbot certonly --webroot -w /var/www/certbot \
    $staging_arg \
    $email_arg \
    $domain_args \
    --rsa-key-size $rsa_key_size \
    --agree-tos \
    --force-renewal" certbot
echo

echo "### Перезагрузка nginx ..."
docker compose -f docker-compose.deploy.yaml exec nginx nginx -s reload
