# Frontend : gestion des stagiaires

Interface Angular 20 de l'application de gestion des stagiaires. Elle consomme l'API Spring Boot de `../backend_spring`.

- **Architecture** : NgModule. Les écrans sont déclarés dans `src/app/app-module.ts` et les routes dans `src/app/app-routing-module.ts`.
- **Style** : Tailwind CSS v4 via PostCSS (`.postcssrc.json`). Les jetons de couleur et les couches communes se trouvent dans `src/styles.css`.
- **Serveur** : Express (`src/server.ts`). Il sert l'application et ajoute les en-têtes de sécurité, dont la CSP. Le rendu se fait côté navigateur (`RenderMode.Client` dans `src/app/app.routes.server.ts`), car toutes les pages dépendent du jeton de l'utilisateur connecté.

## Commandes

```bash
npm install
npm start                               # ng serve → http://localhost:4200 (API : environment.apiUrl)
npm run build                           # bundles navigateur + serveur dans dist/frontend_angular/
npm run serve:ssr:frontend_angular      # lance le serveur Express construit (port 4000)
npm test                                # Karma + Jasmine
npx ng test --watch=false --browsers=ChromeHeadless --include='src/app/services/stage.spec.ts'
```

Karma a besoin de Chrome. S'il n'est pas installé sur le système, un Chrome headless peut être placé dans `chrome-headless-shell/` (ce dossier est ignoré par git). Indiquez alors le chemin de son binaire dans `CHROME_BIN`.

## Configuration

| Variable | Où | Rôle |
|---|---|---|
| `API_URL` | build Docker (`ARG`) | URL de l'API, écrite dans `environment.prod.ts` au build. **Toute modification impose de reconstruire l'image.** |
| `API_ORIGIN` | exécution du serveur | Origine autorisée par la CSP (`connect-src`). Par défaut, elle est déduite de `apiUrl`. |
| `ALLOWED_HOSTS` | exécution du serveur | Hôtes acceptés par le serveur Angular (protection anti-SSRF). Par défaut : `localhost,127.0.0.1`. |
| `PORT` | exécution du serveur | Port d'écoute. Par défaut : `4000`. |

En développement, l'URL de l'API se règle dans `src/environments/environment.ts`.

Le serveur envoie `upgrade-insecure-requests` et HSTS. Si le site est servi en HTTPS, `API_URL` doit donc être en `https://`.

## Rôles et accès

L'authentification utilise un jeton JWT, gardé en mémoire et dans le `sessionStorage`. La session est fermée automatiquement à l'expiration du jeton ou sur une réponse 401.

| Rôle | Accès |
|---|---|
| ADMIN | tout, y compris la gestion des comptes utilisateurs |
| RH | gestion métier : stagiaires, stages, encadrants, départements, candidatures, statistiques, exports, attestations |
| ENCADRANT | consultation de **ses** stagiaires ; suivi des présences, rapports hebdomadaires et évaluations |

Les routes sont protégées par les guards de `src/app/shared/auth/`. Le backend reste toutefois la seule vraie barrière d'autorisation.

## Conventions

- **Messages à l'utilisateur** : `NotificationService` pour les toasts et `ConfirmService` pour les confirmations. Ne jamais utiliser `alert()` ni `confirm()`.
- **Erreurs de l'API** : les afficher avec `extractApiError()` (`src/app/shared/api-error.ts`).
- **Code propre au navigateur** (Chart.js, stockage, etc.) : le protéger avec `isPlatformBrowser`.
- **Nouvel hôte externe** (polices, API, etc.) : l'ajouter à la CSP dans `src/server.ts`.
- **Tests de composants** : utiliser `TEST_IMPORTS` et `TEST_PROVIDERS` de `src/app/shared/testing/test-utils.ts`.
