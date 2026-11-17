import { TestBed } from '@angular/core/testing';
import { ActivatedRouteSnapshot, Router, RouterStateSnapshot, UrlTree, provideRouter } from '@angular/router';
import { Role } from '../../models/auth.model';
import { AuthService } from '../../services/auth';
import { accueilGuard, authGuard, invitesGuard, redirectionInterne } from './auth.guards';

describe('Gardes d’authentification', () => {
  let connecte: boolean;
  let role: Role | null;

  const auth = {
    estConnecte: () => connecte,
    aRole: (...roles: Role[]) => role !== null && roles.includes(role),
    accueil: () => (!connecte ? '/login' : role === 'ENCADRANT' ? '/stagiaires' : '/dashboard'),
  };

  beforeEach(() => {
    connecte = false;
    role = null;
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: auth }],
    });
  });

  const route = (roles?: Role[]) => ({ data: roles ? { roles } : {} }) as unknown as ActivatedRouteSnapshot;
  const etat = (url: string) => ({ url }) as RouterStateSnapshot;
  const url = (resultat: unknown) => TestBed.inject(Router).serializeUrl(resultat as UrlTree);

  it('renvoie un visiteur vers la connexion en gardant la page demandée', () => {
    const resultat = TestBed.runInInjectionContext(() => authGuard(route(), etat('/stages')));
    expect(url(resultat)).toBe('/login?redirect=%2Fstages');
  });

  it('laisse passer un utilisateur connecté', () => {
    connecte = true;
    role = 'ENCADRANT';
    expect(TestBed.runInInjectionContext(() => authGuard(route(), etat('/stages')))).toBeTrue();
  });

  it("renvoie vers son accueil un utilisateur qui n'a pas le rôle requis", () => {
    connecte = true;
    role = 'ENCADRANT';
    const resultat = TestBed.runInInjectionContext(() => authGuard(route(['ADMIN', 'RH']), etat('/dashboard')));
    expect(url(resultat)).toBe('/stagiaires');
  });

  it('la racine et la page de connexion mènent à l’accueil du rôle', () => {
    connecte = true;
    role = 'RH';
    expect(url(TestBed.runInInjectionContext(() => accueilGuard(route(), etat('/'))))).toBe('/dashboard');
    expect(url(TestBed.runInInjectionContext(() => invitesGuard(route(), etat('/login'))))).toBe('/dashboard');
  });

  it("n'accepte que des redirections internes", () => {
    expect(redirectionInterne('/stages')).toBe('/stages');
    expect(redirectionInterne('//pirate.example')).toBeNull();
    expect(redirectionInterne('https://pirate.example')).toBeNull();
    expect(redirectionInterne('/login')).toBeNull();
    expect(redirectionInterne(null)).toBeNull();
  });
});
