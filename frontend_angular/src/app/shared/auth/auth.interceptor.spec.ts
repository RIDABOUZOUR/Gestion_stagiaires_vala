import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Router, provideRouter } from '@angular/router';
import { environment } from '../../../environments/environment';
import { AuthService } from '../../services/auth';
import { authInterceptor } from './auth.interceptor';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;
  let auth: jasmine.SpyObj<Pick<AuthService, 'deconnecter'>> & { token: string | null };
  let router: Router;

  beforeEach(() => {
    auth = { token: 'jeton', deconnecter: jasmine.createSpy('deconnecter') };
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        { provide: AuthService, useValue: auth },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    spyOn(router, 'navigate').and.resolveTo(true);
  });

  afterEach(() => controller.verify());

  it("ajoute le jeton aux appels vers l'API", () => {
    http.get(`${environment.apiUrl}/stagiaires`).subscribe();

    const req = controller.expectOne(`${environment.apiUrl}/stagiaires`);
    expect(req.request.headers.get('Authorization')).toBe('Bearer jeton');
    req.flush([]);
  });

  it("n'envoie jamais le jeton à un autre domaine", () => {
    http.get('https://autre-site.example/donnees').subscribe();

    const req = controller.expectOne('https://autre-site.example/donnees');
    expect(req.request.headers.has('Authorization')).toBeFalse();
    req.flush({});
  });

  it('sur un 401, ferme la session et renvoie vers la connexion', () => {
    http.get(`${environment.apiUrl}/stagiaires`).subscribe({ error: () => undefined });

    controller.expectOne(`${environment.apiUrl}/stagiaires`).flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.deconnecter).toHaveBeenCalled();
    expect(router.navigate).toHaveBeenCalledWith(['/login'], jasmine.objectContaining({ queryParams: jasmine.objectContaining({ expire: 1 }) }));
  });

  it("un 401 sur la connexion elle-même est laissé au formulaire", () => {
    http.post(`${environment.apiUrl}/auth/login`, {}).subscribe({ error: () => undefined });

    controller.expectOne(`${environment.apiUrl}/auth/login`).flush({}, { status: 401, statusText: 'Unauthorized' });

    expect(auth.deconnecter).not.toHaveBeenCalled();
    expect(router.navigate).not.toHaveBeenCalled();
  });
});
