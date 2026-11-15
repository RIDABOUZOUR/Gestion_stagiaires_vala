# 📌 Application de Gestion des Stagiaires

## 📝 Description
Ce projet est une application web complète de **gestion des stagiaires**, développée dans le cadre de mon stage.  
Elle permet de gérer les stagiaires, stages, départements et encadrants, avec une interface utilisateur simple et moderne.

---

## ⚙️ Technologies utilisées
- **Backend** : Java 21, Spring Boot 3.5  
- **Frontend** : Angular 20 (SSR), Bootstrap 5  
- **Base de données** : MySQL 8  
- **ORM** : Spring Data JPA  
- **API** : RESTful (Spring Boot)  
- **AOP / Logs** : Spring AOP (aspect de journalisation)  
- **Tests** : JUnit 5, Mockito, MockMvc, JaCoCo  
- **Conteneurisation** : Docker / Docker Compose  

---

## 🚀 Fonctionnalités principales
- 🔹 Gestion des stagiaires (ajout, modification, suppression, liste)  
- 🔹 Gestion des stages et départements  
- 🔹 Gestion des encadrants  
- 🔹 Recherche et filtrage des données  
- 🔹 Communication frontend-backend via HTTPClient  
- 🔹 Tableau de bord statistique (Chart.js)  

---

## 🐳 Démarrage avec Docker (recommandé)

### Prérequis
- Docker + Docker Compose v2

### Étapes
```bash
# 1. Créer le fichier d'environnement local
cp .env.example .env
#    puis éditer .env et définir des mots de passe forts

# 2. Construire et démarrer la stack
docker compose up --build

# 3. Accéder à l'application
#    Frontend : http://localhost:4000
#    Backend  : http://localhost:8080
#    Swagger  : http://localhost:8080/swagger-ui/index.html
```

Pour arrêter :
```bash
docker compose down          # conserve les données MySQL
docker compose down -v       # supprime aussi le volume de données
```

### Services
| Service  | Image / Build          | Port par défaut | Rôle                       |
|----------|------------------------|-----------------|----------------------------|
| mysql    | `mysql:8.0`            | 3306            | Base de données            |
| backend  | `./backend_spring`     | 8080            | API Spring Boot            |
| frontend | `./frontend_angular`   | 4000            | Interface Angular (SSR)    |

Les ports sont configurables via `.env` (`MYSQL_PORT`, `BACKEND_PORT`, `FRONTEND_PORT`).

---

## 🔧 Démarrage manuel (développement)

**Backend** (nécessite un MySQL local) :
```bash
cd backend_spring
./mvnw spring-boot:run
```

**Frontend** :
```bash
cd frontend_angular
npm install
npm start        # http://localhost:4200
```

---

## 🧪 Tests (backend)

Les tests unitaires s'exécutent sur une base **H2 en mémoire** (profil `test`),
sans MySQL ni aucune configuration externe.

```bash
cd backend_spring

./mvnw test       # lance uniquement les tests
./mvnw verify     # tests + rapport de couverture JaCoCo
```

Le rapport de couverture est généré dans
`backend_spring/target/site/jacoco/index.html`.

Couverture :

| Couche | Tests |
|--------|-------|
| Services (`*ServiceImpl`) | JUnit 5 + Mockito (`@Mock` / `@InjectMocks`) |
| Contrôleurs REST | MockMvc *standalone* + validation Bean Validation |
| `GlobalExceptionHandler` | Tests directs des handlers (400 / 404 / 409) |
| `LoggingAspect` | Tests unitaires de l'aspect AOP |

---

## 🔐 Configuration & sécurité

Toute la configuration sensible est externalisée dans le fichier `.env` (non versionné) :

| Variable | Description |
|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | Mot de passe root MySQL |
| `MYSQL_DATABASE` | Nom de la base |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Identifiants applicatifs |
| `CORS_ALLOWED_ORIGINS` | Origines autorisées (séparées par des virgules) |
| `API_URL` | URL publique du backend (injectée au build frontend) |
| `ALLOWED_HOSTS` | Hôtes autorisés pour le rendu SSR (séparés par des virgules) |
| `LOG_LEVEL_ASPECT` | Niveau des logs de l'aspect AOP (`INFO` ou `DEBUG`) |

> ℹ️ Le CORS est configuré de manière centralisée dans
> `backend_spring/.../config/CorsConfig.java` — plus aucune origine `*` n'est autorisée.

---

## 📊 Journalisation (Spring AOP)

Un aspect AOP (`backend_spring/.../aspect/LoggingAspect.java`) journalise automatiquement
toutes les méthodes des couches **`@Service`** et **`@RestController`**, ainsi que toute
méthode annotée `@Loggable`.

| Événement | Niveau | Contenu |
|-----------|--------|---------|
| Entrée    | `DEBUG` | nom de la méthode + arguments |
| Sortie    | `INFO`  | nom de la méthode + durée (ms) |
| Exception | `ERROR` | message + pile d'appels |

Le niveau se règle via la variable `LOG_LEVEL_ASPECT` (`.env`) :

```bash
LOG_LEVEL_ASPECT=DEBUG docker compose up -d backend   # affiche aussi arguments et résultats
```

Exemple de sortie :
```
INFO  c.v.g.aspect.LoggingAspect : ← DepartementServiceImpl.getDepartement terminé en 12 ms
DEBUG c.v.g.aspect.LoggingAspect : → DepartementController.addDepartement : [DepartementDto(id=null, nom=DebugLog, ...)]
```

---

## 📁 Structure du projet
```
.
├── docker-compose.yml          # Orchestration MySQL + Backend + Frontend
├── .env.example                # Template des variables d'environnement
├── .gitignore
├── backend_spring/             # API Spring Boot
│   ├── Dockerfile
│   └── src/main/resources/
│       ├── application.properties
│       └── application-docker.properties
└── frontend_angular/           # Interface Angular (SSR)
    ├── Dockerfile
    └── src/environments/       # Configuration par environnement (apiUrl)
```

---
