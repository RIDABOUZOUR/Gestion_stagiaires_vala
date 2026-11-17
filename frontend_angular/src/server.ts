import {
  AngularNodeAppEngine,
  createNodeRequestHandler,
  isMainModule,
  writeResponseToNodeResponse,
} from '@angular/ssr/node';
import express from 'express';
import { join } from 'node:path';
import { environment } from './environments/environment';

const browserDistFolder = join(import.meta.dirname, '../browser');

const app = express();

/**
 * Hôtes autorisés pour le rendu SSR (protection anti-SSRF d'Angular).
 * Configurable via ALLOWED_HOSTS (liste séparée par des virgules). Par défaut,
 * seuls le développement local sont autorisés : en production, renseigner le
 * domaine public, sinon Angular retombe sur un rendu côté client.
 */
const allowedHosts = (process.env['ALLOWED_HOSTS'] ?? 'localhost,127.0.0.1')
  .split(',')
  .map((host) => host.trim())
  .filter((host) => host.length > 0);

const angularApp = new AngularNodeAppEngine({ allowedHosts });

// Derrière un reverse-proxy (docker-compose, ingress…), faire confiance au
// premier hop permet à Express de résoudre correctement l'IP cliente et le
// protocole (utile pour les logs et les cookies sécurisés).
app.set('trust proxy', 1);
app.disable('x-powered-by');

/**
 * En-têtes de sécurité appliqués à toutes les réponses.
 * `connect-src` autorise l'origine du backend : API_ORIGIN si elle est définie,
 * sinon celle de `environment.apiUrl` (l'API que l'application appelle réellement).
 * Sans cette valeur par défaut, `npm start` bloquait tous les appels à l'API.
 */
const apiOrigin = process.env['API_ORIGIN'] || origineDe(environment.apiUrl);

function origineDe(url: string): string | undefined {
  try {
    return new URL(url).origin;
  } catch {
    return undefined;
  }
}
const connectSrc = ["'self'", apiOrigin].filter(Boolean).join(' ');

const contentSecurityPolicy = [
  "default-src 'self'",
  "base-uri 'self'",
  "object-src 'none'",
  "frame-ancestors 'none'",
  "form-action 'self'",
  "img-src 'self' data:",
  "font-src 'self' https://fonts.gstatic.com data:",
  // Angular inligne le CSS critique et les polices Google fournissent une feuille de style.
  "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
  // Angular SSR (event replay) injecte un script inline avant l'hydratation.
  "script-src 'self' 'unsafe-inline'",
  `connect-src ${connectSrc}`,
  'upgrade-insecure-requests',
].join('; ');

app.use((_req, res, next) => {
  res.setHeader('Content-Security-Policy', contentSecurityPolicy);
  res.setHeader('X-Content-Type-Options', 'nosniff');
  res.setHeader('X-Frame-Options', 'DENY');
  res.setHeader('Referrer-Policy', 'strict-origin-when-cross-origin');
  res.setHeader('Cross-Origin-Opener-Policy', 'same-origin');
  res.setHeader('X-DNS-Prefetch-Control', 'off');
  res.setHeader('Strict-Transport-Security', 'max-age=31536000; includeSubDomains');
  next();
});

/**
 * Sert les fichiers statiques du dossier /browser.
 */
app.use(
  express.static(browserDistFolder, {
    maxAge: '1y',
    index: false,
    redirect: false,
    dotfiles: 'ignore',
  }),
);

/**
 * Toutes les autres requêtes sont rendues par l'application Angular (SSR).
 */
app.use((req, res, next) => {
  angularApp
    .handle(req)
    .then((response) => (response ? writeResponseToNodeResponse(response, res) : next()))
    .catch(next);
});

/**
 * Démarre le serveur lorsqu'il est exécuté directement.
 * Le port provient de la variable d'environnement PORT (4000 par défaut).
 */
if (isMainModule(import.meta.url)) {
  const port = Number(process.env['PORT'] ?? 4000);
  app.listen(port, (error) => {
    if (error) {
      throw error;
    }

    console.log(`Node Express server listening on http://localhost:${port}`);
  });
}

/**
 * Handler de requêtes utilisé par le CLI Angular (dev-server, build) ou Firebase.
 */
export const reqHandler = createNodeRequestHandler(app);
