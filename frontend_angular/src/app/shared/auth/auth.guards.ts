import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../../models/auth.model';
import { AuthService } from '../../services/auth';

/**
 * Route réservée aux utilisateurs connectés ; si la route déclare
 * {@code data: { roles: [...] }}, le rôle doit en faire partie.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  if (!auth.estConnecte()) {
    return router.createUrlTree(['/login'], { queryParams: { redirect: state.url } });
  }
  const roles = route.data['roles'] as Role[] | undefined;
  if (roles && !auth.aRole(...roles)) {
    return router.createUrlTree([auth.accueil()]);
  }
  return true;
};

/** Page de connexion : un utilisateur déjà connecté va directement à son accueil. */
export const invitesGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.estConnecte() ? inject(Router).createUrlTree([auth.accueil()]) : true;
};

/** Racine du site : redirige vers l'accueil adapté au rôle (ou la connexion). */
export const accueilGuard: CanActivateFn = () => inject(Router).createUrlTree([inject(AuthService).accueil()]);

/** N'accepte qu'une adresse interne à l'application (évite les redirections ouvertes). */
export function redirectionInterne(url: string | null | undefined): string | null {
  return url && url.startsWith('/') && !url.startsWith('//') && !url.startsWith('/login') ? url : null;
}
