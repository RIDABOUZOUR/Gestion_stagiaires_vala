# 📌 Application de Gestion des Stagiaires

## 📝 Description

Ce projet est une application web complète de **gestion des stagiaires**, développée dans le cadre de mon stage.
Elle couvre tout le parcours d'un stagiaire : **candidature → affectation à un stage → suivi (présences, rapports
hebdomadaires) → évaluation de fin de stage → attestation PDF**, avec des tableaux de bord statistiques et un accès
sécurisé par rôle (administrateur, ressources humaines, encadrant).

---

## ⚙️ Technologies utilisées

- **Backend** : Java 21, Spring Boot 3.5
- **Frontend** : Angular 20 (SSR), Tailwind CSS 4, Chart.js
- **Base de données** : MySQL 8 (H2 en mémoire pour les tests)
- **ORM** : Spring Data JPA (Specifications, `@EntityGraph`)
- **API** : RESTful, documentée avec Swagger (springdoc-openapi)
- **Sécurité** : Spring Security, jetons JWT (HS256), mots de passe BCrypt
- **Documents** : OpenPDF (attestations), export CSV
- **AOP / Logs** : Spring AOP (aspect de journalisation)
- **Tests** : JUnit 5, Mockito, MockMvc, Spring Security Test, JaCoCo ; Jasmine / Karma côté Angular
- **Conteneurisation** : Docker / Docker Compose

---

## 🚀 Fonctionnalités principales

### Gestion des données

- 🔹 Stagiaires, stages, encadrants et départements (ajout, modification, suppression, liste)
- 🔹 Recherche multicritère paginée et triable (mot-clé, département, stage, statut, organisme…)
- 🔹 Fiches détaillées : fiche stage (stagiaires, historique) et fiche stagiaire (onglets de suivi)
- 🔹 Export CSV des listes avec les filtres en cours (compatible Excel en français)

### Règles métier

- 🔹 **Cycle de vie d'un stage** : `PLANIFIE → EN_COURS → TERMINE`, ou `ANNULE` ; transitions contrôlées et
  cohérentes avec les dates, mise à jour automatique chaque nuit, **historique** de chaque changement (date, auteur)
- 🔹 Un stagiaire ne peut pas être affecté à un stage terminé ou annulé
- 🔹 Email unique par stagiaire et par encadrant, âge minimum au début du stage (configurable)
- 🔹 Capacité maximale de stages en cours par encadrant (optionnelle)
- 🔹 Suppressions protégées : impossible de supprimer un élément encore utilisé (message explicite)

### Parcours du stagiaire

- 🔹 **Candidatures** : dépôt public (`/postuler`, sans compte), étude, acceptation ou refus motivé ;
  l'acceptation crée automatiquement le stagiaire et l'affecte au stage choisi
- 🔹 **Présences** : calendrier mensuel (présent, retard, absent, absence justifiée) et taux de présence
- 🔹 **Rapports hebdomadaires** : dépôt, validation ou demande de révision par l'encadrant
- 🔹 **Évaluation de fin de stage** : note sur 20, 4 compétences, appréciation, recommandation
- 🔹 **Attestation de stage en PDF**, numérotée (`ATT-2026-0001`) et réémise à l'identique

### Statistiques (tableau de bord)

- 🔹 Indicateurs clés : stagiaires actifs, stages en cours, échéances, assiduité, note moyenne, taux d'acceptation
- 🔹 Alertes : stages qui se terminent dans les 30 jours, stagiaires sans rapport depuis 14 jours
- 🔹 Graphiques : stages par statut, candidatures sur 12 mois, répartition par département, filière, organisme,
  encadrant, sexe et niveau, assiduité, notes et compétences
- 🔹 Durée moyenne des stages, taux d'achèvement et d'annulation (global, par département, par encadrant)

### Sécurité et rôles

| Fonctionnalité                                         | ADMIN | RH  | ENCADRANT                    |
| ------------------------------------------------------ | :---: | :-: | ---------------------------- |
| Consulter stagiaires, stages, encadrants, départements |  ✔   | ✔  | ses stagiaires et stages     |
| Créer, modifier, supprimer ces données                 |  ✔   | ✔  | —                            |
| Présences, rapports, évaluation                        |  ✔   | ✔  | ses stagiaires uniquement    |
| Candidatures, statistiques, exports, attestations      |  ✔   | ✔  | —                            |
| Comptes utilisateurs                                   |  ✔   | —  | —                            |

Le dépôt d'une candidature et la connexion sont publics. Toute autre route est refusée sans jeton valide.

---

## 🐳 Démarrage avec Docker (recommandé)

### Prérequis

- Docker + Docker Compose v2

### Étapes

```bash
# 1. Créer le fichier d'environnement local
cp .env.example .env
#    puis éditer .env : mots de passe MySQL, et OBLIGATOIREMENT
#      JWT_SECRET      (générer avec : openssl rand -base64 48)
#      ADMIN_EMAIL / ADMIN_PASSWORD  (compte administrateur initial)

# 2. Construire et démarrer la stack
docker compose up --build

# 3. Accéder à l'application
#    Frontend : http://localhost:4000   (connexion avec ADMIN_EMAIL / ADMIN_PASSWORD)
#    Backend  : http://localhost:8080
#    Swagger  : http://localhost:8080/swagger-ui/index.html  (avec SPRINGDOC_ENABLED=true dans .env)
```

Le compte administrateur est créé au **premier démarrage**, si la base ne contient encore aucun utilisateur.
Les autres comptes (RH, encadrants) se créent ensuite depuis l'écran **Utilisateurs**.
Sans `JWT_SECRET`, Docker Compose refuse de démarrer.

Pour arrêter :

```bash
docker compose down          # conserve les données MySQL
docker compose down -v       # supprime aussi le volume de données
```

### Services

| Service  | Image / Build        | Port par défaut | Rôle                    |
| -------- | -------------------- | --------------- | ----------------------- |
| mysql    | `mysql:8.0`          | 3306            | Base de données         |
| backend  | `./backend_spring`   | 8080            | API Spring Boot         |
| frontend | `./frontend_angular` | 4000            | Interface Angular (SSR) |

Les ports sont configurables via `.env` (`MYSQL_PORT`, `BACKEND_PORT`, `FRONTEND_PORT`).

---

## 🔧 Démarrage manuel (développement)

**Backend** (nécessite un MySQL local et Maven 3.9+) :

```bash
cd backend_spring
export JWT_SECRET="$(openssl rand -base64 48)"
export ADMIN_EMAIL=admin@exemple.com ADMIN_PASSWORD=motdepasse-solide
export MYSQL_USER=... MYSQL_PASSWORD=...
mvn spring-boot:run
```

> ℹ️ Le wrapper `./mvnw` ne fonctionne pas depuis `backend_spring/` : sa configuration (`.mvn/`) se trouve à la
> racine du dépôt. Utilisez une installation locale de Maven.

**Frontend** :

```bash
cd frontend_angular
npm install
npm start        # http://localhost:4200, appelle l'API sur http://localhost:8080
```

---

## 🧪 Tests

### Backend

Les tests s'exécutent sur une base **H2 en mémoire** (profil `test`), sans MySQL ni aucune configuration externe.

```bash
cd backend_spring

mvn test       # lance uniquement les tests
mvn verify     # tests + rapport de couverture JaCoCo
mvn test -Dtest=StageServiceImplTest   # une seule classe
```

Le rapport de couverture est généré dans `backend_spring/target/site/jacoco/index.html`.

| Couche                         | Tests                                                                 |
| ------------------------------ | --------------------------------------------------------------------- |
| Services (`*ServiceImpl`)      | JUnit 5 + Mockito, horloge fixe pour les règles de date               |
| Contrôleurs REST               | MockMvc _standalone_ + validation Bean Validation                     |
| Requêtes JPA et statistiques   | `@DataJpaTest` sur H2 (filtres, pagination, projections JPQL)         |
| Sécurité                       | `@SpringBootTest` : matrice des droits avec de vrais jetons JWT       |
| Parcours complets              | Acceptation d'une candidature, export CSV, génération du PDF          |
| `GlobalExceptionHandler`       | Tests directs des handlers (400 / 401 / 404 / 409)                    |
| `LoggingAspect`                | Tests unitaires de l'aspect AOP                                       |

### Frontend

```bash
cd frontend_angular
npm test                                   # Karma + Jasmine
npx ng test --watch=false --browsers=ChromeHeadless   # exécution unique, sans fenêtre
```

Sans Chrome installé, indiquez un navigateur compatible via la variable `CHROME_BIN`.

---

## 🔐 Configuration & sécurité

Toute la configuration sensible est externalisée dans le fichier `.env` (non versionné) :

| Variable                        | Description                                                           |
| ------------------------------- | --------------------------------------------------------------------- |
| `MYSQL_ROOT_PASSWORD`           | Mot de passe root MySQL                                               |
| `MYSQL_DATABASE`                | Nom de la base                                                        |
| `MYSQL_USER` / `MYSQL_PASSWORD` | Identifiants applicatifs                                              |
| `JWT_SECRET`                    | **Obligatoire** : clé de signature des jetons (32 caractères minimum) |
| `JWT_DUREE`                     | Durée de validité d'une connexion (défaut : `8h`)                     |
| `ADMIN_EMAIL` / `ADMIN_PASSWORD` | Compte administrateur créé au premier démarrage                    |
| `CORS_ALLOWED_ORIGINS`          | Origines autorisées (séparées par des virgules)                       |
| `API_URL`                       | URL publique du backend (injectée au build frontend)                  |
| `ALLOWED_HOSTS`                 | Hôtes autorisés pour le rendu SSR (séparés par des virgules)          |
| `LOG_LEVEL_ASPECT`              | Niveau des logs de l'aspect AOP (`INFO` ou `DEBUG`)                   |
| `ORGANISATION_*`                | Nom, ville, fonction et signataire imprimés sur les attestations      |

Réglages optionnels des règles métier (valeurs par défaut entre parenthèses) :
`REGLE_AGE_MINIMUM` (16 ans, 0 = désactivé), `REGLE_MAX_STAGES_EN_COURS` (0 = illimité),
`STAGES_STATUS_CRON` (mise à jour des statuts chaque nuit à 00:05), `APP_SCHEDULING_ENABLED` (`true`).

> ℹ️ Le CORS est configuré de manière centralisée dans
> `backend_spring/.../config/CorsConfig.java` — plus aucune origine `*` n'est autorisée.

> ℹ️ L'API est sans état : le jeton est envoyé dans l'en-tête `Authorization: Bearer <jeton>`.
> Dans Swagger, obtenez un jeton avec `POST /auth/login` puis utilisez le bouton **Authorize**.

---

## 📊 Journalisation (Spring AOP)

Un aspect AOP (`backend_spring/.../aspect/LoggingAspect.java`) journalise automatiquement
toutes les méthodes des couches **`@Service`** et **`@RestController`**, ainsi que toute
méthode annotée `@Loggable`. Les mots de passe et les jetons sont masqués dans les journaux.

| Événement | Niveau  | Contenu                        |
| --------- | ------- | ------------------------------ |
| Entrée    | `DEBUG` | nom de la méthode + arguments  |
| Sortie    | `INFO`  | nom de la méthode + durée (ms) |
| Exception | `ERROR` | message + pile d'appels        |

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
│   └── src/main/java/com/vala/gestionStagiaires/
│       ├── controller/         # Routes REST
│       ├── service/            # Règles métier et statistiques
│       ├── repository/         # Spring Data JPA, Specifications, projections
│       ├── entities/ DTOs/     # Modèle et objets échangés
│       ├── securite/           # Spring Security, JWT, contrôles d'accès
│       ├── export/ pdf/        # Export CSV, attestation PDF
│       └── scheduler/          # Mise à jour automatique des statuts
└── frontend_angular/           # Interface Angular (SSR)
    ├── Dockerfile
    ├── src/server.ts           # Serveur Express (en-têtes de sécurité, CSP)
    ├── src/environments/       # Configuration par environnement (apiUrl)
    └── src/app/                # Écrans, services HTTP, gardes et intercepteur
```

---
