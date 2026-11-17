import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../services/auth';
import { extractApiError } from '../shared/api-error';
import { redirectionInterne } from '../shared/auth/auth.guards';

@Component({
  selector: 'app-login',
  standalone: false,
  templateUrl: './login.html',
})
export class Login implements OnInit {
  form!: FormGroup;
  enCours = false;
  erreur: string | null = null;
  sessionExpiree = false;

  constructor(
    private readonly fb: FormBuilder,
    private readonly auth: AuthService,
    private readonly router: Router,
    private readonly route: ActivatedRoute,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required]],
    });
    this.sessionExpiree = this.route.snapshot.queryParamMap.has('expire');
  }

  isInvalid(field: string): boolean {
    const control = this.form.get(field);
    return !!control && control.invalid && (control.touched || control.dirty);
  }

  seConnecter(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.erreur = null;
    this.sessionExpiree = false;
    this.enCours = true;
    const { email, motDePasse } = this.form.value;
    this.auth
      .connecter(email.trim(), motDePasse)
      .pipe(finalize(() => (this.enCours = false)))
      .subscribe({
        next: () => {
          const demande = redirectionInterne(this.route.snapshot.queryParamMap.get('redirect'));
          void this.router.navigateByUrl(demande ?? this.auth.accueil());
        },
        error: (err) => {
          this.erreur = extractApiError(err);
          this.form.patchValue({ motDePasse: '' });
        },
      });
  }
}
