#!/bin/bash

# ============================================
# Скрипт для локального тестування перед деплоєм
# ============================================

set -e  # Зупинка при помилках

# Кольори для виводу
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "================================================"
echo "🧪 Тестування готовності до деплою на Render"
echo "================================================"
echo ""

# ============================================
# 1. Перевірка структури проєкту
# ============================================
echo "📁 Перевірка структури проєкту..."

check_file() {
    if [ -f "$1" ]; then
        echo -e "${GREEN}✓${NC} $1"
    else
        echo -e "${RED}✗${NC} $1 - НЕ ЗНАЙДЕНО!"
        exit 1
    fi
}

check_dir() {
    if [ -d "$1" ]; then
        echo -e "${GREEN}✓${NC} $1/"
    else
        echo -e "${RED}✗${NC} $1/ - НЕ ЗНАЙДЕНО!"
        exit 1
    fi
}

# Перевірка модулів
check_dir "core"
check_dir "persistence"
check_dir "web"

# Перевірка конфігураційних файлів
check_file "pom.xml"
check_file "Dockerfile"
check_file ".dockerignore"
check_file "render.yaml"
check_file "web/src/main/resources/application.properties"

# Перевірка Flyway міграцій
check_file "persistence/src/main/resources/db/migration/V1__init_schema.sql"

echo ""

# ============================================
# 2. Перевірка Java та Maven
# ============================================
echo "☕ Перевірка Java та Maven..."

if command -v java &> /dev/null; then
    JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
    echo -e "${GREEN}✓${NC} Java version: $JAVA_VERSION"

    # Перевірка версії Java
    JAVA_MAJOR=$(echo "$JAVA_VERSION" | cut -d'.' -f1)
    if [ "$JAVA_MAJOR" -ge 17 ]; then
        echo -e "${GREEN}✓${NC} Java 17+ встановлено"
    else
        echo -e "${RED}✗${NC} Потрібна Java 17 або новіша! Поточна: $JAVA_VERSION"
        exit 1
    fi
else
    echo -e "${RED}✗${NC} Java не знайдено!"
    exit 1
fi

if command -v mvn &> /dev/null; then
    MVN_VERSION=$(mvn -version | head -n 1 | awk '{print $3}')
    echo -e "${GREEN}✓${NC} Maven version: $MVN_VERSION"
else
    echo -e "${RED}✗${NC} Maven не знайдено!"
    exit 1
fi

echo ""

# ============================================
# 3. Перевірка Docker
# ============================================
echo "🐳 Перевірка Docker..."

if command -v docker &> /dev/null; then
    DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
    echo -e "${GREEN}✓${NC} Docker version: $DOCKER_VERSION"

    # Перевірка, чи запущено Docker
    if docker ps &> /dev/null; then
        echo -e "${GREEN}✓${NC} Docker daemon запущено"
    else
        echo -e "${YELLOW}⚠${NC} Docker daemon не запущено (не критично для Render)"
    fi
else
    echo -e "${YELLOW}⚠${NC} Docker не знайдено (не критично для Render)"
fi

echo ""

# ============================================
# 4. Збірка проєкту
# ============================================
echo "🔨 Збірка проєкту..."

# Очистка та збірка
mvn clean package -DskipTests

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Проєкт успішно зібрано"
else
    echo -e "${RED}✗${NC} Помилка збірки проєкту!"
    exit 1
fi

# Перевірка наявності JAR
if [ -f web/target/*.jar ]; then
    JAR_SIZE=$(du -h web/target/*.jar | awk '{print $1}')
    echo -e "${GREEN}✓${NC} JAR файл створено (розмір: $JAR_SIZE)"
else
    echo -e "${RED}✗${NC} JAR файл не знайдено!"
    exit 1
fi

echo ""

# ============================================
# 5. Запуск тестів
# ============================================
echo "🧪 Запуск тестів..."

mvn test

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Всі тести пройдено"
else
    echo -e "${RED}✗${NC} Деякі тести не пройшли!"
    echo -e "${YELLOW}⚠${NC} Виправте помилки перед деплоєм"
    exit 1
fi

echo ""

# ============================================
# 6. Перевірка Dockerfile
# ============================================
echo "🐳 Перевірка Dockerfile..."

# Lint Dockerfile (якщо встановлено hadolint)
if command -v hadolint &> /dev/null; then
    hadolint Dockerfile
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓${NC} Dockerfile коректний"
    else
        echo -e "${YELLOW}⚠${NC} Знайдено рекомендації для Dockerfile"
    fi
else
    echo -e "${YELLOW}⚠${NC} hadolint не встановлено (опціонально)"
fi

# Перевірка базового образу
BASE_IMAGE=$(grep "FROM" Dockerfile | head -n 1 | awk '{print $2}')
echo -e "${GREEN}✓${NC} Базовий образ: $BASE_IMAGE"

echo ""

# ============================================
# 7. Перевірка application.properties
# ============================================
echo "⚙️ Перевірка конфігурації..."

PROPS_FILE="web/src/main/resources/application.properties"

check_property() {
    if grep -q "$1" "$PROPS_FILE"; then
        echo -e "${GREEN}✓${NC} $1 налаштовано"
    else
        echo -e "${RED}✗${NC} $1 відсутня!"
        exit 1
    fi
}

check_property "spring.datasource.url"
check_property "spring.jpa.hibernate.ddl-auto"
check_property "spring.flyway.enabled"

# Перевірка, що не використовується H2 в продакшн
if grep -q "h2" "$PROPS_FILE"; then
    echo -e "${YELLOW}⚠${NC} Знайдено посилання на H2 (переконайтеся, що це тільки для dev)"
fi

echo ""

# ============================================
# 8. Перевірка Git
# ============================================
echo "🔍 Перевірка Git..."

# Перевірка, чи є незакомічені зміни
if [ -n "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠${NC} Є незакомічені зміни:"
    git status --short
    echo ""
    echo -e "${YELLOW}⚠${NC} Не забудьте закомітити перед деплоєм!"
else
    echo -e "${GREEN}✓${NC} Всі зміни закомічено"
fi

# Перевірка наявності гілки deploy
if git rev-parse --verify deploy &> /dev/null; then
    echo -e "${GREEN}✓${NC} Гілка 'deploy' існує"
else
    echo -e "${YELLOW}⚠${NC} Гілка 'deploy' не знайдена"
    echo "   Створіть: git checkout -b deploy"
fi

# Перевірка remote
if git remote -v | grep -q "github.com"; then
    REMOTE_URL=$(git remote get-url origin)
    echo -e "${GREEN}✓${NC} GitHub remote: $REMOTE_URL"
else
    echo -e "${RED}✗${NC} GitHub remote не налаштовано!"
    exit 1
fi

echo ""

# ============================================
# 9. Перевірка секретів
# ============================================
echo "🔒 Перевірка секретів..."

# Перевірка .gitignore
if [ -f ".gitignore" ]; then
    if grep -q ".env" ".gitignore"; then
        echo -e "${GREEN}✓${NC} .env у .gitignore"
    else
        echo -e "${YELLOW}⚠${NC} Додайте .env до .gitignore"
    fi
else
    echo -e "${RED}✗${NC} .gitignore не знайдено!"
fi

# Перевірка, чи немає жорстко закодованих паролів
if grep -r "password=" web/src/main/resources/*.properties 2>/dev/null | grep -v "\${" | grep -v "#"; then
    echo -e "${RED}✗${NC} Знайдено жорстко закодовані паролі!"
    echo "   Використовуйте змінні середовища!"
else
    echo -e "${GREEN}✓${NC} Жорстко закодованих паролів не знайдено"
fi

echo ""

# ============================================
# 10. Docker Build Test (опціонально)
# ============================================
if command -v docker &> /dev/null && docker ps &> /dev/null; then
    echo "🐳 Тестова збірка Docker образу..."

    docker build -t book-catalog:test . --quiet

    if [ $? -eq 0 ]; then
        IMAGE_SIZE=$(docker images book-catalog:test --format "{{.Size}}")
        echo -e "${GREEN}✓${NC} Docker образ зібрано (розмір: $IMAGE_SIZE)"

        # Очистка тестового образу
        docker rmi book-catalog:test --force &> /dev/null
    else
        echo -e "${RED}✗${NC} Помилка збірки Docker образу!"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠${NC} Пропуск Docker збірки (Docker не доступний)"
fi

echo ""

# ============================================
# ПІДСУМОК
# ============================================
echo "================================================"
echo -e "${GREEN}✅ ВСІ ПЕРЕВІРКИ ПРОЙДЕНО!${NC}"
echo "================================================"
echo ""
echo "📝 Чек-лист перед деплоєм:"
echo ""
echo "   ☐ Створити PostgreSQL на Render"
echo "   ☐ Зберегти Database URL та credentials"
echo "   ☐ Створити Web Service на Render"
echo "   ☐ Налаштувати Environment Variables"
echo "   ☐ Перевірити логи запуску"
echo "   ☐ Протестувати веб-інтерфейс"
echo ""
echo "📚 Детальна інструкція: DEPLOYMENT_GUIDE.md"
echo "⚡ Швидкий старт: QUICK_START.md"
echo ""
echo "🚀 Готово до деплою!"
echo ""