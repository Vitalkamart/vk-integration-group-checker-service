VK User Service
Микросервис для получения информации о пользователях VK и проверки их членства в группах с кэшированием через Redis.

🚀 Быстрый старт
Предварительные требования
Java 21+

Docker & Docker Desktop

Minikube

kubectl

Локальный запуск (без Kubernetes)
bash
# Запуск Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine

# Запуск приложения
./gradlew bootRun -Dspring.data.redis.host=localhost

Или с Docker Compose:

bash
docker-compose up --build

Развертывание в Minikube

Запустите Minikube:

bash
minikube start --driver=docker
Настройте Docker daemon Minikube:

bash
minikube docker-env | Invoke-Expression

Соберите Docker образ:

bash
docker build -t vk-user-service:latest .

Создайте секрет с VK токеном:

bash
kubectl create secret generic vk-secrets --from-literal=service-token='your_vk_service_token_here'
Разверните приложение:

bash
kubectl apply -f redis-deployment.yaml
kubectl apply -f vk-service-deployment.yaml

🔧 Конфигурация

Environment Variables

SPRING_DATA_REDIS_HOST: Redis хост (по умолчанию: redis)

SPRING_DATA_REDIS_PORT: Redis порт (по умолчанию: 6379)

VK_API_SERVICE_TOKEN: Сервисный токен VK (через секрет)

Secrets Management

bash
# Создание секрета

kubectl create secret generic vk-secrets --from-literal=service-token='your_token'

# Проверка секрета

kubectl get secrets

kubectl describe secret vk-secrets

📡 API Endpoints

Основной endpoint
text
POST /api/v1/vk-users/info
Content-Type: application/json
Authorization: Basic YWRtaW46cGFzc3dvcmQ=
vk_service_token: ваш_токен

{
  "user_id": 123,
  "group_id": 456
}

Управление кэшем
text
POST /api/v1/cache/clear
POST /api/v1/cache/clear-all

Swagger документация
text
/swagger-ui.html

🧪 Тестирование

Тестовый запрос
bash
# Получить URL сервиса
SERVICE_URL=$(minikube service vk-user-service --url)

# Тестовый запрос
curl -X POST \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic YWRtaW46cGFzc3dvcmQ=" \
  -H "vk_service_token: ваш_токен" \
  -d '{"user_id":1,"group_id":1}' \
  $SERVICE_URL/api/v1/vk-users/info
  
Проверка кэша
bash
# Port-forward Redis
kubectl port-forward svc/redis 6379:6379

# Проверить ключи в Redis
kubectl exec -it deployment/redis -- redis-cli KEYS *

🛠️ Команды управления

Мониторинг
bash
# Статус подов
kubectl get pods

# Логи приложения
kubectl logs deployment/vk-user-service -f

# Логи Redis
kubectl logs deployment/redis
Обновление приложения
bash
# Пересобрать образ
docker build -t vk-user-service:latest .

# Перезапустить deployment
kubectl rollout restart deployment/vk-user-service
Остановка и очистка
bash
# Остановка сервисов
kubectl delete -f vk-service-deployment.yaml
kubectl delete -f redis-deployment.yaml

# Остановка Minikube
minikube stop

# Полная очистка
minikube delete --all --purge

📊 Проверка работы

Проверьте статус подов:

bash
kubectl get pods -w
Проверьте сервисы:

bash
kubectl get services
Откройте приложение:

bash
minikube service vk-user-service
Проверьте Swagger:
Откройте http://<minikube-ip>/swagger-ui.html

🐛 Troubleshooting

Common Issues
ImagePullBackOff: Убедитесь, что imagePullPolicy: Never установлен

CreateContainerConfigError: Проверьте существование секрета vk-secrets

Connection refused: Убедитесь, что Redis запущен

Debug Commands
bash
# Детальная информация о поде
kubectl describe pod <pod-name>

# Проверка событий кластера
kubectl get events

# Доступ к Redis CLI
kubectl exec -it deployment/redis -- redis-cli

📝 Примечания

Сервис использует Basic Authentication: admin/password

Кэш автоматически очищается через 1 минуту

Для production использования настройте proper secrets management

Swagger документация доступна по /swagger-ui.html
