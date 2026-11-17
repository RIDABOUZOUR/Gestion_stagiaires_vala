import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth';

/**
 * Ajoute le jeton aux appels vers l'API (et seulement vers elle). Sur un 401,
 * la session est fermée et l'utilisateur renvoyé vers la page de connexion.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const versApi = req.url.startsWith(environment.apiUrl);
  const token = versApi ? auth.token : null;
  const requete = token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(requete).pipe(
    catchError((erreur: unknown) => {
      const connexion = req.url.endsWith('/auth/login');
      if (erreur instanceof HttpErrorResponse && erreur.status === 401 && versApi && !connexion) {
        auth.deconnecter();
        const retour = router.url.startsWith('/login') ? undefined : router.url;
        void router.navigate(['/login'], { queryParams: { expire: 1, redirect: retour } });
      }
      return throwError(() => erreur);
    }),
  );
};
