import {
  Component,
  ElementRef,
  NgZone,
  OnDestroy,
  OnInit,
  PLATFORM_ID,
  QueryList,
  ViewChildren,
  inject,
} from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import Chart, { ChartConfiguration } from 'chart.js/auto';
import { Observable, catchError, finalize, forkJoin, of } from 'rxjs';
import { StageModel } from '../models/stage.model';
import {
  AssiduiteStatistiques,
  CandidatureStatistiques,
  DureesEtTaux,
  EvaluationStatistiques,
  IndicateursStages,
  ProfilStagiaires,
  RapportEnRetard,
  Repartition,
  ResumeStatistiques,
  StagesParStatut,
} from '../models/stats.model';
import { StatistiquesService } from '../services/statistiques';
import { CANDIDATURE_STATUT, STAGE_STATUT } from '../shared/libelles';
import { NotificationService } from '../shared/notification/notification.service';

/** Toutes les données du tableau de bord ; une source en échec vaut null. */
export interface DonneesTableau {
  resume: ResumeStatistiques | null;
  statuts: StagesParStatut | null;
  echeances: StageModel[] | null;
  durees: DureesEtTaux | null;
  profil: ProfilStagiaires | null;
  assiduite: AssiduiteStatistiques | null;
  retards: RapportEnRetard[] | null;
  evaluations: EvaluationStatistiques | null;
  candidatures: CandidatureStatistiques | null;
  departements: Repartition | null;
  filieres: Repartition | null;
  organismes: Repartition | null;
  encadrants: Repartition | null;
}

interface Indicateur {
  libelle: string;
  valeur: string;
  detail?: string;
  icone: string;
  couleur: string;
}

interface GraphiqueConfig {
  id: string;
  titre: string;
  icone: string;
  couleur: string;
  type: 'bar' | 'doughnut' | 'line';
  /** Suffixe des valeurs (ex. « % ») et maximum de l'axe. */
  unite?: string;
  max?: number;
  donnees: (d: DonneesTableau) => Repartition | null;
}

const SEXES: Record<string, { libelle: string }> = { FEMME: { libelle: 'Femme' }, HOMME: { libelle: 'Homme' } };

const PALETTE = ['#4f46e5', '#0ea5e9', '#10b981', '#f59e0b', '#f43f5e', '#8b5cf6', '#14b8a6', '#64748b'];

/** Remplace les clés techniques (EN_COURS…) par leurs libellés. */
function renommer<K extends string>(valeurs: Record<K, number> | null, libelles: Record<K, { libelle: string }>): Repartition | null {
  if (!valeurs) {
    return null;
  }
  const r: Repartition = {};
  (Object.keys(valeurs) as K[]).forEach((k) => (r[libelles[k]?.libelle ?? k] = valeurs[k]));
  return r;
}

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.html',
  styleUrls: ['./dashboard.css'],
  standalone: false,
})
export class Dashboard implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly stats = inject(StatistiquesService);
  private readonly notifications = inject(NotificationService);
  private readonly zone = inject(NgZone);

  loading = true;
  donnees: DonneesTableau | null = null;
  indicateurs: Indicateur[] = [];
  /** Calculés une seule fois au chargement (jamais pendant la détection de changements). */
  tableauxDurees: { titre: string; lignes: { nom: string; ind: IndicateursStages }[] }[] = [];
  private graphiquesAvecDonnees = new Set<string>();
  private instances: Chart[] = [];

  @ViewChildren('chartCanvas')
  private canvases!: QueryList<ElementRef<HTMLCanvasElement>>;

  readonly graphiques: GraphiqueConfig[] = [
    { id: 'statuts', titre: 'Stages par statut', icone: 'briefcase', couleur: 'bg-indigo-50 text-indigo-600', type: 'doughnut',
      donnees: (d) => renommer(d.statuts, STAGE_STATUT) },
    { id: 'candidaturesMois', titre: 'Candidatures reçues (12 mois)', icone: 'user-plus', couleur: 'bg-sky-50 text-sky-600', type: 'line',
      donnees: (d) => d.candidatures?.parMois ?? null },
    { id: 'departements', titre: 'Stagiaires par département', icone: 'building-office', couleur: 'bg-indigo-50 text-indigo-600', type: 'bar',
      donnees: (d) => d.departements },
    { id: 'assiduite', titre: 'Assiduité par département (stages en cours)', icone: 'check-circle', couleur: 'bg-emerald-50 text-emerald-600',
      type: 'bar', unite: '%', max: 100, donnees: (d) => d.assiduite?.parDepartement ?? null },
    { id: 'notes', titre: 'Répartition des notes de fin de stage', icone: 'chart-bar', couleur: 'bg-amber-50 text-amber-600', type: 'bar',
      donnees: (d) => d.evaluations?.repartitionNotes ?? null },
    { id: 'competences', titre: 'Compétences moyennes (sur 5)', icone: 'chart-bar', couleur: 'bg-violet-50 text-violet-600', type: 'bar',
      max: 5, donnees: (d) => d.evaluations?.moyennesCompetences ?? null },
    { id: 'sexe', titre: 'Stagiaires par sexe', icone: 'users', couleur: 'bg-rose-50 text-rose-600', type: 'doughnut',
      donnees: (d) => renommer(d.profil?.parSexe ?? null, SEXES) },
    { id: 'niveau', titre: 'Stagiaires par niveau', icone: 'users', couleur: 'bg-teal-50 text-teal-600', type: 'bar',
      donnees: (d) => d.profil?.parNiveau ?? null },
    { id: 'candidaturesStatut', titre: 'Candidatures par statut', icone: 'user-plus', couleur: 'bg-sky-50 text-sky-600', type: 'doughnut',
      donnees: (d) => renommer(d.candidatures?.parStatut ?? null, CANDIDATURE_STATUT) },
    { id: 'filieres', titre: 'Stagiaires par filière', icone: 'chart-bar', couleur: 'bg-sky-50 text-sky-600', type: 'bar',
      donnees: (d) => d.filieres },
    { id: 'organismes', titre: 'Stagiaires par organisme', icone: 'briefcase', couleur: 'bg-emerald-50 text-emerald-600', type: 'bar',
      donnees: (d) => d.organismes },
    { id: 'encadrants', titre: 'Stagiaires par encadrant', icone: 'identification', couleur: 'bg-amber-50 text-amber-600', type: 'bar',
      donnees: (d) => d.encadrants },
  ];

  ngOnInit(): void {
    this.charger();
  }

  ngOnDestroy(): void {
    this.detruire();
  }

  charger(): void {
    this.loading = true;
    let echecs = 0;
    const sur = <T>(source: Observable<T>): Observable<T | null> =>
      source.pipe(catchError(() => { echecs++; return of(null); }));

    forkJoin({
      resume: sur(this.stats.resume()),
      statuts: sur(this.stats.stagesParStatut()),
      echeances: sur(this.stats.stagesAEcheance(30)),
      durees: sur(this.stats.dureesEtTaux()),
      profil: sur(this.stats.profil()),
      assiduite: sur(this.stats.assiduite()),
      retards: sur(this.stats.rapportsEnRetard(14)),
      evaluations: sur(this.stats.evaluations()),
      candidatures: sur(this.stats.candidatures()),
      departements: sur(this.stats.repartition('departements')),
      filieres: sur(this.stats.repartition('filieres')),
      organismes: sur(this.stats.repartition('organismes')),
      encadrants: sur(this.stats.repartition('encadrants')),
    })
      .pipe(finalize(() => (this.loading = false)))
      .subscribe((donnees) => {
        this.donnees = donnees;
        this.indicateurs = Dashboard.construireIndicateurs(donnees);
        this.tableauxDurees = donnees.durees
          ? [
              { titre: 'Par département', lignes: Dashboard.lignes(donnees.durees.parDepartement) },
              { titre: 'Par encadrant', lignes: Dashboard.lignes(donnees.durees.parEncadrant) },
            ]
          : [];
        this.graphiquesAvecDonnees = new Set(
          this.graphiques.filter((g) => Object.keys(g.donnees(donnees) ?? {}).length > 0).map((g) => g.id),
        );
        this.dessiner(donnees);
        if (echecs > 0) {
          this.notifications.error(`${echecs} indicateur(s) n'ont pas pu être chargés.`);
        }
      });
  }

  static construireIndicateurs(d: DonneesTableau): Indicateur[] {
    const r = d.resume;
    const pct = (v: number | null | undefined) => (v === null || v === undefined ? '—' : `${v} %`);
    return [
      { libelle: 'Stagiaires actifs', valeur: r ? String(r.stagiairesActifs) : '—', detail: r ? `sur ${r.totalStagiaires} au total` : undefined,
        icone: 'users', couleur: 'bg-indigo-50 text-indigo-600' },
      { libelle: 'Stages en cours', valeur: r ? String(r.stagesEnCours) : '—', detail: r ? `${r.stagesPlanifies} planifié(s)` : undefined,
        icone: 'briefcase', couleur: 'bg-emerald-50 text-emerald-600' },
      { libelle: 'Fin dans 30 jours', valeur: r ? String(r.stagesAEcheance) : '—', detail: 'stages en cours',
        icone: 'check-circle', couleur: 'bg-amber-50 text-amber-600' },
      { libelle: 'Assiduité', valeur: pct(d.assiduite?.tauxGlobal), detail: 'stages en cours',
        icone: 'check-circle', couleur: 'bg-teal-50 text-teal-600' },
      { libelle: 'Note moyenne', valeur: d.evaluations?.noteMoyenne != null ? `${d.evaluations.noteMoyenne} / 20` : '—',
        detail: d.evaluations ? `${d.evaluations.nombreEvaluations} évaluation(s)` : undefined, icone: 'chart-bar', couleur: 'bg-violet-50 text-violet-600' },
      { libelle: "Taux d'acceptation", valeur: pct(d.candidatures?.tauxAcceptation),
        detail: d.candidatures?.delaiMoyenDecisionJours != null ? `décision en ${d.candidatures.delaiMoyenDecisionJours} j en moyenne` : undefined,
        icone: 'user-plus', couleur: 'bg-sky-50 text-sky-600' },
    ];
  }

  /** Lignes d'un tableau d'indicateurs (par département ou par encadrant). */
  static lignes(groupes: Record<string, IndicateursStages> | undefined): { nom: string; ind: IndicateursStages }[] {
    return groupes ? Object.entries(groupes).map(([nom, ind]) => ({ nom, ind })) : [];
  }

  aDesDonnees(config: GraphiqueConfig): boolean {
    return this.graphiquesAvecDonnees.has(config.id);
  }

  private dessiner(d: DonneesTableau): void {
    // Chart.js manipule le DOM : uniquement dans le navigateur.
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    this.detruire();
    const canvases = this.canvases.toArray();
    // Hors de la zone Angular : les animations et le redimensionnement de Chart.js
    // ne déclenchent pas de détection de changements à chaque image.
    this.zone.runOutsideAngular(() => {
      this.graphiques.forEach((config, i) => {
        const valeurs = config.donnees(d);
        const canvas = canvases[i]?.nativeElement;
        if (canvas && valeurs && Object.keys(valeurs).length > 0) {
          this.instances.push(new Chart(canvas, Dashboard.configuration(config, valeurs)));
        }
      });
    });
  }

  static configuration(config: GraphiqueConfig, valeurs: Repartition): ChartConfiguration {
    const labels = Object.keys(valeurs);
    const data = Object.values(valeurs);
    const suffixe = config.unite ? ` ${config.unite}` : '';
    const tooltip = { padding: 10, cornerRadius: 8, callbacks: { label: (c: { formattedValue: string }) => `${c.formattedValue}${suffixe}` } };
    // Pas d'animation : une douzaine de graphiques animés en même temps saturent le
    // fil principal du navigateur (page figée sur des machines modestes).
    const animation = false as const;

    if (config.type === 'doughnut') {
      return {
        type: 'doughnut',
        data: { labels, datasets: [{ data, backgroundColor: PALETTE, borderWidth: 2 }] },
        options: { animation, responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom' }, tooltip } },
      };
    }
    return {
      type: config.type,
      data: {
        labels,
        datasets: [{
          label: config.titre,
          data,
          backgroundColor: config.type === 'line' ? 'rgba(14, 165, 233, 0.15)' : PALETTE[0],
          borderColor: config.type === 'line' ? '#0ea5e9' : undefined,
          fill: config.type === 'line',
          tension: 0.3,
          borderRadius: 6,
          maxBarThickness: 48,
        }],
      },
      options: {
        animation,
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false }, tooltip },
        scales: {
          y: { beginAtZero: true, max: config.max, ticks: { precision: config.max === 5 ? 1 : 0 }, grid: { color: '#e2e8f0' } },
          x: { grid: { display: false } },
        },
      },
    };
  }

  private detruire(): void {
    this.instances.forEach((c) => c.destroy());
    this.instances = [];
  }
}
