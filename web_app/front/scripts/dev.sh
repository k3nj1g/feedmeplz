#!/bin/bash

# Запуск локального сервера с поддержкой HTTPS
npx local-ssl-proxy --source 3001 --target 8280 &
npx shadow-cljs watch app 
