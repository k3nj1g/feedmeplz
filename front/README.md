# Установка окружения: 

## Установка nodejs:

`cd ~`

`sudo apt-get install curl`

`curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/master/install.sh | bash`

перезапустить терминал

`nvm install --lts`

`node -v`

## Установка shadow-cljs

`npm install -g shadow-cljs`

## Установка зависимостей nodejs

`npm install`

## Запуск билдера TailwindCss

Следит за используемыми классами, добавляет используемые и удаляем ненужные

`npx tailwindcss -i ./resources/public/css/main.css -o ./resources/public/css/tailwind.css --watch`
