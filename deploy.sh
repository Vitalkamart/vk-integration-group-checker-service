#!/bin/bash

# Build Docker image
./gradlew bootBuildImage --imageName=vk-user-service:latest

# Load image into Minikube
minikube image load vk-user-service:latest

# Apply Kubernetes manifests
kubectl apply -f redis-deployment.yaml
kubectl apply -f vk-secrets.yaml
kubectl apply -f vk-service-deployment.yaml
kubectl apply -f ingress.yaml

# Wait for services to be ready
kubectl wait --for=condition=available deployment/redis --timeout=120s
kubectl wait --for=condition=available deployment/vk-user-service --timeout=120s

# Get service URL
minikube service vk-user-service --url