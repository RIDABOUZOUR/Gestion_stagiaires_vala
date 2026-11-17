import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Toutes les pages dépendent de l'utilisateur connecté, dont le jeton n'existe
  // que dans le navigateur : le serveur renvoie la coquille de l'application et
  // le rendu se fait côté client (pas d'appel à l'API sans jeton pendant le SSR).
  {
    path: '**',
    renderMode: RenderMode.Client
  }
];
