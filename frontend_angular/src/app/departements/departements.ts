import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Departement } from '../services/departement';
import { DepartementModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { AuthService } from '../services/auth';
import { extractApiError } from '../shared/api-error';

@Component({
  selector: 'app-departements',
  standalone: false,
  templateUrl: './departements.html',
  styleUrl: './departements.css',
})
export class Departements implements OnInit {
  departements: DepartementModel[] = [];
  searchFormGroup!: FormGroup;
  loading = false;

  constructor(
    private readonly departementService: Departement,
    private readonly fb: FormBuilder,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
    readonly auth: AuthService,
  ) {}

  /** Création, modification et suppression : réservées à la gestion (ADMIN, RH). */
  get peutGerer(): boolean {
    return this.auth.aRole('ADMIN', 'RH');
  }

  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({ keyword: this.fb.control('') });
    this.loadDepartements();
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  loadDepartements(): void {
    this.loading = true;
    this.departementService
      .getDepartements()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.departements = data),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  handleSearchDepartements(): void {
    const keyword = String(this.searchFormGroup.value.keyword ?? '').trim();
    this.loading = true;
    this.departementService
      .searchDepartements(keyword)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.departements = data),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  /** Identifiant en cours de suppression : bloque un second clic pendant la requête. */
  suppressionEnCours: number | null = null;

  async handleDeleteDepartement(departement: DepartementModel): Promise<void> {
    if (this.suppressionEnCours !== null) {
      return;
    }
    const confirmed = await this.confirm.confirm({
      title: 'Supprimer le département',
      message: `Voulez-vous vraiment supprimer le département « ${departement.nom} » ?`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.suppressionEnCours = departement.id;
    this.departementService.deleteDepartement(departement.id).pipe(finalize(() => (this.suppressionEnCours = null))).subscribe({
      next: () => {
        this.departements = this.departements.filter((item) => item.id !== departement.id);
        this.notifications.success(`Le département « ${departement.nom} » a été supprimé.`);
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
