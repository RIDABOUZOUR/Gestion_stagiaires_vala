import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { ActivatedRoute, Router, convertToParamMap } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Session } from '../models/auth.model';
import { AuthService } from '../services/auth';
import { TEST_IMPORTS, TEST_PROVIDERS } from '../shared/testing/test-utils';
import { Login } from './login';

const SESSION: Session = { token: 'jeton', expiresAt: Date.now() + 3_600_000, email: 'rh@vala.ma', role: 'RH', encadrantId: null };

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let component: Login;
  let auth: jasmine.SpyObj<AuthService>;
  let router: Router;

  async function creer(queryParams: Record<string, string> = {}): Promise<void> {
    auth = jasmine.createSpyObj<AuthService>('AuthService', ['connecter', 'accueil']);
    auth.accueil.and.returnValue('/dashboard');
    await TestBed.configureTestingModule({
      declarations: [Login],
      imports: TEST_IMPORTS,
      providers: [
        ...TEST_PROVIDERS,
        { provide: AuthService, useValue: auth },
        { provide: ActivatedRoute, useValue: { snapshot: { queryParamMap: convertToParamMap(queryParams) } } },
      ],
    }).compileComponents();
    router = TestBed.inject(Router);
    spyOn(router, 'navigateByUrl').and.resolveTo(true);
    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    fixture.detectChanges();
  }

  it("n'appelle pas l'API si le formulaire est invalide", async () => {
    await creer();
    component.seConnecter();
    expect(auth.connecter).not.toHaveBeenCalled();
    expect(component.isInvalid('email')).toBeTrue();
  });

  it('connecte puis redirige vers la page demandée', async () => {
    await creer({ redirect: '/stages' });
    auth.connecter.and.returnValue(of(SESSION));
    component.form.setValue({ email: 'rh@vala.ma', motDePasse: 'secret123' });

    component.seConnecter();

    expect(auth.connecter).toHaveBeenCalledWith('rh@vala.ma', 'secret123');
    expect(router.navigateByUrl).toHaveBeenCalledWith('/stages');
  });

  it("ignore une redirection externe et va à l'accueil du rôle", async () => {
    await creer({ redirect: '//pirate.example' });
    auth.connecter.and.returnValue(of(SESSION));
    component.form.setValue({ email: 'rh@vala.ma', motDePasse: 'secret123' });

    component.seConnecter();

    expect(router.navigateByUrl).toHaveBeenCalledWith('/dashboard');
  });

  it("affiche l'erreur de l'API et vide le mot de passe", async () => {
    await creer();
    auth.connecter.and.returnValue(throwError(() => new HttpErrorResponse({
      status: 401, error: { message: 'Email ou mot de passe incorrect.' },
    })));
    component.form.setValue({ email: 'rh@vala.ma', motDePasse: 'mauvais' });

    component.seConnecter();
    fixture.detectChanges();

    expect(component.erreur).toBe('Email ou mot de passe incorrect.');
    expect(component.form.value.motDePasse).toBe('');
    expect((fixture.nativeElement as HTMLElement).querySelector('[role="alert"]')?.textContent).toContain('incorrect');
  });

  it('signale une session expirée', async () => {
    await creer({ expire: '1' });
    expect((fixture.nativeElement as HTMLElement).textContent).toContain('Votre session a expiré');
  });
});
