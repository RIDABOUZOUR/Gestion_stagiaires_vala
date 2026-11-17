import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { finalize } from 'rxjs';
import { Candidature } from '../models/candidature.model';
import { DepartementModel, StageModel, StageStatus } from '../models/stage.model';
import { CandidatureService } from '../services/candidature';
import { Departement } from '../services/departement';
import { Stage } from '../services/stage';
import { extractApiError } from '../shared/api-error';
import { ConfirmService } from '../shared/confirm/confirm.service';
import { CANDIDATURE_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';
import { erreurCv } from '../shared/cv';
import { enregistrerFichier, messageErreurBlob } from '../shared/telechargement';

@Component({
  selector: 'app-candidature-detail',
  standalone: false,
  templateUrl: './candidature-detail.html',
})
export class CandidatureDetail implements OnInit {
  readonly statuts = CANDIDATURE_STATUT;

  candidature: Candidature | null = null;
  loading = true;
  enCours = false;

  /** Formulaire affiché : refus (motif) ou acceptation (stage + département). */
  panneau: 'refus' | 'acceptation' | null = null;
  motif = '';
  stageId: number | null = null;
  departementId: number | null = null;
  stagesOuverts: StageModel[] = [];
  departements: DepartementModel[] = [];

  constructor(
    private readonly route: ActivatedRoute,
    private readonly router: Router,
    private readonly candidatureService: CandidatureService,
    private readonly stageService: Stage,
    private readonly departementService: Departement,
    private readonly notifications: NotificationService,
    private readonly confirm: ConfirmService,
  ) {}

  ouvertureCv = false;
  envoiCv = false;

  /**
   * Ouvre le CV dans un nouvel onglet. L'onglet est ouvert tout de suite (au clic)
   * pour ne pas être bloqué par le navigateur, puis reçoit le PDF une fois chargé
   * (le téléchargement passe par HttpClient pour envoyer le jeton).
   */
  voirCv(): void {
    const onglet = typeof window !== 'undefined' ? window.open('', '_blank') : null;
    this.ouvertureCv = true;
    this.candidatureService
      .cv(this.candidature!.id)
      .pipe(finalize(() => (this.ouvertureCv = false)))
      .subscribe({
        next: (reponse) => {
          if (onglet && reponse.body) {
            const url = URL.createObjectURL(new Blob([reponse.body], { type: 'application/pdf' }));
            onglet.location.href = url;
            setTimeout(() => URL.revokeObjectURL(url), 60_000);
          } else {
            enregistrerFichier(reponse, 'cv.pdf'); // fenêtre bloquée : téléchargement classique
          }
        },
        error: async (err) => {
          onglet?.close();
          this.notifications.error(await messageErreurBlob(err, 'Impossible d’ouvrir le CV.'));
        },
      });
  }

  /** Ajoute ou remplace le CV d'une candidature en cours de traitement. */
  envoyerCv(evenement: Event): void {
    const champ = evenement.target as HTMLInputElement;
    const fichier = champ.files?.[0] ?? null;
    champ.value = '';
    if (!fichier) {
      return;
    }
    const erreur = erreurCv(fichier);
    if (erreur) {
      this.notifications.error(erreur);
      return;
    }
    this.envoiCv = true;
    this.candidatureService
      .remplacerCv(this.candidature!.id, fichier)
      .pipe(finalize(() => (this.envoiCv = false)))
      .subscribe({
        next: (c) => {
          this.candidature = c;
          this.notifications.success('CV enregistré.');
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  get active(): boolean {
    return this.candidature?.statut === 'SOUMISE' || this.candidature?.statut === 'EN_ETUDE';
  }

  ngOnInit(): void {
    this.candidatureService
      .candidature(Number(this.route.snapshot.paramMap.get('id')))
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (c) => (this.candidature = c),
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  etudier(): void {
    this.appliquer(this.candidatureService.etudier(this.candidature!.id), 'Candidature mise à l’étude.');
  }

  async retirer(): Promise<void> {
    const ok = await this.confirm.confirm({
      title: 'Retirer la candidature',
      message: 'La candidature sera marquée comme retirée par le candidat.',
      confirmLabel: 'Retirer',
      danger: true,
    });
    if (ok) {
      this.appliquer(this.candidatureService.retirer(this.candidature!.id), 'Candidature retirée.');
    }
  }

  ouvrirRefus(): void {
    this.motif = '';
    this.panneau = 'refus';
  }

  refuser(): void {
    if (!this.motif.trim()) {
      this.notifications.error('Le motif du refus est obligatoire.');
      return;
    }
    this.appliquer(this.candidatureService.refuser(this.candidature!.id, this.motif.trim()), 'Candidature refusée.');
  }

  /** Acceptation : seuls les stages planifiés ou en cours peuvent accueillir un stagiaire. */
  ouvrirAcceptation(): void {
    this.panneau = 'acceptation';
    this.stageId = null;
    this.departementId = this.candidature?.departementSouhaiteId ?? null;
    this.stageService.getStages().subscribe({
      next: (s) => (this.stagesOuverts = s.filter((x) => x.status === StageStatus.PLANIFIE || x.status === StageStatus.EN_COURS)),
    });
    this.departementService.getDepartements().subscribe({ next: (d) => (this.departements = d) });
  }

  accepter(): void {
    if (!this.stageId) {
      this.notifications.error('Choisissez le stage d’affectation.');
      return;
    }
    this.appliquer(
      this.candidatureService.accepter(this.candidature!.id, Number(this.stageId), this.departementId ? Number(this.departementId) : null),
      'Candidature acceptée : le stagiaire a été créé.',
    );
  }

  async supprimer(): Promise<void> {
    const ok = await this.confirm.confirm({
      title: 'Supprimer la candidature',
      message: 'Cette action est définitive.',
      confirmLabel: 'Supprimer',
      danger: true,
    });
    if (!ok) {
      return;
    }
    this.candidatureService.supprimer(this.candidature!.id).subscribe({
      next: () => {
        this.notifications.success('Candidature supprimée.');
        void this.router.navigate(['/candidatures']);
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }

  private appliquer(appel: ReturnType<CandidatureService['etudier']>, message: string): void {
    this.enCours = true;
    appel.pipe(finalize(() => (this.enCours = false))).subscribe({
      next: (c) => {
        this.candidature = c;
        this.panneau = null;
        this.notifications.success(message);
      },
      error: (err) => this.notifications.error(extractApiError(err)),
    });
  }
}
