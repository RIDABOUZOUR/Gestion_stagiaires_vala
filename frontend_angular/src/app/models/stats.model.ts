import { StageModel, StageStatus } from './stage.model';
import { CandidatureStatus } from './candidature.model';

export type Repartition = Record<string, number>;

export interface ResumeStatistiques {
  totalStagiaires: number;
  stagiairesActifs: number;
  totalStages: number;
  stagesEnCours: number;
  stagesPlanifies: number;
  stagesAEcheance: number;
  joursEcheance: number;
  totalEncadrants: number;
  totalDepartements: number;
}

export interface AssiduiteStagiaire {
  stagiaireId: number;
  nom: string;
  prenom: string;
  departement: string;
  joursSaisis: number;
  presents: number;
  retards: number;
  absences: number;
  absencesJustifiees: number;
  tauxPresence: number | null;
}

export interface AssiduiteStatistiques {
  tauxGlobal: number | null;
  parDepartement: Repartition;
  stagiaires: AssiduiteStagiaire[];
}

export interface RapportEnRetard {
  stagiaireId: number;
  nom: string;
  prenom: string;
  email: string;
  sujetStage: string;
  encadrant: string | null;
  dernierRapport: string | null;
  joursSansRapport: number;
}

export interface EvaluationStatistiques {
  nombreEvaluations: number;
  noteMoyenne: number | null;
  tauxRecommandation: number | null;
  stagiairesTerminesSansEvaluation: number;
  moyennesCompetences: Repartition;
  repartitionNotes: Repartition;
  moyenneParDepartement: Repartition;
  moyenneParEncadrant: Repartition;
  moyenneParFiliere: Repartition;
}

export interface CandidatureStatistiques {
  total: number;
  parStatut: Record<CandidatureStatus, number>;
  parMois: Repartition;
  tauxAcceptation: number | null;
  delaiMoyenDecisionJours: number | null;
  parOrganisme: Repartition;
}

export interface IndicateursStages {
  nombreStages: number;
  stagesTermines: number;
  stagesAnnules: number;
  dureeMoyenneJours: number | null;
  tauxAchevement: number | null;
  tauxAnnulation: number | null;
}

export interface DureesEtTaux {
  global: IndicateursStages;
  parEncadrant: Record<string, IndicateursStages>;
  parDepartement: Record<string, IndicateursStages>;
}

export interface ProfilStagiaires {
  total: number;
  parSexe: Repartition;
  parNiveau: Repartition;
  parFiliere: Repartition;
  parDepartementEtSexe: Record<string, Repartition>;
  parDepartementEtNiveau: Record<string, Repartition>;
}

export type StagesParStatut = Record<StageStatus, number>;
export type StagesAEcheance = StageModel[];
