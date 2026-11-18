import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup } from '@angular/forms';
import { finalize } from 'rxjs';
import { Stage } from '../services/stage';
import { StageModel, StageStatus } from '../models/stage.model';
import { NotificationService } from '../shared/notification/notification.service';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { AuthService } from '../services/auth';
import { extractApiError } from '../shared/api-error';
import { ExportService } from '../services/export';
import { enregistrerFichier, messageErreurBlob } from '../shared/telechargement';

@Component({
  selector: 'app-stages',
  standalone: false,
  templateUrl: './stages.html',
  styleUrl: './stages.css',
})
export class Stages implements OnInit {
  stages: StageModel[] = [];
  searchFormGroup!: FormGroup;
  loading = false;

  constructor(
    private readonly stageService: Stage,
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
      .csv('stages', { keyword: this.searchFormGroup.value.keyword })
      .pipe(finalize(() => (this.exporting = false)))
      .subscribe({
        next: (r) => enregistrerFichier(r, 'stages.csv'),
        error: async (err) => this.notifications.error(await messageErreurBlob(err, "L'export a échoué.")),
      });
  }

  ngOnInit(): void {
    this.searchFormGroup = this.fb.group({ keyword: this.fb.control('') });
    this.loadStages();
  }

  trackById(_index: number, item: { id: number }): number {
    return item.id;
  }

  statusClass(status: StageStatus): string {
    switch (status) {
      case StageStatus.EN_COURS:
        return 'badge-green';
      case StageStatus.TERMINE:
        return 'badge-amber';
      case StageStatus.ANNULE:
        return 'badge-rose';
      default:
        return 'badge-slate';
    }
  }

  statusLabel(status: StageStatus): string {
    switch (status) {
      case StageStatus.PLANIFIE:
        return 'Planifié';
      case StageStatus.EN_COURS:
        return 'En cours';
      case StageStatus.TERMINE:
        return 'Terminé';
      case StageStatus.ANNULE:
        return 'Annulé';
      default:
        return status ?? '—';
    }
  }

  loadStages(): void {
    this.loading = true;
    this.stageService
      .getStages()
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stages = data),
        error: () => this.notifications.error('Impossible de charger la liste des stages.'),
      });
  }

  handleSearchStages(): void {
    const keyword = String(this.searchFormGroup.value.keyword ?? '').trim();
    this.loading = true;
    this.stageService
      .searchStages(keyword)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (data) => (this.stages = data),
        error: () => this.notifications.error('La recherche a échoué.'),
      });
  }

  async handleDeleteStage(stage: StageModel): Promise<void> {
    const confirmed = await this.confirm.confirm({
      title: 'Supprimer le stage',
      message: `Voulez-vous vraiment supprimer le stage « ${stage.sujet} » ?`,
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!confirmed) {
      return;
    }

    this.stageService.deleteStage(stage.id).subscribe({
      next: () => {
        this.stages = this.stages.filter((item) => item.id !== stage.id);
        this.notifications.success(`Le stage « ${stage.sujet} » a été supprimé.`);
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
