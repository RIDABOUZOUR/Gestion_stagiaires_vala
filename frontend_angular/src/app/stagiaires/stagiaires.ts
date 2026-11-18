import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Stagiaire } from '../services/stagiaire';
import { StagiaireModel } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { AuthService } from '../services/auth';
import { extractApiError } from '../shared/api-error';
import { ExportService } from '../services/export';
import { enregistrerFichier, messageErreurBlob } from '../shared/telechargement';

@Component({
  selector: 'app-stagiaires',
  standalone: false,
  templateUrl: './stagiaires.html',
  styleUrl: './stagiaires.css',
})
export class Stagiaires implements OnInit {
  stagiaires: StagiaireModel[] = [];
  searchFormGroup!: FormGroup;
  loading = false;

  constructor(
    private readonly stagiaireService: Stagiaire,
    private readonly fb: FormBuilder,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
    readonly auth: AuthService,
    private readonly exportService: ExportService,
  ) {}

  /** Création, modification et suppression : réservées à la gestion (ADMIN, RH). */
  get peutGerer(): boolean {
    return this.auth.aRole('ADMIN', 'RH');
  }

  exporting = false;

  /** Export CSV avec le mot-clé de recherche courant. */
  exporter(): void {
    this.exporting = true;
    this.exportService
      .csv('stagiaires', { keyword: this.searchFormGroup.value.keyword })
      .pipe(finalize(() => (this.exporting = false)))
      .subscribe({
        next: (r) => enregistrerFichier(r, 'stagiaires.csv'),
        error: async (err) => this.notifications.error(await messageErreurBlob(err, "L'export a échoué.")),
      });
  }

  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({ keyword: this.fb.control('') });
    this.loadStagiaires();
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  loadStagiaires(): void {
    this.loading = true;
    this.stagiaireService
      .getStagiaires()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stagiaires = data),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  handleSearchStagiaires(): void {
    const keyword = String(this.searchFormGroup.value.keyword ?? '').trim();
    this.loading = true;
    this.stagiaireService
      .searchStagiaires(keyword)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stagiaires = data),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  /** Identifiant en cours de suppression : bloque un second clic pendant la requête. */
  suppressionEnCours: number | null = null;

  async handleDeleteStagiaire(stagiaire: StagiaireModel): Promise<void> {
    if (this.suppressionEnCours !== null) {
      return;
    }
    const confirmed = await this.confirm.confirm({
      title: 'Supprimer le stagiaire',
      message: `Voulez-vous vraiment supprimer « ${stagiaire.prenom} ${stagiaire.nom} » ?`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.suppressionEnCours = stagiaire.id;
    this.stagiaireService.deleteStagiaire(stagiaire.id).pipe(finalize(() => (this.suppressionEnCours = null))).subscribe({
      next: () => {
        this.stagiaires = this.stagiaires.filter((item) => item.id !== stagiaire.id);
        this.notifications.success(
          `Le stagiaire « ${stagiaire.prenom} ${stagiaire.nom} » a été supprimé.`,
        );
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
