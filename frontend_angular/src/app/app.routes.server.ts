import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  // Rendu SSR à la demande : adapté aux routes dynamiques (edit-*/:id)
  // qui ne peuvent pas être pré-rendues sans getPrerenderParams.
  {
    path: '**',
    renderMode: RenderMode.Server
  }
];
