import { HttpClient } from '@angular/common/http';
import { Injectable, PLATFORM_ID, computed, inject, signal } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { Observable, map, tap } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginResponse, Role, Session } from '../models/auth.model';

const CLE_STOCKAGE = 'gestion-stagiaires.session';

/**
 * Session de l'utilisateur connecté.
 *
 * Le jeton est gardé en mémoire et dans le sessionStorage : il survit à un
 * rechargement de la page mais disparaît à la fermeture de l'onglet. Rien n'est
 * lu ni écrit pendant le rendu côté serveur.
 */
@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly navigateur = isPlatformBrowser(inject(PLATFORM_ID));
  private readonly session = signal<Session | null>(this.restaurer());
  private minuterieExpiration: ReturnType<typeof setTimeout> | null = null;

  readonly utilisateur = this.session.asReadonly();
  readonly estConnecte = computed(() => this.session() !== null);
  readonly role = computed<Role | null>(() => this.session()?.role ?? null);

  constructor() {
    this.programmerExpiration(this.session());
  }

  connecter(email: string, motDePasse: string): Observable<Session> {
    return this.http
      .post<LoginResponse>(`${environment.apiUrl}/auth/login`, { email, motDePasse })
      .pipe(
        map((r) => ({
          token: r.token,
          expiresAt: Date.parse(r.expiresAt),
          email: r.email,
          role: r.role,
          encadrantId: r.encadrantId,
        })),
        tap((session) => this.ouvrir(session)),
      );
  }

  deconnecter(): void {
    this.annulerMinuterie();
    this.session.set(null);
    if (this.navigateur) {
      try {
        sessionStorage.removeItem(CLE_STOCKAGE);
      } catch {
        // stockage indisponible (navigation privée stricte) : la session en mémoire suffit
      }
    }
  }

  /** Jeton à envoyer à l'API, ou null s'il n'y en a pas (ou s'il a expiré). */
  get token(): string | null {
    const session = this.session();
    if (!session) {
      return null;
    }
    if (session.expiresAt <= Date.now()) {
      this.deconnecter();
      return null;
    }
    return session.token;
  }

  aRole(...roles: Role[]): boolean {
    const role = this.role();
    return role !== null && roles.includes(role);
  }

  /** Page d'accueil selon le rôle : le tableau de bord est réservé à la gestion. */
  accueil(): string {
    if (!this.estConnecte()) {
      return '/login';
    }
    return this.aRole('ENCADRANT') ? '/stagiaires' : '/dashboard';
  }

  private ouvrir(session: Session): void {
    this.session.set(session);
    this.programmerExpiration(session);
    if (this.navigateur) {
      try {
        sessionStorage.setItem(CLE_STOCKAGE, JSON.stringify(session));
      } catch {
        // idem : la session reste valable pour cet onglet
      }
    }
  }

  /**
   * Ferme la session à l'heure d'expiration du jeton, même sans appel à l'API :
   * sinon les guards laisseraient naviguer jusqu'au premier 401.
   */
  private programmerExpiration(session: Session | null): void {
    this.annulerMinuterie();
    if (!this.navigateur || !session) {
      return;
    }
    // setTimeout est limité à ~24,8 jours ; au-delà, on reprogramme à l'échéance.
    const delai = Math.min(Math.max(session.expiresAt - Date.now(), 0), 2_147_000_000);
    this.minuterieExpiration = setTimeout(() => {
      if (session.expiresAt > Date.now()) {
        this.programmerExpiration(session);
        return;
      }
      this.deconnecter();
      this.router.navigate(['/login'], { queryParams: { expire: 1 } });
    }, delai);
  }

  private annulerMinuterie(): void {
    if (this.minuterieExpiration !== null) {
      clearTimeout(this.minuterieExpiration);
      this.minuterieExpiration = null;
    }
  }

  private restaurer(): Session | null {
    if (!this.navigateur) {
      return null;
    }
    try {
      const brut = sessionStorage.getItem(CLE_STOCKAGE);
      const session = brut ? (JSON.parse(brut) as Session) : null;
      return session && session.token && session.expiresAt > Date.now() ? session : null;
    } catch {
      return null;
    }
  }
}
