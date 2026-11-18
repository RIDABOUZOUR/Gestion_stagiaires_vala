import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize, forkJoin } from 'rxjs';
import { StageModel, StageStatus, StagiaireModel } from '../models/stage.model';
import { StageHistorique } from '../models/suivi.model';
import { AuthService } from '../services/auth';
import { Stage } from '../services/stage';
import { Stagiaire } from '../services/stagiaire';
import { extractApiError } from '../shared/api-error';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { STAGE_STATUT, STAGE_TRANSITIONS, estStatutFinal } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';

interface ActionStatut {
  cible: StageStatus;
  action: 'demarrer' | 'terminer' | 'annuler';
  libelle: string;
  classe: string;
}

const ACTIONS: Record<StageStatus, Omit<ActionStatut, 'cible'> | null> = {
  PLANIFIE: null,
  EN_COURS: { action: 'demarrer', libelle: 'Démarrer', classe: 'btn-primary' },
  TERMINE: { action: 'terminer', libelle: 'Terminer', classe: 'btn-secondary' },
  ANNULE: { action: 'annuler', libelle: 'Annuler le stage', classe: 'btn-danger' },
};

@Component({
  selector: 'app-stage-detail',
  standalone: false,
  templateUrl: './stage-detail.html',
})
export class StageDetail implements OnInit {
  readonly estFinal = estStatutFinal;
  readonly statuts = STAGE_STATUT;

  stage: StageModel | null = null;
  historique: StageHistorique[] = [];
  stagiaires: StagiaireModel[] = [];
  loading = true;
  enCours = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly stageService: Stage,
    private readonly stagiaireService: Stagiaire,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
    readonly auth: AuthService,
  ) {}

  get id(): number {
    return Number(this.route.snapshot.paramMap.get('id'));
  }

  get peutGerer(): boolean {
    return this.auth.aRole('ADMIN', 'RH');
  }

  /** Actions possibles depuis le statut actuel, dans l'ordre du cycle de vie. */
  get actions(): ActionStatut[] {
    if (!this.stage) {
      return [];
    }
    return STAGE_TRANSITIONS[this.stage.status]
      .map((cible) => ({ cible, ...ACTIONS[cible]! }))
      .filter((a) => !!a.action);
  }

  ngOnInit(): void {
    this.charger();
  }

  charger(): void {
    this.loading = true;
    forkJoin({
      stage: this.stageService.getStageById(this.id),
      historique: this.stageService.getHistorique(this.id),
      stagiaires: this.stagiaireService.rechercher({ stageId: this.id }, 0, 100),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: ({ stage, historique, stagiaires }) => {
          this.stage = stage;
          this.historique = [...historique].reverse(); // le plus récent en premier
          this.stagiaires = stagiaires.content;
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  async executer(action: ActionStatut): Promise<void> {
    if (!this.stage) {
      return;
    }
    if (action.action === 'annuler') {
      const ok = await this.confirm.confirm({
        title: 'Annuler le stage',
        message: `Le stage « ${this.stage.sujet} » sera définitivement annulé. Continuer ?`,
        confirmLabel: 'Annuler le stage',
        danger: true,
      });
      if (!ok) {
        return;
      }
    }
    this.enCours = true;
    this.stageService
      .changerStatut(this.stage.id, action.action)
      .pipe(finalize(() => (this.enCours = false)))
      .subscribe({
        next: () => {
          this.notifications.success(`Stage passé au statut « ${STAGE_STATUT[action.cible].libelle} ».`);
          this.charger();
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  libelleStatut(statut: StageStatus | null): string {
    return statut ? STAGE_STATUT[statut].libelle : 'Création';
  }
}
