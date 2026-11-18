import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { LIBELLES_ROLE, Role } from '../models/auth.model';
import { EncadrantModel } from '../models/stage.model';
import { Utilisateur } from '../models/utilisateur.model';
import { AuthService } from '../services/auth';
import { Encadrant } from '../services/encadrant';
import { UtilisateurService } from '../services/utilisateur';
import { extractApiError } from '../shared/api-error';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { NotificationService } from '../shared/notification/notification.service';

@Component({
  selector: 'app-utilisateurs',
  standalone: false,
  templateUrl: './utilisateurs.html',
})
export class Utilisateurs implements OnInit {
  readonly roles: Role[] = ['ADMIN', 'RH', 'ENCADRANT'];
  readonly libellesRole = LIBELLES_ROLE;

  utilisateurs: Utilisateur[] = [];
  encadrants: EncadrantModel[] = [];
  loading = false;
  formulaireOuvert = false;
  enregistrement = false;
  /** Une action sur un compte existant (rôle, activation, mot de passe, suppression) est en cours. */
  action = false;
  form!: FormGroup;

  /** Réinitialisation du mot de passe en cours : compte et nouveau mot de passe saisi. */
  reinitialisation: { id: number; motDePasse: string } | null = null;

  constructor(
    private readonly fb: FormBuilder,
    private readonly utilisateurService: UtilisateurService,
    private readonly encadrantService: Encadrant,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
    readonly auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
      role: ['RH' as Role, [Validators.required]],
      encadrantId: [null as number | null],
    });
    this.encadrantService.getEncadrants().subscribe({
      next: (e) => (this.encadrants = e),
      error: (err) => this.notifications.error(extractApiError(err)),
    });
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.utilisateurService
      .lister()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (u) => (this.utilisateurs = u),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  estMoi(u: Utilisateur): boolean {
    return u.email.toLowerCase() === this.auth.utilisateur()?.email.toLowerCase();
  }

  /** Fiches encadrant pas encore liées à un compte (plus celle du compte courant). */
  encadrantsDisponibles(courant: number | null = null): EncadrantModel[] {
    const lies = new Set(this.utilisateurs.map((u) => u.encadrantId).filter((id) => id !== null && id !== courant));
    return this.encadrants.filter((e) => !lies.has(e.id));
  }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!c && c.invalid && (c.touched || c.dirty);
  }

  creer(): void {
    const v = this.form.value;
    if (this.form.invalid || (v.role === 'ENCADRANT' && !v.encadrantId)) {
      this.form.markAllAsTouched();
      if (v.role === 'ENCADRANT' && !v.encadrantId) {
        this.notifications.error('Choisissez la fiche encadrant liée à ce compte.');
      }
      return;
    }
    this.enregistrement = true;
    this.utilisateurService
      .creer({
        email: v.email.trim(),
        motDePasse: v.motDePasse,
        role: v.role,
        encadrantId: v.role === 'ENCADRANT' ? Number(v.encadrantId) : null,
      })
      .pipe(finalize(() => (this.enregistrement = false)))
      .subscribe({
        next: (u) => {
          this.utilisateurs = [...this.utilisateurs, u].sort((a, b) => a.email.localeCompare(b.email));
          this.form.reset({ email: '', motDePasse: '', role: 'RH', encadrantId: null });
          this.formulaireOuvert = false;
          this.notifications.success(`Compte ${u.email} créé.`);
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  /** Change le rôle ou active / désactive un compte. */
  modifier(u: Utilisateur, changement: { role?: Role; actif?: boolean }): void {
    const role = changement.role ?? u.role;
    if (role === 'ENCADRANT' && !u.encadrantId) {
      this.notifications.error('Pour en faire un compte encadrant, supprimez-le et recréez-le en choisissant la fiche encadrant.');
      this.charger();
      return;
    }
    this.action = true;
    this.utilisateurService
      .modifier(u.id, { role, actif: changement.actif ?? u.actif, encadrantId: role === 'ENCADRANT' ? u.encadrantId : null })
      .pipe(finalize(() => (this.action = false)))
      .subscribe({
        next: (maj) => {
          this.utilisateurs = this.utilisateurs.map((x) => (x.id === maj.id ? maj : x));
          this.notifications.success(`Compte ${maj.email} mis à jour.`);
        },
        error: (err) => {
          this.notifications.error(extractApiError(err));
          this.charger(); // rétablit la valeur affichée
        },
      });
  }

  changerMotDePasse(): void {
    const r = this.reinitialisation;
    if (!r || r.motDePasse.length < 8 || r.motDePasse.length > 72) {
      this.notifications.error('Le mot de passe doit contenir entre 8 et 72 caractères.');
      return;
    }
    this.action = true;
    this.utilisateurService.changerMotDePasse(r.id, r.motDePasse).pipe(finalize(() => (this.action = false))).subscribe({
      next: () => {
        this.reinitialisation = null;
        this.notifications.success('Mot de passe modifié.');
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  async supprimer(u: Utilisateur): Promise<void> {
    if (this.action) {
      return;
    }
    const ok = await this.confirm.confirm({
      title: 'Supprimer le compte',
      message: `Le compte ${u.email} ne pourra plus se connecter.`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!ok) {
      return;
    }
    this.action = true;
    this.utilisateurService.supprimer(u.id).pipe(finalize(() => (this.action = false))).subscribe({
      next: () => {
        this.utilisateurs = this.utilisateurs.filter((x) => x.id !== u.id);
        this.notifications.success(`Compte ${u.email} supprimé.`);
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
