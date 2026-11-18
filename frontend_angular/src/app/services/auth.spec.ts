import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { AuthService } from './auth';

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;

  const dansUneHeure = () => new Date(Date.now() + 3_600_000).toISOString();

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    http.verify();
    sessionStorage.clear();
  });

  function connecter(role: 'ADMIN' | 'RH' | 'ENCADRANT', expiresAt = dansUneHeure()): void {
    service.connecter('paul@vala.ma', 'secret123').subscribe();
    const req = http.expectOne(`${environment.apiUrl}/auth/login`);
    expect(req.request.body).toEqual({ email: 'paul@vala.ma', motDePasse: 'secret123' });
    req.flush({ token: 'jeton', expiresAt, email: 'paul@vala.ma', role, encadrantId: role === 'ENCADRANT' ? 9 : null });
  }

  it('ouvre la session et la conserve dans le sessionStorage', () => {
    connecter('RH');

    expect(service.estConnecte()).toBeTrue();
    expect(service.token).toBe('jeton');
    expect(service.aRole('ADMIN', 'RH')).toBeTrue();
    expect(sessionStorage.getItem('gestion-stagiaires.session')).toContain('jeton');
  });

  it("dirige l'encadrant vers ses stagiaires et la gestion vers le tableau de bord", () => {
    expect(service.accueil()).toBe('/login');
    connecter('ENCADRANT');
    expect(service.accueil()).toBe('/stagiaires');
    service.deconnecter();
    connecter('ADMIN');
    expect(service.accueil()).toBe('/dashboard');
  });

  it('ferme la session à la déconnexion', () => {
    connecter('RH');

    service.deconnecter();

    expect(service.estConnecte()).toBeFalse();
    expect(service.token).toBeNull();
    expect(sessionStorage.getItem('gestion-stagiaires.session')).toBeNull();
  });

  it("ne renvoie plus un jeton expiré et ferme alors la session", () => {
    connecter('RH', new Date(Date.now() - 1000).toISOString());

    expect(service.token).toBeNull();
    expect(service.estConnecte()).toBeFalse();
  });

  describe("à l'expiration du jeton", () => {
    beforeEach(() => jasmine.clock().install());
    afterEach(() => jasmine.clock().uninstall());

    it('ferme la session et renvoie vers la connexion sans attendre un appel API', () => {
      const navigate = spyOn(TestBed.inject(Router), 'navigate').and.resolveTo(true);
      connecter('RH', new Date(Date.now() + 1_000).toISOString());
      expect(service.estConnecte()).toBeTrue();

      jasmine.clock().mockDate(new Date(Date.now() + 1_001));
      jasmine.clock().tick(1_001);

      expect(service.estConnecte()).toBeFalse();
      expect(navigate).toHaveBeenCalledWith(['/login'], { queryParams: { expire: 1 } });
    });
  });
});
