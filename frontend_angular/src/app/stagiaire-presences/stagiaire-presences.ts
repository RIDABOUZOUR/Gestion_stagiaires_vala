import { Component, Input, OnInit } from '@angular/core';
import { finalize } from 'rxjs';
import { StageStatus, StagiaireModel } from '../models/stage.model';
import { Presence, StatutPresence } from '../models/suivi.model';
import { SuiviService } from '../services/suivi';
import { extractApiError } from '../shared/api-error';
import {
  aujourdhui,
  decalerMois,
  isoDate,
  jourSemaine,
  joursDansLeMois,
  libelleMois,
  moisDe,
} from '../shared/dates';
import { PRESENCE_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';

export interface JourCalendrier {
  date: string;
  numero: number;
  weekend: boolean;
  saisissable: boolean;
  presence?: Presence;
}

/** Outil sélectionné : un statut à appliquer, ou l'effacement. */
export type OutilPresence = StatutPresence | 'EFFACER';

@Component({
  selector: 'app-stagiaire-presences',
  standalone: false,
  templateUrl: './stagiaire-presences.html',
})
export class StagiairePresences implements OnInit {
  @Input({ required: true }) stagiaire!: StagiaireModel;

  readonly statuts = PRESENCE_STATUT;
  readonly outils: OutilPresence[] = ['PRESENT', 'RETARD', 'ABSENT', 'ABSENCE_JUSTIFIEE', 'EFFACER'];
  readonly enTetes = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];

  mois = '';
  outil: OutilPresence = 'PRESENT';
  cases: (JourCalendrier | null)[] = [];
  loading = false;
  private presences = new Map<string, Presence>();
  private enregistrement = new Set<string>();

  constructor(
    private readonly suivi: SuiviService,
    private readonly notifications: NotificationService,
  ) {}

  get debut(): string {
    return this.stagiaire.stage.dateDebut;
  }

  get fin(): string {
    return this.stagiaire.stage.dateFin;
  }

  get stageAnnule(): boolean {
    return this.stagiaire.stage.status === StageStatus.ANNULE;
  }

  get libelleDuMois(): string {
    return libelleMois(this.mois);
  }

  get premierMois(): boolean {
    return this.mois <= moisDe(this.debut);
  }

  get dernierMois(): boolean {
    return this.mois >= moisDe(this.fin);
  }

  /** Décompte du mois affiché. */
  get resume(): { statut: StatutPresence; nombre: number }[] {
    const compte = new Map<StatutPresence, number>();
    for (const p of this.presences.values()) {
      if (p.date.startsWith(this.mois)) {
        compte.set(p.statut, (compte.get(p.statut) ?? 0) + 1);
      }
    }
    return (Object.keys(PRESENCE_STATUT) as StatutPresence[]).map((statut) => ({ statut, nombre: compte.get(statut) ?? 0 }));
  }

  get tauxPresence(): number | null {
    const r = this.resume;
    const total = r.reduce((s, x) => s + x.nombre, 0);
    if (total === 0) {
      return null;
    }
    const presents = r.filter((x) => x.statut === 'PRESENT' || x.statut === 'RETARD').reduce((s, x) => s + x.nombre, 0);
    return Math.round((1000 * presents) / total) / 10;
  }

  ngOnInit(): void {
    // Mois affiché : le mois courant s'il est dans le stage, sinon le premier ou le dernier mois du stage.
    const courant = moisDe(aujourdhui());
    this.mois = courant < moisDe(this.debut) ? moisDe(this.debut) : courant > moisDe(this.fin) ? moisDe(this.fin) : courant;
    this.charger();
  }

  changerMois(delta: number): void {
    this.mois = decalerMois(this.mois, delta);
    this.charger();
  }

  charger(): void {
    const premier = `${this.mois}-01`;
    const dernier = `${this.mois}-${String(joursDansLeMois(this.mois)).padStart(2, '0')}`;
    this.loading = true;
    this.suivi
      .presences(this.stagiaire.id, premier, dernier)
      .pipe(finalize(() => (this.loading = false)))
      .subscribe({
        next: (liste) => {
          liste.forEach((p) => this.presences.set(p.date, p));
          this.construire();
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  /** Grille du mois, alignée sur le lundi. */
  construire(): void {
    const [annee, mois] = this.mois.split('-').map(Number);
    const total = joursDansLeMois(this.mois);
    const ajd = aujourdhui();
    const cases: (JourCalendrier | null)[] = Array(jourSemaine(isoDate(annee, mois, 1))).fill(null);
    for (let j = 1; j <= total; j++) {
      const date = isoDate(annee, mois, j);
      cases.push({
        date,
        numero: j,
        weekend: jourSemaine(date) >= 5,
        saisissable: !this.stageAnnule && date >= this.debut && date <= this.fin && date <= ajd,
        presence: this.presences.get(date),
      });
    }
    this.cases = cases;
  }

  enCours(jour: JourCalendrier): boolean {
    return this.enregistrement.has(jour.date);
  }

  /** Applique l'outil sélectionné au jour cliqué. */
  appliquer(jour: JourCalendrier): void {
    if (!jour.saisissable || this.enCours(jour)) {
      return;
    }
    if (this.outil === 'EFFACER') {
      if (!jour.presence) {
        return;
      }
      this.enregistrement.add(jour.date);
      this.suivi
        .supprimerPresence(this.stagiaire.id, jour.date)
        .pipe(finalize(() => this.enregistrement.delete(jour.date)))
        .subscribe({
          next: () => {
            this.presences.delete(jour.date);
            this.construire();
          },
          error: (err) => this.notifications.error(extractApiError(err)),
        });
      return;
    }
    if (jour.presence?.statut === this.outil) {
      return;
    }
    this.enregistrement.add(jour.date);
    this.suivi
      .enregistrerPresence(this.stagiaire.id, jour.date, this.outil)
      .pipe(finalize(() => this.enregistrement.delete(jour.date)))
      .subscribe({
        next: (p) => {
          this.presences.set(p.date, p);
          this.construire();
        },
        error: (err) => this.notifications.error(extractApiError(err)),
      });
  }

  libelleOutil(outil: OutilPresence): string {
    return outil === 'EFFACER' ? 'Effacer' : PRESENCE_STATUT[outil].libelle;
  }
}
