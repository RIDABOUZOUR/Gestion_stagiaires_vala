import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Encadrant } from '../services/encadrant';
import { EncadrantModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { AuthService } from '../services/auth';
import { extractApiError } from '../shared/api-error';

@Component({
  selector: 'app-encadrants',
  standalone: false,
  templateUrl: './encadrants.html',
  styleUrl: './encadrants.css',
})
export class Encadrants implements OnInit {
  encadrants: EncadrantModel[] = [];
  searchFormGroup!: FormGroup;
  loading = false;

  constructor(
    private readonly encadrantService: Encadrant,
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
    this.loadEncadrants();
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  loadEncadrants(): void {
    this.loading = true;
    this.encadrantService
      .getEncadrants()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.encadrants = data),
        error: () => this.notifications.error('Impossible de charger la liste des encadrants.'),
      });
  }

  handleSearchEncadrants(): void {
    const keyword = String(this.searchFormGroup.value.keyword ?? '').trim();
    this.loading = true;
    this.encadrantService
      .searchEncadrants(keyword)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.encadrants = data),
        error: () => this.notifications.error('La recherche a échoué.'),
      });
  }

  async handleDeleteEncadrant(encadrant: EncadrantModel): Promise<void> {
    const confirmed = await this.confirm.confirm({
      title: 'Supprimer l’encadrant',
      message: `Voulez-vous vraiment supprimer « ${encadrant.prenom} ${encadrant.nom} » ?`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.encadrantService.deleteEncadrant(encadrant.id).subscribe({
      next: () => {
        this.encadrants = this.encadrants.filter((item) => item.id !== encadrant.id);
        this.notifications.success(
          `L’encadrant « ${encadrant.prenom} ${encadrant.nom} » a été supprimé.`,
        );
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
