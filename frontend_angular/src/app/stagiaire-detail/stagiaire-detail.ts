import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { StageStatus, StagiaireModel } from '../models/stage.model';
import { AuthService } from '../services/auth';
import { EvaluationService } from '../services/evaluation';
import { Stagiaire } from '../services/stagiaire';
import { extractApiError } from '../shared/api-error';
import { STAGE_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';
import { enregistrerFichier, messageErreurBlob } from '../shared/telechargement';

export type OngletStagiaire = 'infos' | 'presences' | 'rapports' | 'evaluation';

@Component({
  selector: 'app-stagiaire-detail',
  standalone: false,
  templateUrl: './stagiaire-detail.html',
})
export class StagiaireDetail implements OnInit {
  readonly statuts = STAGE_STATUT;
  readonly onglets: { id: OngletStagiaire; libelle: string }[] = [
    { id: 'infos', libelle: 'Informations' },
    { id: 'presences', libelle: 'Présences' },
    { id: 'rapports', libelle: 'Rapports' },
    { id: 'evaluation', libelle: 'Évaluation' },
  ];

  stagiaire: StagiaireModel | null = null;
  onglet: OngletStagiaire = 'infos';
  loading = true;
  telechargement = false;

  constructor(
    private readonly route: ActivatedRoute,
    private readonly stagiaireService: Stagiaire,
    private readonly evaluationService: EvaluationService,
    private readonly notifications: NotificationService,
    readonly auth: AuthService,
  ) {}

  get peutGerer(): boolean {
    return this.auth.aRole('ADMIN', 'RH');
  }

  /** L'attestation n'existe qu'une fois le stage terminé (réservée à la gestion). */
  get attestationDisponible(): boolean {
    return this.peutGerer && this.stagiaire?.stage?.status === StageStatus.TERMINE;
  }

  ngOnInit(): void {
    const onglet = this.route.snapshot.queryParamMap.get('onglet') as OngletStagiaire | null;
    if (onglet && this.onglets.some((o) => o.id === onglet)) {
      this.onglet = onglet;
    }
    this.stagiaireService
      .getStagiaireById(Number(this.route.snapshot.paramMap.get('id')))
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (s) => (this.stagiaire = s),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  telechargerAttestation(): void {
    if (!this.stagiaire) {
      return;
    }
    this.telechargement = true;
    this.evaluationService
      .attestation(this.stagiaire.id)
      .pipe(finalize(() => (this.telechargement = false)))
      .subscribe({
        next: (reponse) => {
          enregistrerFichier(reponse, `attestation-${this.stagiaire!.nom}.pdf`);
          const numero = reponse.headers.get('X-Attestation-Numero');
          this.notifications.success(numero ? `Attestation ${numero} téléchargée.` : 'Attestation téléchargée.');
        },
        error: async (err) =>
          this.notifications.error(await messageErreurBlob(err, "Impossible de générer l'attestation.")),
      });
  }
}
